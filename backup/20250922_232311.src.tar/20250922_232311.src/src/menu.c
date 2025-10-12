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
    MENU_SETTINGS = 4,      // Настройки
    MENU_INSTRUCTIONS = 5,  // Инструкции
    MENU_ITEMS_COUNT        // Общее количество пунктов
} MenuItem;

// Статическая переменная для отслеживания текущей части инструкций (0-5)
static int current_instruction_page = 0;

// Функция для отображения текста с переносом по словам
static void draw_wrapped_text(float x, float y, const char* text, u32 color, float max_width, float line_height) {
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
            float width = graphics_measure_text(temp_line, 1);
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
        graphics_draw_text(x, current_y, temp_line, color);

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
            
            // Сброс всех счётчиков для новой игры
            g_game.numRings = 0;
            g_game.score = 0;
            g_game.numLives = 3;
            game_reset_exit();
            g_game.saved_game_state = SAVED_GAME_IN_PROGRESS; // Теперь можно Continue
            
            player_init(&g_game.player, g_level.startPosX, g_level.startPosY, 
                       g_level.ballSize == BALL_SIZE_SMALL ? SMALL_SIZE_STATE : LARGE_SIZE_STATE);
        } else if(g_game.menu_selection == 2) {
            // 2 = SELECT LEVEL
            g_game.state = STATE_LEVEL_SELECT;
        } else if(g_game.menu_selection == 3) {
            // 3 = HIGH SCORE - экран рекордов
            g_game.state = STATE_HIGH_SCORE;
        } else if(g_game.menu_selection == 4) {
            // 4 = SETTINGS - заглушка пока
            // TODO: реализовать экран настроек
        } else if(g_game.menu_selection == 5) {
            // 5 = INSTRUCTIONS - экран правил/инструкций
            g_game.state = STATE_INSTRUCTIONS;
        }
    }
}

void instructions_update(void) {
    // Навигация между частями (0-5)
    if(input_pressed(PSP_CTRL_LEFT) && current_instruction_page > 0) {
        current_instruction_page--;
    }
    if(input_pressed(PSP_CTRL_RIGHT) && current_instruction_page < 5) {
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
const float center_x = (float)SCREEN_CENTER_X;
    const float title_y  = MENU_TITLE_Y;
    const int scale    = 2;
    const int   bg_height = MENU_SELECTION_BG_HEIGHT;
    
    // Заголовок (по центру)
    {
        const char* title = local_get_text(QTJ_BOUN_GAME_NAME); // ID 10, как в оригинале BounceUI.java:65
        float w = graphics_measure_text(title, scale);
        float x = center_x - w * 0.5f;
        graphics_draw_text_scaled(x, title_y, title, COLOR_SELECTION_BG, scale);
    }
    
    // Данные пунктов меню (6 пунктов с равномерным расстоянием 25px)
    struct Item { const char* label; float y; u32 color_unselected; };
    struct Item items[6] = {
        { local_get_text(QTJ_BOUN_CONTINUE),      85.0f,         COLOR_TEXT_NORMAL },      // Сдвинули вверх на 25px
        { local_get_text(QTJ_BOUN_NEW_GAME),      110.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_select_level_text(),          135.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_text(QTJ_BOUN_HIGH_SCORES),   160.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_settings_text(),              185.0f,        COLOR_TEXT_NORMAL },      // +25px
        { local_get_text(QTJ_BOUN_INSTRUCTIONS),  210.0f,        COLOR_TEXT_NORMAL },      // +25px
    };
    
    for (int i = 0; i < 6; ++i) {
        const int selected = (g_game.menu_selection == i);
        
        // Continue доступен только при наличии сохраненной игры
        const int is_continue_unavailable = (i == MENU_CONTINUE && !game_can_continue());
        
        u32 color;
        if (is_continue_unavailable) {
            color = COLOR_DISABLED; // Серый цвет для недоступного пункта
        } else {
            color = selected ? COLOR_TEXT_SELECTED : items[i].color_unselected;
        }
        float w = graphics_measure_text(items[i].label, scale);
        float x = center_x - w * 0.5f;
        float y = items[i].y;
        
        // Фон под выбранным пунктом — строго по ширине текста с одинаковыми полями
        if (selected) {
            const float padding_x = MENU_PADDING_X;
            const float padding_y = MENU_PADDING_Y;
            graphics_draw_rect(x - padding_x, y - padding_y, w + 2*padding_x, bg_height, COLOR_SELECTION_BG);
        }
        
        // Текст пункта
        graphics_draw_text_scaled(x, y, items[i].label, color, scale);
    }

    // Будущие иконки меню
    // graphics_begin_textured();
    // TODO: render menu icons here
}

void instructions_render(void) {
    graphics_begin_plain();

    graphics_draw_text(20.0f, 220.0f, local_get_text(QTJ_BOUN_BACK), COLOR_TEXT_NORMAL);

    // Заголовок "Инструкции" по центру
    const char* title = local_get_text(QTJ_BOUN_INSTRUCTIONS);
    float w = graphics_measure_text(title, 1);
    float x = SCREEN_CENTER_X - w * 0.5f;
    graphics_draw_text(x, 20.0f, title, COLOR_TEXT_NORMAL);

    // Отображаем только 1 строку за раз с переносом
    float y_pos = 50.0f;
    float max_width = SCREEN_WIDTH - 40.0f; // 20 пикселей отступа с каждой стороны
    float line_height = 18.0f; // Высота строки

    // Отображаем одну строку по индексу current_instruction_page
    if (current_instruction_page < 6) {
        const char* text = local_get_text(QHJ_BOUN_INSTRUCTIONS_PART_1 + current_instruction_page);
        draw_wrapped_text(20.0f, y_pos, text, COLOR_TEXT_NORMAL, max_width, line_height);
        free((void*)text);
    }

    // Индикатор страницы
    char page_info[64];
    snprintf(page_info, sizeof(page_info), "Часть %d из 6", current_instruction_page + 1);
    graphics_draw_text(20.0f, 200.0f, page_info, COLOR_TEXT_NORMAL);

    // Подсказка навигации
    graphics_draw_text(20.0f, 230.0f, "← → - листать части", COLOR_TEXT_NORMAL);

    // Тест кириллицы внизу экрана
    const char* cyrillic_test = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    graphics_draw_text(20.0f, 250.0f, cyrillic_test, COLOR_TEXT_NORMAL);
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
        
        // Сброс счётчиков при старте уровня (как в Java BounceCanvas.startLevel)
        g_game.numRings = 0;
        g_game.score = 0;
        g_game.numLives = 3;        // Сбрасываем жизни при старте уровня
        game_reset_exit();          // Сбрасываем анимацию двери
        
        player_init(&g_game.player, g_level.startPosX, g_level.startPosY, 
                   g_level.ballSize == BALL_SIZE_SMALL ? SMALL_SIZE_STATE : LARGE_SIZE_STATE);
    }
    
    // Возврат в меню
    if(input_pressed(PSP_CTRL_CIRCLE)) {
        g_game.state = STATE_MENU;
    }
}

void level_select_render(void) {
    const float center_x = (float)SCREEN_CENTER_X;
    
    // UI цифры из битмапа — без текстурной модуляции
    graphics_begin_plain();
// Заголовок
    {
        const char* title = local_get_select_level_text();
        float w = graphics_measure_text(title, 2);
        float x = center_x - w * 0.5f;
        graphics_draw_text_scaled(x, LEVEL_SELECT_TITLE_Y, title, COLOR_TEXT_NORMAL, 2);
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
            graphics_draw_text_scaled((float)x, (float)y, level_text, color, 2);
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
    graphics_draw_text_scaled((float)x11, (float)y11, level11_text, color11, 2);
    
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
    const float center_x = (float)SCREEN_CENTER_X;
    
    // UI без текстурной модуляции
    graphics_begin_plain();
    
    // Заголовок "HIGH SCORES" (как в оригинале Local.getText(12))
    {
        const char* title = "HIGH SCORES";
        float w = graphics_measure_text(title, 2);
        float x = center_x - w * 0.5f;
        graphics_draw_text_scaled(x, MENU_TITLE_Y, title, COLOR_SELECTION_BG, 2);
    }
    
    // Лучший счёт по центру (как в оригинале - просто число)
    {
        char score_text[16];
        snprintf(score_text, sizeof(score_text), "%d", save->best_score);
        float w = graphics_measure_text(score_text, 2);
        graphics_draw_text_scaled(center_x - w * 0.5f, 150.0f, score_text, COLOR_TEXT_NORMAL, 2);
    }
    
    // Инструкция возврата (как Back command)
    const char* help_text = "X/O/Start - Return";
    float help_width = graphics_measure_text(help_text, 1);
    graphics_draw_text(center_x - help_width * 0.5f, 220.0f, help_text, COLOR_TEXT_HELP);
}

void game_over_update(void) {
    // Возврат в меню только по X (Continue) как в оригинале
    if(input_pressed(PSP_CTRL_CROSS)) {
        g_game.state = STATE_MENU;
    }
}

void game_over_render(void) {
    SaveData* save = save_get_data();
    
    // UI без текстурной модуляции
    graphics_begin_plain();
    
    // Фон
    graphics_draw_rect(0.0f, 0.0f, (float)SCREEN_WIDTH, (float)SCREEN_HEIGHT, BACKGROUND_COLOUR);
    
    // Белая панель 240x128 по центру экрана
    float panel_x = (SCREEN_WIDTH - 240) / 2.0f;  // 120
    float panel_y = (SCREEN_HEIGHT - 128) / 2.0f;  // 72
    graphics_draw_rect(panel_x, panel_y, 240.0f, 128.0f, COLOR_WHITE_ABGR);
    
    float center_x = (float)SCREEN_WIDTH / 2.0f;  // 240
    float text_y = panel_y + 15.0f;   // Отступ от верха панели
    
    // Заголовок "GAME OVER" (как в оригинале Local.getText(11))
    {
        const char* title = "GAME OVER";
        float w = graphics_measure_text(title, 2);
        graphics_draw_text_scaled(center_x - w * 0.5f, text_y, title, COLOR_TEXT_NORMAL, 2);
        text_y += 25.0f;
    }
    
    // Текущий счёт
    {
        char score_text[32];
        snprintf(score_text, sizeof(score_text), "Score: %d", g_game.score);
        float w = graphics_measure_text(score_text, 1);
        graphics_draw_text_scaled(center_x - w * 0.5f, text_y, score_text, COLOR_TEXT_NORMAL, 1);
        text_y += 20.0f;
    }
    
    // Сообщение о новом рекорде (как в оригинале mNewBestScore)
    if (g_game.score > save->best_score) {
        const char* new_record = "NEW HIGH SCORE!";
        float w = graphics_measure_text(new_record, 1);
        graphics_draw_text_scaled(center_x - w * 0.5f, text_y, new_record, COLOR_TEXT_HIGHLIGHT, 1);
        text_y += 18.0f;
    }
    
    // Лучший счёт
    {
        char best_text[32];
        snprintf(best_text, sizeof(best_text), "Best: %d", save->best_score);
        float w = graphics_measure_text(best_text, 1);
        graphics_draw_text_scaled(center_x - w * 0.5f, text_y, best_text, COLOR_TEXT_NORMAL, 1);
        text_y += 20.0f;
    }
    
    // Кнопка Continue (убираем "Press any key")
    {
        const char* continue_text = "X - Continue";
        float w = graphics_measure_text(continue_text, 1);
        graphics_draw_text_scaled(center_x - w * 0.5f, text_y, continue_text, COLOR_TEXT_NORMAL, 1);
    }
}

void menu_cleanup(void) {
    // Очистка ресурсов меню (пока пустая)
}