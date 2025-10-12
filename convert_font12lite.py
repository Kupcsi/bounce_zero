#!/usr/bin/env python3
"""
Конвертер глифов из glyphs12lite.txt в font12lite.c
"""
import re

def bits_to_bytes(bit_lines, target_height):
    """Конвертирует строки битов в байты (u8)"""
    result = []

    # Паддинг до нужной высоты
    while len(bit_lines) < target_height:
        bit_lines.append('0' * len(bit_lines[0]) if bit_lines else '0')

    for line in bit_lines[:target_height]:
        # Паддинг строки до 8 бит
        padded_line = line[:8]  # Обрезаем до 8 бит
        while len(padded_line) < 8:
            padded_line += '0'

        # Конвертируем 8 бит в одно u8 значение
        val = 0
        for i in range(8):
            if padded_line[i] == '1':
                val |= (1 << (7 - i))
        result.append(val)

    return result

def extract_glyphs_from_file(filename):
    """Извлекает глифы из файла"""
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

            # Фильтруем только символы с ровно 12 строками (размер font12lite)
            if lines and len(lines) == 12:
                # Вычисляем ширину по сетке (максимальная длина строки)
                actual_width = 0
                for line in lines:
                    actual_width = max(actual_width, len(line))

                # Для пробела оставляем фиксированную ширину
                if ascii_code == 32:
                    actual_width = 4

                # Конвертируем биты в u8 для высоты 12
                byte_data = bits_to_bytes(lines, 12)

                glyphs.append({
                    'char': char,
                    'ascii': ascii_code,
                    'width': min(actual_width, 255) if actual_width > 0 else 1,
                    'data': byte_data
                })

    return glyphs

def main():
    print("Creating indexed glyph table (194 elements)...")

    # Создаем индексированную таблицу 194 элемента
    indexed_glyphs = [None] * 194

    # Создаем пустой глиф
    empty_data = [0] * 12
    empty_glyph = {'char': ' ', 'ascii': 32, 'width': 4, 'data': empty_data}

    print("Extracting glyphs from glyphs12lite.txt...")
    glyphs = extract_glyphs_from_file('glyphs12lite.txt')
    print(f"  Found {len(glyphs)} glyphs")

    # Заполняем индексированную таблицу
    print("Filling indexed table...")
    for glyph in glyphs:
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
    print("Generating font12lite.c...")
    content = '''#include "font12lite.h"

// Поле width в структуре Glyph12lite определяет логическую ширину символа для отступов при рендеринге
static const Glyph12lite FONT12LITE_DATA[FONT12LITE_COUNT] = {
'''

    for i, glyph in enumerate(indexed_glyphs):
        data_str = ', '.join(f'0x{b:02X}' for b in glyph['data'])
        if glyph == empty_glyph:
            content += f"    {{{{{data_str}}}, {glyph['width']}}}, // Empty slot {i}\n"
        else:
            content += f"    // '{glyph['char']}' ({glyph['ascii']})\n"
            content += f"    {{{{{data_str}}}, {glyph['width']}}},\n"

    content += '''};

const Glyph12lite* font12lite_table(void) {
    return FONT12LITE_DATA;
}

const Glyph12lite* font12lite_get_glyph_by_index(int index) {
    if (index < 0 || index >= FONT12LITE_COUNT) {
        return &FONT12LITE_DATA[32]; // Возвращаем пробел для неверного индекса
    }
    return &FONT12LITE_DATA[index];
}
'''

    with open('src/font12lite.c', 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"Generated font12lite.c with {non_empty} glyphs (height=12)")
    print(f"  Index 134 (Ё): '{indexed_glyphs[134]['char']}' (U+{indexed_glyphs[134]['ascii']:04X})")
    print(f"  Index 167 (ё): '{indexed_glyphs[167]['char']}' (U+{indexed_glyphs[167]['ascii']:04X})")

if __name__ == '__main__':
    main()
