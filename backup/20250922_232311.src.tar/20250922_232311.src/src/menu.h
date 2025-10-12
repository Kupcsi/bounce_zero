#ifndef MENU_H
#define MENU_H

#include "types.h"

// Константы расположения элементов меню  
#define SCREEN_CENTER_X     (SCREEN_WIDTH / 2)   // Центр экрана по X (240px)

// Главное меню
#define MENU_TITLE_Y        50        // Позиция заголовка "Bounce"
#define MENU_PADDING_X      5.0f      // Горизонтальный отступ выделения
#define MENU_PADDING_Y      5.0f      // Вертикальный отступ выделения
#define MENU_SELECTION_BG_HEIGHT 25   // Высота подложки выделения

// Экран выбора уровня
#define LEVEL_SELECT_TITLE_Y 30       // Позиция заголовка "SELECT LEVEL"
#define LEVEL_GRID_START_X   140      // Начальная X позиция сетки уровней
#define LEVEL_GRID_START_Y   80       // Начальная Y позиция сетки уровней  
#define LEVEL_CELL_WIDTH     40       // Ширина ячейки уровня
#define LEVEL_CELL_HEIGHT    30       // Высота ячейки уровня
#define LEVEL_SPACING_X      50       // Расстояние между ячейками по X
#define LEVEL_SPACING_Y      40       // Расстояние между ячейками по Y
#define LEVEL_HELP_Y         250      // Позиция текста помощи

// Инициализация меню (загрузка текстур и т.д.)
void menu_init(void);

// Обновление логики меню (обработка input)
void menu_update(void);

// Рендеринг меню
void menu_render(void);

// Обновление экрана выбора уровня
void level_select_update(void);

// Рендеринг экрана выбора уровня
void level_select_render(void);


// Обновление high score экрана
void high_score_update(void);

// Рендеринг high score экрана
void high_score_render(void);

// Обновление game over экрана
void game_over_update(void);

// Рендеринг game over экрана
void game_over_render(void);

// Обновление экрана инструкций
void instructions_update(void);

// Рендеринг экрана инструкций
void instructions_render(void);

// Очистка ресурсов меню
void menu_cleanup(void);

#endif