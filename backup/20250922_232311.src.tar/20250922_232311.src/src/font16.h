#ifndef FONT16_H
#define FONT16_H

#include <psptypes.h>

#ifdef __cplusplus
extern "C" {
#endif

#define FONT16_HEIGHT 16
#define FONT16_FIRST 32   // Первый печатный ASCII символ (пробел)
#define FONT16_LAST  126  // Последний печатный ASCII символ (~)
#define FONT16_COUNT (FONT16_LAST - FONT16_FIRST + 1)
#define FONT16_SPACING 1  // Межсимвольный отступ при рендеринге

typedef struct {
    u8 row[FONT16_HEIGHT];  // 16 строк пиксельных данных
    u8 width;         // Ширина символа в пикселях
} Glyph16;

/**
 * Получить указатель на таблицу шрифта
 * @return Указатель на массив всех глифов
 */
const Glyph16* font16_table(void);

/**
 * Получить глиф для символа
 * @param c Код символа (ASCII 32-126)
 * @return Указатель на глиф. Для символов вне диапазона возвращает пробел
 */
const Glyph16* font16_get_glyph(uint8_t c);

#ifdef __cplusplus
}
#endif

#endif