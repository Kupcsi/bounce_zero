#include "graphics.h"
#include "font9.h"
#include "font12lite.h"
#include "font23.h"
#include "font24.h"
#include "types.h"  // Для SCREEN_WIDTH/SCREEN_HEIGHT
#include "png.h"    // Для texture_t
#include <pspgu.h>
#include <pspdisplay.h>
#include <pspkernel.h>
#include <stdint.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>   // Для sinf/cosf в graphics_draw_button_x

// Используем константы из graphics.h для VRAM буферов
// (локальные #define заменены на глобальные константы)

// Размеры буферов (байты на пиксель)
#define FRAMEBUFFER_BPP 4  // GU_PSM_8888 = 4 байта на пиксель

// Увеличенный буфер команд GU для более сложной отрисовки
#define GU_CMD_LIST_SIZE (256 * 1024)  // 256KB буфер команд GU (стандартный размер)
static char s_list[GU_CMD_LIST_SIZE] __attribute__((aligned(64)));


// VRAM буферы - вычисляются динамически
static void* s_draw_buffer;
static void* s_disp_buffer;

// Единое управление состоянием текстур
// ИНВАРИАНТ: кадр начинается в plain-режиме (текстуры выключены)
static int s_texturing_enabled = 0;


// Sprite batching система (по образцу pspsdk/samples/gu/sprite)
#define MAX_SPRITES_PER_BATCH 128
typedef struct {
    float u, v;
    float x, y, z;
} BatchVertex;

typedef struct {
    BatchVertex vertices[MAX_SPRITES_PER_BATCH * 2]; // GU_SPRITES = 2 вершины на спрайт
    texture_t* current_texture;
    int count;
} SpriteBatch;

static SpriteBatch s_batch;

// Forward declarations
static void batch_init(void);

// Простые примитивы без текстур
typedef struct {
    short x, y, z;
} Vertex2D;

void graphics_init(void) {
    // Размеры буферов
    const size_t framebuffer_size = VRAM_BUFFER_WIDTH * VRAM_BUFFER_HEIGHT * FRAMEBUFFER_BPP;
    
    // Буферы как смещения в VRAM (не абсолютные адреса!)
    s_draw_buffer = (void*)0;                    // смещение 0
    s_disp_buffer = (void*)framebuffer_size;     // смещение после первого FB
    
    sceGuInit();

    sceGuStart(GU_DIRECT, s_list);
    sceGuDrawBuffer(GU_PSM_8888, s_draw_buffer, VRAM_BUFFER_WIDTH);
    sceGuDispBuffer(SCREEN_WIDTH, SCREEN_HEIGHT, s_disp_buffer, VRAM_BUFFER_WIDTH);
    // Depth buffer не выделяем - не нужен для 2D рендера

    sceGuOffset(2048 - (SCREEN_WIDTH / 2), 2048 - (SCREEN_HEIGHT / 2));
    sceGuViewport(2048, 2048, SCREEN_WIDTH, SCREEN_HEIGHT);
    sceGuEnable(GU_SCISSOR_TEST);
    sceGuScissor(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    // Включаем альфа-блендинг внутри активного списка
    sceGuEnable(GU_BLEND);
    sceGuBlendFunc(GU_ADD, GU_SRC_ALPHA, GU_ONE_MINUS_SRC_ALPHA, 0, 0);

    // Очистить буферы перед показом (избежать мусора)
    // Цвет уже в ABGR формате, передаём напрямую
    sceGuClearColor(0);
    sceGuClear(GU_COLOR_BUFFER_BIT);
    sceGuFinish();
    sceGuSync(0, 0);
    sceDisplayWaitVblankStart();
    sceGuDisplay(GU_TRUE);
    
    // Инвариант: начинаем кадр в plain-режиме (текстуры выключены)
    s_texturing_enabled = 0;
    
    // Инициализация batch системы
    batch_init();
}

void graphics_start_frame(void) {
    sceGuStart(GU_DIRECT, s_list);
}

void graphics_set_scissor_fullscreen(void) {
    sceGuScissor(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
}

void graphics_clear(u32 color) {
    sceGuScissor(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    sceGuClearColor(color);
    sceGuClear(GU_COLOR_BUFFER_BIT);
}


void graphics_draw_rect(float x, float y, float w, float h, u32 color) {
    // Не трогаем GU_TEXTURE_2D / GU_BLEND — вариант B
    Vertex2D* v = (Vertex2D*)sceGuGetMemory(2 * sizeof(Vertex2D));

    v[0].x = (short)x;
    v[0].y = (short)y;
    v[0].z = 0;

    v[1].x = (short)(x + w);
    v[1].y = (short)(y + h);
    v[1].z = 0;

    // sceGuColor принимает цвет в том же ABGR формате что и graphics_clear()
    sceGuColor(color);
    sceGuDrawArray(GU_SPRITES, GU_VERTEX_16BIT | GU_TRANSFORM_2D, 2, 0, v);
}


// Прямое декодирование UTF-8 в индекс таблицы font9
int utf8_decode_to_index(const char* str, int* bytes_read) {
    unsigned char c = (unsigned char)str[0];
    *bytes_read = 1;

    // ASCII символы (0-127) - прямое соответствие
    if (c < 0x80) {
        return c;
    }

    // 2-байтная UTF-8 последовательность (кириллица)
    if ((c & 0xE0) == 0xC0) {
        if (str[1] == '\0') return 32; // Пробел для обрезанной последовательности
        unsigned char c2 = (unsigned char)str[1];
        if ((c2 & 0xC0) != 0x80) return 32; // Пробел для невалидной последовательности
        *bytes_read = 2;

        int unicode_char = ((c & 0x1F) << 6) | (c2 & 0x3F);

        // Маппинг кириллицы по реальному расположению в массиве
        switch (unicode_char) {
            // Заглавные А-Я (позиции 128-159) - в порядке Unicode без Ё
            case 0x0410: return 128; // А
            case 0x0411: return 129; // Б
            case 0x0412: return 130; // В
            case 0x0413: return 131; // Г
            case 0x0414: return 132; // Д
            case 0x0415: return 133; // Е
            case 0x0416: return 135; // Ж (сдвиг на +1 из-за Ё)
            case 0x0417: return 136; // З
            case 0x0418: return 137; // И
            case 0x0419: return 138; // Й
            case 0x041A: return 139; // К
            case 0x041B: return 140; // Л
            case 0x041C: return 141; // М
            case 0x041D: return 142; // Н
            case 0x041E: return 143; // О
            case 0x041F: return 144; // П
            case 0x0420: return 145; // Р
            case 0x0421: return 146; // С
            case 0x0422: return 147; // Т
            case 0x0423: return 148; // У
            case 0x0424: return 149; // Ф
            case 0x0425: return 150; // Х
            case 0x0426: return 151; // Ц
            case 0x0427: return 152; // Ч
            case 0x0428: return 153; // Ш
            case 0x0429: return 154; // Щ
            case 0x042A: return 155; // Ъ
            case 0x042B: return 156; // Ы
            case 0x042C: return 157; // Ь
            case 0x042D: return 158; // Э
            case 0x042E: return 159; // Ю
            case 0x042F: return 160; // Я (сдвиг на +1)

            // Строчные а-я (позиции 161-192) - сдвиг на +1 из-за Ё
            case 0x0430: return 161; // а
            case 0x0431: return 162; // б
            case 0x0432: return 163; // в
            case 0x0433: return 164; // г
            case 0x0434: return 165; // д
            case 0x0435: return 166; // е
            case 0x0436: return 168; // ж (сдвиг на +2: +1 от Ё, +1 от ё)
            case 0x0437: return 169; // з
            case 0x0438: return 170; // и
            case 0x0439: return 171; // й
            case 0x043A: return 172; // к
            case 0x043B: return 173; // л
            case 0x043C: return 174; // м
            case 0x043D: return 175; // н
            case 0x043E: return 176; // о
            case 0x043F: return 177; // п
            case 0x0440: return 178; // р
            case 0x0441: return 179; // с
            case 0x0442: return 180; // т
            case 0x0443: return 181; // у
            case 0x0444: return 182; // ф
            case 0x0445: return 183; // х
            case 0x0446: return 184; // ц
            case 0x0447: return 185; // ч
            case 0x0448: return 186; // ш
            case 0x0449: return 187; // щ
            case 0x044A: return 188; // ъ
            case 0x044B: return 189; // ы
            case 0x044C: return 190; // ь
            case 0x044D: return 191; // э
            case 0x044E: return 192; // ю
            case 0x044F: return 193; // я

            // Ё после Е (позиция 134)
            case 0x0401: return 134; // Ё
            // ё после е (позиция 167)
            case 0x0451: return 167; // ё
        }
    }

    // Неизвестный символ -> пробел
    return 32;
}

void graphics_draw_text(float x, float y, const char* text, u32 color, int font_height) {
    if (!text) return;
    float cur_x = x;
    int i = 0;

    // Выбор шрифта по высоте
    int height, spacing;
    if (font_height == 23) {
        height = FONT23_HEIGHT;
        spacing = FONT23_SPACING;
    } else if (font_height == 12) {
        height = FONT12LITE_HEIGHT;
        spacing = FONT12LITE_SPACING;
    } else {
        height = FONT9_HEIGHT;
        spacing = FONT9_SPACING;
    }

    while (text[i] != '\0') {
        int bytes_read;
        int index = utf8_decode_to_index(&text[i], &bytes_read);

        int width;

        if (font_height == 23) {
            const Glyph23* glyph = font23_get_glyph_by_index(index);
            width = glyph->width;
            // Рисуем символ (font23 использует u16 для строк)
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (glyph->row[row] & (1 << (15 - col))) {
                        graphics_draw_rect(cur_x + col, y + row, 1, 1, color);
                    }
                }
            }
        } else {
            const u8* glyph_data;
            if (font_height == 12) {
                const Glyph12lite* glyph = font12lite_get_glyph_by_index(index);
                width = glyph->width;
                glyph_data = glyph->row;
            } else {
                const Glyph9* glyph = font9_get_glyph_by_index(index);
                width = glyph->width;
                glyph_data = glyph->row;
            }
            // Рисуем символ (font9/font12lite используют u8 для строк)
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (glyph_data[row] & (1 << (7 - col))) {
                        graphics_draw_rect(cur_x + col, y + row, 1, 1, color);
                    }
                }
            }
        }

        cur_x += width + spacing;
        i += bytes_read;
    }
}

float graphics_measure_text(const char* text, int font_height) {
    if (!text) return 0.0f;
    float width = 0.0f;
    int i = 0;

    // Выбор шрифта по высоте
    int spacing;
    if (font_height == 23) {
        spacing = FONT23_SPACING;
    } else if (font_height == 12) {
        spacing = FONT12LITE_SPACING;
    } else {
        spacing = FONT9_SPACING;
    }

    while (text[i] != '\0') {
        int bytes_read;
        int index = utf8_decode_to_index(&text[i], &bytes_read);

        int glyph_width;
        if (font_height == 23) {
            const Glyph23* glyph = font23_get_glyph_by_index(index);
            glyph_width = glyph->width;
        } else if (font_height == 12) {
            const Glyph12lite* glyph = font12lite_get_glyph_by_index(index);
            glyph_width = glyph->width;
        } else {
            const Glyph9* glyph = font9_get_glyph_by_index(index);
            glyph_width = glyph->width;
        }

        width += glyph_width;

        // Добавить spacing только между символами (не после последнего)
        int next_i = i + bytes_read;
        if (text[next_i] != '\0') {
            width += spacing;
        }

        i = next_i;
    }
    return width;
}

void graphics_draw_number(float x, float y, int number, u32 color) {
    char buffer[32];
    snprintf(buffer, sizeof(buffer), "%d", number);

    int cur_x = (int)x;
    int cur_y = (int)y;

    for (int i = 0; buffer[i] != '\0'; i++) {
        if (buffer[i] >= '0' && buffer[i] <= '9') {
            int digit = buffer[i] - '0';
            const Glyph24* glyph = font24_get_digit(digit);

            for (int row = 0; row < FONT24_HEIGHT; row++) {
                for (int col = 0; col < glyph->width; col++) {
                    if (glyph->row[row] & (1 << (15 - col))) {
                        graphics_draw_rect(cur_x + col, cur_y + row, 1, 1, color);
                    }
                }
            }

            cur_x += glyph->width + FONT24_SPACING;
        } else if (buffer[i] == '-') {
            graphics_draw_rect(cur_x, cur_y + FONT24_HEIGHT / 2, 8, 2, color);
            cur_x += 10;
        }
    }
}

float graphics_measure_number(int number) {
    char buffer[32];
    snprintf(buffer, sizeof(buffer), "%d", number);

    float width = 0;

    for (int i = 0; buffer[i] != '\0'; i++) {
        if (buffer[i] >= '0' && buffer[i] <= '9') {
            int digit = buffer[i] - '0';
            const Glyph24* glyph = font24_get_digit(digit);
            width += glyph->width;

            if (buffer[i + 1] != '\0') {
                width += FONT24_SPACING;
            }
        } else if (buffer[i] == '-') {
            width += 10;
        }
    }

    return width;
}

// Единое управление состоянием текстур
void graphics_set_texturing(int enabled) {
    if (enabled && !s_texturing_enabled) {
        sceGuEnable(GU_TEXTURE_2D);
        s_texturing_enabled = 1;
    } else if (!enabled && s_texturing_enabled) {
        sceGuDisable(GU_TEXTURE_2D);
        s_texturing_enabled = 0;
    }
}

void graphics_begin_plain(void) {
    graphics_flush_batch(); // Завершить накопленные спрайты перед переключением в plain
    graphics_set_texturing(0);
    // Блендинг уже включён в graphics_init(); ничего менять не нужно
}

void graphics_begin_textured(void) {
    graphics_set_texturing(1);                 // включает GU_TEXTURE_2D при необходимости
    sceGuTexFunc(GU_TFX_REPLACE, GU_TCC_RGBA); // отключаем модуляцию цветом вершины
}

// Получить текущее состояние текстур (для оптимизации в level.c)
int graphics_get_texturing_state(void) {
    return s_texturing_enabled; // 0=plain, 1=textured
}

void graphics_end_frame(void) {
    graphics_flush_batch(); // Завершить все накопленные спрайты перед концом кадра
    sceGuFinish();
    sceGuSync(GU_SYNC_FINISH, GU_SYNC_WHAT_DONE);
    sceDisplayWaitVblankStart();
    sceGuSwapBuffers();
}

void graphics_shutdown(void) {
    graphics_flush_batch(); // Убедимся что все спрайты отрисованы перед завершением
    
    sceGuDisplay(GU_FALSE);
    sceGuTerm();
}

// =================== SPRITE BATCHING СИСТЕМА ===================

// Инициализация batch системы
static void batch_init(void) {
    s_batch.current_texture = NULL;
    s_batch.count = 0;
}

// Отправить накопленные спрайты на рендер
void graphics_flush_batch(void) {
    if (s_batch.count <= 0) return;
    
    graphics_begin_textured(); // на случай, если режим был plain
    if (s_batch.current_texture) {
        graphics_bind_texture(s_batch.current_texture);
        // НЕ дублируем TexScale/Offset — они уже выставляются внутри graphics_bind_texture()
    }
    
    // «Железобезопасная» альтернатива - копируем в GE память
    const int vcount = s_batch.count * 2;
    BatchVertex* vtx = (BatchVertex*)sceGuGetMemory(vcount * sizeof(BatchVertex));
    if (!vtx) { 
        s_batch.count = 0; 
        return; 
    }
    memcpy(vtx, s_batch.vertices, vcount * sizeof(BatchVertex));
    
    // Отрисовать всю пачку одним вызовом
    sceGuDrawArray(GU_SPRITES, 
                   GU_TEXTURE_32BITF|GU_VERTEX_32BITF|GU_TRANSFORM_2D, 
                   vcount, 0, vtx);
    
    s_batch.count = 0; // Очистить batch для новых спрайтов
}

// Привязать текстуру для batch'а (flush если текстура изменилась)
void graphics_bind_texture(texture_t* tex) {
    if (!tex) return;
    
    // Если текстура изменилась - flush накопленные спрайты
    if (s_batch.current_texture != tex) {
        graphics_flush_batch();
        
        s_batch.current_texture = tex;
        
        // Привязать новую текстуру (как в pspsdk/samples/gu/sprite)
        sceGuTexMode(tex->format, 0, 0, 0);
        // stride = width корректно: текстуры расширены до POT, данные непрерывны
        sceGuTexImage(0, tex->width, tex->height, tex->width, tex->data);
        sceGuTexFunc(GU_TFX_REPLACE, GU_TCC_RGBA);
        sceGuTexFilter(GU_NEAREST, GU_NEAREST);
        sceGuTexWrap(GU_CLAMP, GU_CLAMP);
        
        // Исправляем масштаб UV для реального железа PSP
        // Мы подаем UV в пикселях, нужно нормализовать
        sceGuTexScale(1.0f / tex->width, 1.0f / tex->height);
        sceGuTexOffset(0.0f, 0.0f);
    }
}

// Добавить спрайт в batch (не отрисовывает сразу!)
void graphics_batch_sprite(float u1, float v1, float u2, float v2, 
                          float x, float y, float w, float h) {
    // Flush если batch переполнен
    if (s_batch.count >= MAX_SPRITES_PER_BATCH) {
        graphics_flush_batch();
    }
    
    int idx = s_batch.count * 2; // 2 вершины на спрайт
    
    // Первая вершина (левый верхний угол)
    s_batch.vertices[idx].u = u1;
    s_batch.vertices[idx].v = v1;
    s_batch.vertices[idx].x = x;
    s_batch.vertices[idx].y = y;
    s_batch.vertices[idx].z = 0.0f;
    
    // Вторая вершина (правый нижний угол)
    s_batch.vertices[idx + 1].u = u2;
    s_batch.vertices[idx + 1].v = v2;
    s_batch.vertices[idx + 1].x = x + w;
    s_batch.vertices[idx + 1].y = y + h;
    s_batch.vertices[idx + 1].z = 0.0f;
    
    s_batch.count++;
}

// Bitmap иконки кнопки X (13x13 пикселей, диаметр 12)
// 0 = прозрачно, 1 = черный, 2 = белый X (диагональный крестик)
static const unsigned char button_x_bitmap[13][13] = {
    {0,0,0,0,1,1,1,1,1,0,0,0,0},
    {0,0,1,1,1,1,1,1,1,1,1,0,0},
    {0,1,1,2,1,1,1,1,1,2,1,1,0},
    {0,1,1,2,2,1,1,1,2,2,1,1,0},
    {1,1,1,1,2,2,1,2,2,1,1,1,1},
    {1,1,1,1,1,2,2,2,1,1,1,1,1},
    {1,1,1,1,1,1,2,1,1,1,1,1,1},
    {1,1,1,1,1,2,2,2,1,1,1,1,1},
    {1,1,1,1,2,2,1,2,2,1,1,1,1},
    {0,1,1,2,2,1,1,1,2,2,1,1,0},
    {0,1,1,2,1,1,1,1,1,2,1,1,0},
    {0,0,1,1,1,1,1,1,1,1,1,0,0},
    {0,0,0,0,1,1,1,1,1,0,0,0,0}
};

// Нарисовать иконку кнопки X (PSP) из bitmap (радиус игнорируется, фикс 13x13)
void graphics_draw_button_x(float cx, float cy, float radius) {
    (void)radius; // Не используется, размер фиксирован 13x13

    const u32 COLOR_BLACK = 0xFF000000;
    const u32 COLOR_WHITE = 0xFFFFFFFF;

    graphics_begin_plain();

    // Рисуем bitmap построчно (cx,cy = центр иконки)
    float start_x = cx - 6.0f;  // 13/2 = 6.5, округляем до 6
    float start_y = cy - 6.0f;

    for (int row = 0; row < 13; row++) {
        for (int col = 0; col < 13; col++) {
            unsigned char pixel = button_x_bitmap[row][col];
            if (pixel == 0) continue;  // Прозрачный

            u32 color = (pixel == 1) ? COLOR_BLACK : COLOR_WHITE;
            graphics_draw_rect(start_x + col, start_y + row, 1.0f, 1.0f, color);
        }
    }
}

