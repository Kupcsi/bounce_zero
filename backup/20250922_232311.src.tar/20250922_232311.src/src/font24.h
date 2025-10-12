#ifndef FONT24_H
#define FONT24_H

#include <psptypes.h>

#ifdef __cplusplus
extern "C" {
#endif

#define FONT24_HEIGHT 24
#define FONT24_FIRST 32   // Первый печатный ASCII символ (пробел)
#define FONT24_LAST  126  // Последний печатный ASCII символ (~)
#define FONT24_COUNT (FONT24_LAST - FONT24_FIRST + 1)
#define FONT24_SPACING 1  // Межсимвольный отступ при рендеринге

typedef struct {
    u8 row[FONT24_HEIGHT];  // 24 строки пиксельных данных
    u8 width;         // Ширина символа в пикселях
} Glyph24;

/**
 * Получить указатель на таблицу шрифта
 * @return Указатель на массив всех глифов
 */
const Glyph24* font24_table(void);

/**
 * Получить глиф для символа
 * @param c Код символа (ASCII 32-126)
 * @return Указатель на глиф. Для символов вне диапазона возвращает пробел
 */
const Glyph24* font24_get_glyph(uint8_t c);

#ifdef __cplusplus
}
#endif

#endif