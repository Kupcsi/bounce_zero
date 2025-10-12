#!/usr/bin/env python3
"""
Конвертер глифов из glyphs.txt (только цифры) в форматы font24.c
"""
import re
import os

def bits_to_u16_array(bit_lines, target_height):
    """Конвертирует строки битов в u16 значения (одно значение на строку)"""
    result = []

    # Паддинг до нужной высоты
    while len(bit_lines) < target_height:
        bit_lines.append('0' * len(bit_lines[0]) if bit_lines else '0')

    for line in bit_lines[:target_height]:
        # Паддинг строки до 16 бит (максимальная ширина для u16)
        padded_line = line[:16]  # Обрезаем до 16 бит
        while len(padded_line) < 16:
            padded_line += '0'

        # Конвертируем 16 бит в одно u16 значение
        val = 0
        for i in range(16):
            if padded_line[i] == '1':
                val |= (1 << (15 - i))
        result.append(val)

    return result

def extract_glyphs_from_file(filename):
    """Извлекает глифы из указанного файла"""
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()

    glyphs = []

    # Найдем все блоки глифов по комментариям
    pattern = r'// \'([^\']*)\''
    matches = list(re.finditer(pattern, content))

    for match in matches:
        char_str = match.group(1)

        # Обрабатываем только цифры
        if len(char_str) == 1 and char_str.isdigit():
            char = char_str
            ascii_code = ord(char)
        else:
            continue

        # Найдем блок данных после комментария
        start = match.end()
        next_match = None
        for next_m in matches:
            if next_m.start() > start:
                next_match = next_m
                break

        end = next_match.start() if next_match else len(content)
        block = content[start:end]

        # Ищем секцию [BITS]
        bits_match = re.search(r'\[BITS\](.*?)(?=\[|$|----)', block, re.DOTALL)
        if bits_match:
            lines = [line.strip() for line in bits_match.group(1).strip().split('\n')
                    if line.strip() and not line.startswith('-')]

            # Фильтруем только символы с ровно 24 строками (размер font24)
            if lines and len(lines) == 24:
                # Вычисляем ширину по сетке (максимальная длина строки)
                actual_width = 0
                for line in lines:
                    actual_width = max(actual_width, len(line))

                # Конвертируем биты в u16 для высоты 24
                u16_data = bits_to_u16_array(lines, 24)

                glyphs.append({
                    'char': char,
                    'ascii': ascii_code,
                    'width': min(actual_width, 255) if actual_width > 0 else 1,
                    'data': u16_data
                })

    return glyphs

def main():
    print("Creating digit-only font table (10 elements for digits 0-9)...")

    # Создаем таблицу для цифр 0-9
    digit_glyphs = [None] * 10

    # Создаем пустой глиф
    empty_data = [0] * 24
    empty_glyph = {'char': ' ', 'ascii': 32, 'width': 3, 'data': empty_data}

    print("Extracting digit glyphs from glyphs.txt...")
    glyphs = extract_glyphs_from_file('glyphs.txt')
    print(f"  Found {len(glyphs)} digit glyphs")

    # Заполняем таблицу цифр
    print("Filling digit table...")
    for glyph in glyphs:
        char = glyph['char']
        if char.isdigit():
            index = int(char)  # '0' -> 0, '1' -> 1, и т.д.
            if digit_glyphs[index] is not None:
                print(f"  WARNING: Overwriting digit {index}")
            digit_glyphs[index] = glyph

    # Проверяем что все цифры на месте
    for i in range(10):
        if digit_glyphs[i] is None:
            print(f"  ERROR: Missing digit {i}")
            digit_glyphs[i] = empty_glyph

    # Генерируем C код
    print("Generating font24.c...")
    content = '''#include "font24.h"

// Шрифт только для цифр 0-9, высота 24 пикселя
static const Glyph24 FONT24_DATA[10] = {
'''

    for i, glyph in enumerate(digit_glyphs):
        data_str = ', '.join(f'0x{b:04X}' for b in glyph['data'])
        content += f"    // '{glyph['char']}' ({glyph['ascii']})\n"
        content += f"    {{{{{data_str}}}, {glyph['width']}}},\n"

    content += '''};

const Glyph24* font24_get_digit(int digit) {
    if (digit < 0 || digit > 9) {
        return &FONT24_DATA[0]; // Возвращаем '0' для неверного индекса
    }
    return &FONT24_DATA[digit];
}
'''

    with open('src/font24.c', 'w', encoding='utf-8') as f:
        f.write(content)

    # Генерируем заголовочный файл
    print("Generating font24.h...")
    header = '''#ifndef FONT24_H
#define FONT24_H

#include <pspge.h>
#include <psptypes.h>

#define FONT24_HEIGHT 24
#define FONT24_SPACING 0

typedef struct {
    u16 row[FONT24_HEIGHT];  // 24 строки пиксельных данных (до 16 пикселей ширины)
    u8 width;
} Glyph24;

// Получить глиф для цифры (0-9)
const Glyph24* font24_get_digit(int digit);

#endif // FONT24_H
'''

    with open('src/font24.h', 'w', encoding='utf-8') as f:
        f.write(header)

    print(f"Generated font24.c and font24.h with 10 digit glyphs (height=24)")

if __name__ == '__main__':
    main()
