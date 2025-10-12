#include "menu.h"
#include "graphics.h"
#include "input.h"
#include "types.h"
#include "level.h"
#include "game.h"
#include "png.h"
#include "local.h"
#include <pspctrl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

// Нужен доступ к глобальному состоянию игры
extern Game g_game;

// Перечисление пунктов меню для безопасности типов
typedef enum {
    MENU_CONTINUE = 0,      // Продолжить игру
    MENU_NEW_GAME = 1,      // Новая игра
    MENU_SELECT_LEVEL = 2,  // Выбор уровня
    MENU_HIGH_SCORE = 3,    // Рекорды
    MENU_INSTRUCTIONS = 4,  // Инструкции
    MENU_ITEMS_COUNT        // Общее количество пунктов
} MenuItem;

// Статическая переменная для отслеживания текущей части инструкций (0-5)
static int current_instruction_page = 0;

// Функция для отображения текста с переносом по словам
// Отрисовка текста с автоматическим переносом строк по словам
// Разбивает длинный текст на строки, не превышающие max_width пикселей
static void draw_wrapped_text(float x, float y, const char* text, u32 color, float max_width, float line_height, int font_height) {
    if (!text) return;

    char temp_line[256];
    const char* current_pos = text;
    float current_y = y;

    while (*current_pos) {
        // Найти максимальное количество символов, которые помещаются в строку
        int chars_in_line = 0;
        int last_space = -1;

        temp_line[0] = '\0';

        while (current_pos[chars_in_line] && chars_in_line < 255) {
            temp_line[chars_in_line] = current_pos[chars_in_line];
            temp_line[chars_in_line + 1] = '\0';

            // Проверяем ширину строки
            float width = graphics_measure_text(temp_line, font_height);
            if (width > max_width) {
                break;
            }

            // Запоминаем позицию последнего пробела
            if (current_pos[chars_in_line] == ' ') {
                last_space = chars_in_line;
            }

            chars_in_line++;
        }

        // Если строка не помещается, обрезаем по последнему пробелу
        if (current_pos[chars_in_line] && last_space > 0) {
            chars_in_line = last_space;
            temp_line[chars_in_line] = '\0';
        }

        // Рисуем строку
        graphics_draw_text(x, current_y, temp_line, color, font_height);

        // Переходим к следующей строке
        current_pos += chars_in_line;
        if (*current_pos == ' ') current_pos++; // пропускаем пробел
        current_y += line_height;
    }
}

void menu_init(void) {
    // Инициализация меню (пока пустая)
}

void menu_update(void) {
    // Убедиться что стартовая позиция не на недоступном Continue
    if(g_game.menu_selection == 0 && !game_can_continue()) {
        g_game.menu_selection = 1; // Начать с New Game
    }
    
    // Навигация по меню (5 пунктов: 0,1,2,3,4)
    if(input_pressed(PSP_CTRL_UP) && g_game.menu_selection > 0) {
        g_game.menu_selection--;
        // Если Continue недоступен и мы попали на него - перейти на New Game
        if(g_game.menu_selection == 0 && !game_can_continue()) {
            g_game.menu_selection = 1;
        }
    }
    if(input_pressed(PSP_CTRL_DOWN) && g_game.menu_selection < MENU_ITEMS_COUNT-1) {
        g_game.menu_selection++;
    }
    
    // Выбор пункта меню
    if(input_pressed(PSP_CTRL_CROSS)) {
        if(g_game.menu_selection == 0) {
            // 0 = CONTINUE - продолжить сохраненную игру
            if(game_can_continue()) {
                // Просто вернуться в игру - состояние уже сохранено
                g_game.state = STATE_GAME;
            }
        } else if(g_game.menu_selection == 1) {
            // 1 = NEW GAME - новая игра с уровня 1
            g_game.state = STATE_GAME;
            g_game.selected_level = 1;
            if (level_load_by_number(1)) {
                game_reset_camera();
            }

            // Устанавливаем респавн в стартовую позицию (как в game_init)
            level_set_respawn(g_level.startTileX, g_level.startTileY);

            // Сброс всех счётчиков для новой игры
            g_game.numRings = 0;
            g_game.score = 0;
            g_game.numLives = 3;
            game_exit_reset();
            g_game.saved_game_state = SAVED_GAME_IN_PROGRESS; // Теперь можно Continue
            g_game.new_best_score = false; // Сброс флага (как в BounceUI.java:184)
            
            player_init(&g_game.player, g_level.startPosX, g_level.startPosY, 
                       g_level.ballSize == BALL_SIZE_SMALL ? SMALL_SIZE_STATE : LARGE_SIZE_STATE);
        } else if(g_game.menu_selection == 2) {
            // 2 = SELECT LEVEL
            g_game.state = STATE_LEVEL_SELECT;
        } else if(g_game.menu_selection == 3) {
            // 3 = HIGH SCORE - экран рекордов
            g_game.state = STATE_HIGH_SCORE;
        } else if(g_game.menu_selection == 4) {
            // 4 = INSTRUCTIONS - экран правил/инструкций
            g_game.state = STATE_INSTRUCTIONS;
        }
    }
}

void instructions_update(void) {
    // Навигация между частями
    if(input_pressed(PSP_CTRL_LEFT) && current_instruction_page > 0) {
        current_instruction_page--;
    }
    if(input_pressed(PSP_CTRL_RIGHT) && current_instruction_page < INSTRUCTIONS_TOTAL_PAGES - 1) {
        current_instruction_page++;
    }

    // Возврат в меню по любой кнопке
    if(input_pressed(PSP_CTRL_CROSS) || input_pressed(PSP_CTRL_CIRCLE) || input_pressed(PSP_CTRL_START)) {
        current_instruction_page = 0; // Сброс при выходе
        g_game.state = STATE_MENU;
    }
}

void menu_render(void) {

    // UI без текстурной модуляции
    graphics_begin_plain();
const float center_x = (float)(SCREEN_WIDTH / 2);
    const float title_y  = MAIN_MENU_TITLE_Y;
    const int font_height = 23;
    const int   bg_height = MAIN_MENU_SELECTION_BG_HEIGHT;

    // Заголовок (по центру)
    {
        const char* title = local_get_text(QTJ_BOUN_GAME_NAME); // ID 10, как в оригинале BounceUI.java:65
        float w = graphics_measure_text(title, font_height);
        float x = center_x - w * 0.5f;
        graphics_draw_text(x, title_y, title, COLOR_SELECTION_BG, font_height);
    }
    
    // Данные пунктов меню (5 пунктов с равномерным расстоянием 25px)
    struct Item { const char* label; float y; u32 color_unselected; };
    struct Item items[5] = {
        { local_get_text(QTJ_BOUN_CONTINUE),      105.0f,        COLOR_TEXT_NORMAL },      // +20px смещение вниз
        { local_get_text(QTJ_BOUN_NEW_GAME),      130.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_text_select_level(),          155.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_text(QTJ_BOUN_HIGH_SCORES),   180.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_text(QTJ_BOUN_INSTRUCTIONS),  205.0f,        COLOR_TEXT_NORMAL },      // +25px
    };
    
    for (int i = 0; i < 5; ++i) {
        const int selected = (g_game.menu_selection == i);
        
        // Continue доступен только при наличии сохраненной игры
        const int is_continue_unavailable = (i == MENU_CONTINUE && !game_can_continue());
        
        u32 color;
        if (is_continue_unavailable) {
            color = COLOR_DISABLED; // Серый цвет для недоступного пункта
        } else {
            color = selected ? COLOR_TEXT_SELECTED : items[i].color_unselected;
        }
        float w = graphics_measure_text(items[i].label, font_height);
        float x = center_x - w * 0.5f;
        float y = items[i].y;

        // Фон под выбранным пунктом — строго по ширине текста с одинаковыми полями
        if (selected) {
            const float padding_x = MAIN_MENU_PADDING_X;
            const float padding_y = MAIN_MENU_PADDING_Y;
            graphics_draw_rect(x - padding_x, y - padding_y, w + 2*padding_x, bg_height, COLOR_SELECTION_BG);
        }

        // Текст пункта
        graphics_draw_text(x, y, items[i].label, color, font_height);
    }

    // Будущие иконки меню
    // graphics_begin_textured();
    // TODO: render menu icons here
}

void instructions_render(void) {
    graphics_begin_plain();

    // Фон для модального диалога инструкций
    graphics_draw_rect(0.0f, 0.0f, (float)SCREEN_WIDTH, (float)SCREEN_HEIGHT, BACKGROUND_COLOUR);

    // Белая панель 240x128 по центру экрана (как в Game Over)
    float panel_x = (SCREEN_WIDTH - 240) / 2.0f;  // 120
    float panel_y = (SCREEN_HEIGHT - 128) / 2.0f;  // 72
    graphics_draw_rect(panel_x, panel_y, 240.0f, 128.0f, COLOR_WHITE_ABGR);

    float center_x = (float)SCREEN_WIDTH / 2.0f;
    float text_y = panel_y + 5.0f;  // Отступ от верха панели для шапки

    // Шапка "Инструкции" (шрифт 23, getText(13), BounceUI.java:107)
    const char* title = local_get_text(QTJ_BOUN_INSTRUCTIONS);
    float w = graphics_measure_text(title, 23);
    graphics_draw_text(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 23);
    text_y += 23 + 10.0f;  // После шапки + отступ

    // Текст инструкций внутри панели
    float max_width = 240.0f - 40.0f; // Ширина панели минус отступы по 20px с каждой стороны
    float line_height = 14.0f; // Уменьшенная высота строки для помещения в панель

    if (current_instruction_page < INSTRUCTIONS_TOTAL_PAGES) {
        const char* text = local_get_text(QHJ_BOUN_INSTRUCTIONS_PART_1 + current_instruction_page);
        draw_wrapped_text(panel_x + 20.0f, text_y, text, COLOR_TEXT_NORMAL, max_width, line_height, 12);
    }

    // Индикатор страницы под белой панелью, по центру
    text_y = panel_y + 128.0f + 10.0f;  // 10px под панелью
    char page_info[64];
    snprintf(page_info, sizeof(page_info), "Часть %d из %d", current_instruction_page + 1, INSTRUCTIONS_TOTAL_PAGES);
    float page_w = graphics_measure_text(page_info, 9);
    graphics_draw_text(center_x - page_w * 0.5f, text_y, page_info, COLOR_TEXT_NORMAL, 9);
}

void level_select_update(void) {
    // Навигация по уровням (сетка 5x3: 1-5, 6-10, 11)
    if(input_pressed(PSP_CTRL_LEFT) && g_game.selected_level > 1) {
        g_game.selected_level--;
    }
    if(input_pressed(PSP_CTRL_RIGHT) && g_game.selected_level < MAX_LEVEL) {
        g_game.selected_level++;
    }
    if(input_pressed(PSP_CTRL_UP)) {
        if(g_game.selected_level > 5) {
            g_game.selected_level -= 5;
        }
    }
    if(input_pressed(PSP_CTRL_DOWN)) {
        if(g_game.selected_level <= 5) {
            g_game.selected_level += 5;
        } else if(g_game.selected_level <= 10 && g_game.selected_level != MAX_LEVEL) {
            g_game.selected_level = MAX_LEVEL;
        }
    }
    
    // Ограничиваем диапазон 1-11
    if(g_game.selected_level < 1) g_game.selected_level = 1;
    if(g_game.selected_level > MAX_LEVEL) g_game.selected_level = MAX_LEVEL;
    
    // Выбор уровня
    if(input_pressed(PSP_CTRL_CROSS)) {
        g_game.state = STATE_GAME;
        if (level_load_by_number(g_game.selected_level)) {
            game_reset_camera();
        }

        // Устанавливаем респавн в стартовую позицию (как в game_init)
        level_set_respawn(g_level.startTileX, g_level.startTileY);

        // Сброс счётчиков при старте уровня (как в Java BounceCanvas.startLevel)
        g_game.numRings = 0;
        g_game.score = 0;
        g_game.numLives = 3;        // Сбрасываем жизни при старте уровня
        game_exit_reset();          // Сбрасываем анимацию двери
        
        player_init(&g_game.player, g_level.startPosX, g_level.startPosY, 
                   g_level.ballSize == BALL_SIZE_SMALL ? SMALL_SIZE_STATE : LARGE_SIZE_STATE);
    }
    
    // Возврат в меню
    if(input_pressed(PSP_CTRL_CIRCLE)) {
        g_game.state = STATE_MENU;
    }
}

void level_select_render(void) {
    const float center_x = (float)(SCREEN_WIDTH / 2);
    
    // UI цифры из битмапа — без текстурной модуляции
    graphics_begin_plain();
// Заголовок
    {
        const char* title = local_text_select_level();
        float w = graphics_measure_text(title, 23);
        float x = center_x - w * 0.5f;
        graphics_draw_text(x, LEVEL_SELECT_TITLE_Y, title, COLOR_TEXT_NORMAL, 23);
    }
    
    // Размеры для сетки уровней
    int start_x = LEVEL_GRID_START_X;
    int start_y = LEVEL_GRID_START_Y;
    int cell_width = LEVEL_CELL_WIDTH;
    int cell_height = LEVEL_CELL_HEIGHT;
    int spacing_x = LEVEL_SPACING_X;
    int spacing_y = LEVEL_SPACING_Y;
    
    // Рисуем уровни 1-10 в сетке 5x2
    for(int row = 0; row < 2; row++) {
        for(int col = 0; col < 5; col++) {
            int level = row * 5 + col + 1; // 1-10
            int x = start_x + col * spacing_x;
            int y = start_y + row * spacing_y;
            
            // Цветной фон для выбранного уровня (красный как в "Bounce")
            if(level == g_game.selected_level) {
                graphics_draw_rect((float)(x - 5), (float)(y - 5), (float)cell_width, (float)cell_height, COLOR_SELECTION_BG);
            }
            
            // Номер уровня
            char level_text[4];
            snprintf(level_text, sizeof(level_text), "%d", level);
            
            u32 color = (level == g_game.selected_level) ? COLOR_TEXT_SELECTED : COLOR_TEXT_NORMAL;
            graphics_draw_text((float)x, (float)y, level_text, color, 23);
        }
    }
    
    // Уровень 11 отдельно по центру
    int x11 = start_x + 2 * spacing_x; // центр
    int y11 = start_y + 2 * spacing_y;
    
    if(g_game.selected_level == MAX_LEVEL) {
        graphics_draw_rect((float)(x11 - 5), (float)(y11 - 5), (float)cell_width, (float)cell_height, COLOR_SELECTION_BG);
    }
    
    u32 color11 = (g_game.selected_level == MAX_LEVEL) ? COLOR_TEXT_SELECTED : COLOR_TEXT_NORMAL;
    char level11_text[4];
    snprintf(level11_text, sizeof(level11_text), "%d", MAX_LEVEL);
    graphics_draw_text((float)x11, (float)y11, level11_text, color11, 23);
    
}

void high_score_update(void) {
    // Возврат в меню (как в оригинале mBackCmd)
    if(input_pressed(PSP_CTRL_CROSS) || 
       input_pressed(PSP_CTRL_CIRCLE) || 
       input_pressed(PSP_CTRL_START)) {
        g_game.state = STATE_MENU;
    }
}

void high_score_render(void) {
    SaveData* save = save_get_data();

    // UI без текстурной модуляции
    graphics_begin_plain();

    // Фон для модального диалога рекордов
    graphics_draw_rect(0.0f, 0.0f, (float)SCREEN_WIDTH, (float)SCREEN_HEIGHT, BACKGROUND_COLOUR);

    // Белая панель 240x128 по центру экрана
    float panel_x = (SCREEN_WIDTH - 240) / 2.0f;  // 120
    float panel_y = (SCREEN_HEIGHT - 128) / 2.0f;  // 72
    graphics_draw_rect(panel_x, panel_y, 240.0f, 128.0f, COLOR_WHITE_ABGR);

    float center_x = (float)SCREEN_WIDTH / 2.0f;
    float text_y = panel_y + 5.0f;  // Отступ от верха панели для шапки

    // Шапка "HIGH SCORES" (шрифт 23, как в Java Form header)
    {
        const char* title = local_get_text(QTJ_BOUN_HIGH_SCORES);
        float w = graphics_measure_text(title, 23);
        graphics_draw_text(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 23);
    }
    text_y += 23 + 10.0f;  // После шапки + отступ

    // Пустая строка для симметричности с level_complete (там "Level X completed!")
    text_y += 9 + 20.0f;  // Высота шрифта 9 + отступ 20px

    // Фиксированная позиция для счёта (одинаковая во всех окнах)
    text_y = panel_y + 60.0f;

    // Лучший счёт (красный, шрифт 24)
    {
        float w = graphics_measure_number(save->best_score);
        graphics_draw_number(center_x - w * 0.5f, text_y, save->best_score, COLOR_SELECTION_BG);
    }

    // Кнопка "X - Continue/Return" - фиксированная позиция внизу панели
    text_y = panel_y + 128.0f - 25.0f;  // 25px от низа панели
    {
        const char* continue_text = local_get_text(QTJ_BOUN_BACK);
        float w = graphics_measure_text(continue_text, 9);

        // Полная ширина блока: иконка (12px диаметр) + отступ (4px) + текст
        float total_width = 12.0f + 4.0f + w;
        float start_x = center_x - total_width * 0.5f;

        // Иконка кнопки X (радиус 6px)
        graphics_draw_button_x(start_x + 6.0f, text_y + 4.0f, 6.0f);

        // Текст кнопки
        graphics_draw_text(start_x + 12.0f + 4.0f, text_y, continue_text, COLOR_TEXT_NORMAL, 9);
    }
}

void game_over_update(void) {
    // Возврат в меню только по X (Continue) как в оригинале
    if(input_pressed(PSP_CTRL_CROSS)) {
        g_game.state = STATE_MENU;
    }
}

void game_over_render(void) {
    // UI без текстурной модуляции
    graphics_begin_plain();
    
    // Фон для модального диалога Game Over
    graphics_draw_rect(0.0f, 0.0f, (float)SCREEN_WIDTH, (float)SCREEN_HEIGHT, BACKGROUND_COLOUR);
    
    // Белая панель 240x128 по центру экрана
    float panel_x = (SCREEN_WIDTH - 240) / 2.0f;  // 120
    float panel_y = (SCREEN_HEIGHT - 128) / 2.0f;  // 72
    graphics_draw_rect(panel_x, panel_y, 240.0f, 128.0f, COLOR_WHITE_ABGR);

    float center_x = (float)SCREEN_WIDTH / 2.0f;  // 240
    float text_y = panel_y + 5.0f;  // Отступ от верха панели для шапки

    // Шапка "GAME OVER" (шрифт 23, Local.getText(11), BounceUI.java:127-128)
    {
        const char* title = local_get_text(QTJ_BOUN_GAME_OVER);
        float w = graphics_measure_text(title, 23);
        graphics_draw_text(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 23);
    }
    text_y += 23 + 10.0f;  // После шапки + отступ

    // Сообщение о новом рекорде (BounceUI.java:131-133) - для симметричности с level_complete
    if (g_game.new_best_score) {
        const char* new_record = local_get_text(QTJ_BOUN_NEW_HIGH_SCORE);
        float w = graphics_measure_text(new_record, 9);
        graphics_draw_text(center_x - w * 0.5f, text_y, new_record, COLOR_SELECTION_BG, 9);
    }
    text_y += 9 + 20.0f;  // Пропуск для симметричности (высота строки + отступ)

    // Фиксированная позиция для счёта (одинаковая во всех окнах)
    text_y = panel_y + 60.0f;

    // Только число счёта, без "Score:" - красный, шрифт 24 (BounceUI.java:136)
    {
        float w = graphics_measure_number(g_game.score);
        graphics_draw_number(center_x - w * 0.5f, text_y, g_game.score, COLOR_SELECTION_BG);
    }

    // Кнопка "X - OK" - фиксированная позиция внизу панели (Local.getText(19), BounceUI.java:125)
    text_y = panel_y + 128.0f - 25.0f;  // 25px от низа панели
    {
        const char* ok_text = local_get_text(QTJ_BOUN_OK);
        float w = graphics_measure_text(ok_text, 9);

        // Полная ширина блока: иконка (12px диаметр) + отступ (4px) + текст
        float total_width = 12.0f + 4.0f + w;
        float start_x = center_x - total_width * 0.5f;

        // Иконка кнопки X (радиус 6px)
        graphics_draw_button_x(start_x + 6.0f, text_y + 4.0f, 6.0f);

        // Текст кнопки
        graphics_draw_text(start_x + 12.0f + 4.0f, text_y, ok_text, COLOR_TEXT_NORMAL, 9);
    }
}

// =============================================================================
// LEVEL COMPLETE ЭКРАН
// =============================================================================

void level_complete_update(void) {
    // X = продолжить (следующий уровень или Game Over)
    if(input_pressed(PSP_CTRL_CROSS)) {
        // Проверяем если не последний уровень (до 11) - переходим на следующий
        if(g_game.selected_level < MAX_LEVEL) {
            g_game.selected_level++;
            g_game.state = STATE_GAME;
            if (level_load_by_number(g_game.selected_level)) {
                game_reset_camera();
            }

            // Устанавливаем респавн в стартовую позицию
            level_set_respawn(g_level.startTileX, g_level.startTileY);

            // Сброс счётчиков при старте следующего уровня
            g_game.numRings = 0;
            game_exit_reset();          // Сбрасываем анимацию двери

            player_init(&g_game.player, g_level.startPosX, g_level.startPosY,
                       g_level.ballSize == BALL_SIZE_SMALL ? SMALL_SIZE_STATE : LARGE_SIZE_STATE);
        } else {
            // Последний уровень пройден - Game Over экран (как в оригинале BounceCanvas:539)
            g_game.state = STATE_GAME_OVER;
        }
    }
    // O = выйти в меню
    if(input_pressed(PSP_CTRL_CIRCLE)) {
        g_game.state = STATE_MENU;
    }
}

void level_complete_render(void) {
    // UI без текстурной модуляции
    graphics_begin_plain();

    // Фон для модального диалога завершения уровня
    graphics_draw_rect(0.0f, 0.0f, (float)SCREEN_WIDTH, (float)SCREEN_HEIGHT, BACKGROUND_COLOUR);

    // Белая панель 240x128 по центру экрана
    float panel_x = (SCREEN_WIDTH - 240) / 2.0f;  // 120
    float panel_y = (SCREEN_HEIGHT - 128) / 2.0f;  // 72
    graphics_draw_rect(panel_x, panel_y, 240.0f, 128.0f, COLOR_WHITE_ABGR);

    float center_x = (float)SCREEN_WIDTH / 2.0f;  // 240
    float text_y = panel_y + 5.0f;  // Отступ от верха панели для шапки

    // Шапка "Поздравления!" (шрифт 23)
    {
        const char* title = local_get_text(QTJ_BOUN_CONGRATULATIONS);
        float w = graphics_measure_text(title, 23);
        graphics_draw_text(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 23);
    }
    text_y += 23 + 10.0f;  // После шапки + отступ

    // "Level X completed!" (Local.getText(15) с параметром, BounceUI.java:150)
    {
        char level_str[16];
        snprintf(level_str, sizeof(level_str), "%d", g_game.selected_level);
        const char* params[] = { level_str };
        const char* title = local_get_text_with_params(QTJ_BOUN_LEVEL_COMPLETED, params, 1);
        float w = graphics_measure_text(title, 9);
        graphics_draw_text(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 9);
    }

    // Счёт - красный, шрифт 24, фиксированная позиция (как в high_score) (BounceUI.java:152)
    text_y = panel_y + 60.0f;  // Фиксированная позиция (одинаковая в high_score и level_complete)
    {
        float w = graphics_measure_number(g_game.score);
        graphics_draw_number(center_x - w * 0.5f, text_y, g_game.score, COLOR_SELECTION_BG);
    }

    // Кнопка "X - Continue" - фиксированная позиция внизу панели (Local.getText(8), BounceUI.java:147)
    text_y = panel_y + 128.0f - 25.0f;  // 25px от низа панели (как в Game Over)
    {
        const char* continue_text = local_get_text(QTJ_BOUN_CONTINUE);
        float w = graphics_measure_text(continue_text, 9);

        // Полная ширина блока: иконка (12px диаметр) + отступ (4px) + текст
        float total_width = 12.0f + 4.0f + w;
        float start_x = center_x - total_width * 0.5f;

        // Иконка кнопки X (радиус 6px)
        graphics_draw_button_x(start_x + 6.0f, text_y + 4.0f, 6.0f);

        // Текст кнопки
        graphics_draw_text(start_x + 12.0f + 4.0f, text_y, continue_text, COLOR_TEXT_NORMAL, 9);
    }
}

void menu_cleanup(void) {
    // Очистка ресурсов меню (пока пустая)
}

// =============================================================================
// НОВЫЙ УНИФИЦИРОВАННЫЙ API
// =============================================================================

// Универсальная функция обновления меню
void menu_update_by_type(menu_type_t type) {
    switch (type) {
        case MENU_TYPE_MAIN:
            menu_update();
            break;
        case MENU_TYPE_LEVEL_SELECT:
            level_select_update();
            break;
        case MENU_TYPE_HIGH_SCORE:
            high_score_update();
            break;
        case MENU_TYPE_GAME_OVER:
            game_over_update();
            break;
        case MENU_TYPE_LEVEL_COMPLETE:
            level_complete_update();
            break;
        case MENU_TYPE_INSTRUCTIONS:
            instructions_update();
            break;
    }
}

// Универсальная функция рендеринга меню
void menu_render_by_type(menu_type_t type) {
    switch (type) {
        case MENU_TYPE_MAIN:
            menu_render();
            break;
        case MENU_TYPE_LEVEL_SELECT:
            level_select_render();
            break;
        case MENU_TYPE_HIGH_SCORE:
            high_score_render();
            break;
        case MENU_TYPE_GAME_OVER:
            game_over_render();
            break;
        case MENU_TYPE_LEVEL_COMPLETE:
            level_complete_render();
            break;
        case MENU_TYPE_INSTRUCTIONS:
            instructions_render();
            break;
    }
}

// Хелпер: преобразование game state в menu type
menu_type_t menu_get_type_from_game_state(int game_state) {
    switch (game_state) {
        case STATE_MENU:
            return MENU_TYPE_MAIN;
        case STATE_LEVEL_SELECT:
            return MENU_TYPE_LEVEL_SELECT;
        case STATE_HIGH_SCORE:
            return MENU_TYPE_HIGH_SCORE;
        case STATE_GAME_OVER:
            return MENU_TYPE_GAME_OVER;
        case STATE_LEVEL_COMPLETE:
            return MENU_TYPE_LEVEL_COMPLETE;
        case STATE_INSTRUCTIONS:
            return MENU_TYPE_INSTRUCTIONS;
        default:
            return MENU_TYPE_MAIN; // Fallback
    }
}