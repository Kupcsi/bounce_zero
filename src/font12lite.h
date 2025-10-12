#ifndef FONT12LITE_H
#define FONT12LITE_H

#include <psptypes.h>

#ifdef __cplusplus
extern "C" {
#endif

#define FONT12LITE_HEIGHT 12
#define FONT12LITE_COUNT 194  // Таблица: 0-127 ASCII + 128-160 А-Я + 161-193 а-я
#define FONT12LITE_SPACING 0  // Межсимвольный отступ (0 = промежутки вшиты в ширину символов)

// Индексы для прямого доступа к символам
#define FONT12LITE_ASCII_START 0
#define FONT12LITE_CYRILLIC_UPPER_START 128  // А-Я
#define FONT12LITE_CYRILLIC_LOWER_START 161  // а-я

typedef struct {
    u8 row[FONT12LITE_HEIGHT];  // 12 строк пиксельных данных
    u8 width;         // Ширина символа в пикселях
} Glyph12lite;

/**
 * Получить указатель на таблицу шрифта
 * @return Указатель на массив всех глифов
 */
const Glyph12lite* font12lite_table(void);

/**
 * Получить глиф по индексу в таблице (прямой доступ O(1))
 * @param index Индекс в таблице 0-193
 * @return Указатель на глиф. Для неверного индекса возвращает пробел
 */
const Glyph12lite* font12lite_get_glyph_by_index(int index);



#ifdef __cplusplus
}
#endif

#endif
