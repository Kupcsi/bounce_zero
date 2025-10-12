#ifndef FONT23_H
#define FONT23_H

#include <psptypes.h>

#ifdef __cplusplus
extern "C" {
#endif

#define FONT23_HEIGHT 23
#define FONT23_FIRST 32   // Первый печатный ASCII символ (пробел)
#define FONT23_LAST  126  // Последний печатный ASCII символ (~)
#define FONT23_COUNT (FONT23_LAST - FONT23_FIRST + 1)
#define FONT23_SPACING 1  // Межсимвольный отступ при рендеринге

typedef struct {
    u8 row[FONT23_HEIGHT];  // 23 строки пиксельных данных
    u8 width;         // Ширина символа в пикселях
} Glyph23;

/**
 * Получить указатель на таблицу шрифта
 * @return Указатель на массив всех глифов
 */
const Glyph23* font23_table(void);

/**
 * Получить глиф для символа
 * @param c Код символа (ASCII 32-126)
 * @return Указатель на глиф. Для символов вне диапазона возвращает пробел
 */
const Glyph23* font23_get_glyph(uint8_t c);

#ifdef __cplusplus
}
#endif

#endif