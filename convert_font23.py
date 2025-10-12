#!/usr/bin/env python3
"""
Конвертер глифов из glyphs23.txt в форматы font23.c
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

        # Обрабатываем символы
        if char_str == 'пробел':
            char = ' '
            ascii_code = 32
        elif char_str.startswith('0x'):
            ascii_code = int(char_str, 16)
            char = chr(ascii_code)
        elif len(char_str) == 1:
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

            # Фильтруем только символы с ровно 23 строками (размер font23)
            if lines and len(lines) == 23:
                # Вычисляем ширину по сетке (максимальная длина строки)
                actual_width = 0
                for line in lines:
                    actual_width = max(actual_width, len(line))

                # Для пробела оставляем фиксированную ширину
                if ascii_code == 32:
                    actual_width = 8

                # Конвертируем биты в u16 для высоты 23
                u16_data = bits_to_u16_array(lines, 23)

                glyphs.append({
                    'char': char,
                    'ascii': ascii_code,
                    'width': min(actual_width, 255) if actual_width > 0 else 1,
                    'data': u16_data
                })

    return glyphs

def main():
    print("Creating indexed glyph table (194 elements)...")

    # Создаем индексированную таблицу 194 элемента
    indexed_glyphs = [None] * 194

    # Создаем пустой глиф
    empty_data = [0] * 23
    empty_glyph = {'char': ' ', 'ascii': 32, 'width': 3, 'data': empty_data}

    print("Extracting glyphs from glyphs23.txt (Cyrillic)...")
    glyphs_cyrillic = extract_glyphs_from_file('glyphs23.txt')
    print(f"  Found {len(glyphs_cyrillic)} glyphs")

    print("Extracting glyphs from glyphs.txt (Latin + other)...")
    glyphs_latin = extract_glyphs_from_file('glyphs.txt')
    print(f"  Found {len(glyphs_latin)} glyphs")

    # Объединяем все глифы
    all_glyphs = glyphs_cyrillic + glyphs_latin

    # Заполняем индексированную таблицу
    print("Filling indexed table...")
    for glyph in all_glyphs:
        ascii_code = glyph['ascii']
        index = None

        if ascii_code <= 127:
            # ASCII символы в индексы 0-127
            index = ascii_code
        elif ascii_code == 1025:
            # Ё в индекс 134
            index = 134
        elif ascii_code == 1105:
            # ё в индекс 167
            index = 167
        elif 1040 <= ascii_code <= 1045:
            # А-Е (1040-1045) -> 128-133
            index = 128 + (ascii_code - 1040)
        elif 1046 <= ascii_code <= 1071:
            # Ж-Я (1046-1071) -> 135-160
            index = 135 + (ascii_code - 1046)
        elif 1072 <= ascii_code <= 1077:
            # а-е (1072-1077) -> 161-166
            index = 161 + (ascii_code - 1072)
        elif 1078 <= ascii_code <= 1103:
            # ж-я (1078-1103) -> 168-193
            index = 168 + (ascii_code - 1078)

        if index is not None:
            if indexed_glyphs[index] is not None:
                print(f"  WARNING: Overwriting index {index} ('{indexed_glyphs[index]['char']}' U+{indexed_glyphs[index]['ascii']:04X}) with '{glyph['char']}' (U+{ascii_code:04X})")
            indexed_glyphs[index] = glyph

    # Заполняем пустые слоты
    for i in range(194):
        if indexed_glyphs[i] is None:
            indexed_glyphs[i] = empty_glyph

    # Подсчитываем непустые глифы
    non_empty = sum(1 for g in indexed_glyphs if g != empty_glyph)

    # Генерируем C код
    print("Generating font23.c...")
    content = '''#include "font23.h"

// Поле width в структуре Glyph23 определяет логическую ширину символа для отступов при рендеринге,
// а не фактическую ширину в пикселях.
static const Glyph23 FONT23_DATA[FONT23_COUNT] = {
'''

    for i, glyph in enumerate(indexed_glyphs):
        data_str = ', '.join(f'0x{b:04X}' for b in glyph['data'])
        if glyph == empty_glyph:
            content += f"    {{{{{data_str}}}, {glyph['width']}}}, // Empty slot {i}\n"
        else:
            content += f"    // '{glyph['char']}' ({glyph['ascii']})\n"
            content += f"    {{{{{data_str}}}, {glyph['width']}}},\n"

    content += '''};

const Glyph23* font23_table(void) {
    return FONT23_DATA;
}

const Glyph23* font23_get_glyph_by_index(int index) {
    if (index < 0 || index >= FONT23_COUNT) {
        return &FONT23_DATA[32]; // Возвращаем пробел для неверного индекса
    }
    return &FONT23_DATA[index];
}
'''

    with open('src/font23.c', 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"Generated font23.c with {non_empty} glyphs (height=23)")
    print(f"  Index 134 (Ё): '{indexed_glyphs[134]['char']}' (U+{indexed_glyphs[134]['ascii']:04X})")
    print(f"  Index 167 (ё): '{indexed_glyphs[167]['char']}' (U+{indexed_glyphs[167]['ascii']:04X})")

if __name__ == '__main__':
    main()
