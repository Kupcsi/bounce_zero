#!/usr/bin/env python3
"""
Конвертер глифов из glyphs16.txt в форматы font16.c
"""
import re
import os

def bits_to_bytes(bit_lines, target_height):
    """Конвертирует строки битов в байты с паддингом до нужной высоты"""
    bytes_data = []

    # Паддинг до нужной высоты
    while len(bit_lines) < target_height:
        bit_lines.append('0' * len(bit_lines[0]) if bit_lines else '0')

    for line in bit_lines[:target_height]:
        # Паддинг строки до кратности 8
        padded_line = line
        while len(padded_line) % 8 != 0:
            padded_line += '0'

        # Конвертируем каждые 8 бит в байты (по 8 бит на байт)
        for i in range(0, len(padded_line), 8):
            byte_val = 0
            for j in range(8):
                if i + j < len(padded_line) and padded_line[i + j] == '1':
                    byte_val |= (1 << (7 - j))
            bytes_data.append(byte_val)

    return bytes_data

def extract_glyphs_font16():
    """Извлекает глифы из glyphs16.txt для font16"""
    with open('glyphs16.txt', 'r', encoding='utf-8') as f:
        content = f.read()

    glyphs = []

    # Найдем все блоки глифов по комментариям - поддерживаем разные форматы
    pattern = r'// \'([^\']*)\''
    matches = list(re.finditer(pattern, content))

    for match in matches:
        char_str = match.group(1)

        # Обрабатываем символы с плюсом (масштаб)
        if char_str.endswith('+'):
            char = char_str[:-1]  # убираем плюс
            if char == 'пробел':
                char = ' '
                ascii_code = 32
            elif char.startswith('0x'):
                # Hex кодировка
                ascii_code = int(char, 16)
                char = chr(ascii_code)
            elif len(char) == 1:
                # Обычный символ
                ascii_code = ord(char)
            else:
                # Неизвестный формат, пропускаем
                continue
        elif char_str == 'пробел':
            char = ' '
            ascii_code = 32
        elif char_str.startswith('0x'):
            # Hex кодировка
            ascii_code = int(char_str, 16)
            char = chr(ascii_code)
        elif len(char_str) == 1:
            # Обычный символ
            char = char_str
            ascii_code = ord(char)
        else:
            # Неизвестный формат, пропускаем
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

            if lines:
                # Вычисляем правильную ширину без пустых пикселей справа
                actual_width = 0
                for line in lines:
                    # Находим последний '1' в строке
                    last_one = line.rfind('1')
                    if last_one >= 0:
                        actual_width = max(actual_width, last_one + 1)

                # Для пробела оставляем фиксированную ширину
                if ascii_code == 32:  # пробел
                    actual_width = 8

                # Конвертируем биты в байты для высоты 16
                byte_data = bits_to_bytes(lines, 16)

                glyphs.append({
                    'char': char,
                    'ascii': ascii_code,
                    'width': min(actual_width, 255) if actual_width > 0 else 1,
                    'data': byte_data
                })

    return glyphs

def generate_font_file(size_type, font_data, filename):
    """Генерирует .c файл для шрифта"""
    height = font_data['height']
    glyphs = font_data['glyphs']

    # Создаем индексированную таблицу 194 элемента
    indexed_glyphs = [None] * 194

    # Заполняем таблицу глифами
    for glyph in glyphs:
        ascii_code = glyph['ascii']
        if ascii_code <= 127:
            # ASCII символы в индексы 0-127
            indexed_glyphs[ascii_code] = glyph
        elif 1040 <= ascii_code <= 1071:
            # А-Я в индексы 128-159
            indexed_glyphs[128 + (ascii_code - 1040)] = glyph
        elif 1072 <= ascii_code <= 1103:
            # а-я в индексы 160-191
            indexed_glyphs[160 + (ascii_code - 1072)] = glyph

    # Создаем пустой глиф для неиспользуемых слотов
    empty_data = [0] * height
    empty_glyph = {'char': ' ', 'ascii': 32, 'width': 3, 'data': empty_data}

    # Заполняем пустые слоты
    for i in range(194):
        if indexed_glyphs[i] is None:
            indexed_glyphs[i] = empty_glyph

    # Генерируем C код
    header = filename.replace('.c', '.h')
    struct_name = f"Glyph{height}"
    font_name = f"FONT{height}"

    content = f'''#include "{header}"

// Поле width в структуре {struct_name} определяет логическую ширину символа для отступов при рендеринге,
// а не фактическую ширину в пикселях.
static const {struct_name} {font_name}_DATA[{font_name}_COUNT] = {{
'''

    for i, glyph in enumerate(indexed_glyphs):
        if glyph == empty_glyph:
            data_str = ', '.join(f'0x{b:02X}' for b in glyph['data'])
            content += f"    {{{{{data_str}}}, {glyph['width']}}}, // Empty slot {i},\n"
        else:
            data_str = ', '.join(f'0x{b:02X}' for b in glyph['data'])
            content += f"    // '{glyph['char']}' ({glyph['ascii']})\n"
            content += f"    {{{{{data_str}}}, {glyph['width']}}},\n"

    content += f'''}};

const {struct_name}* font{height}_table(void) {{
    return {font_name}_DATA;
}}

const {struct_name}* font{height}_get_glyph_by_index(int index) {{
    if (index < 0 || index >= {font_name}_COUNT) {{
        return &{font_name}_DATA[32]; // Возвращаем пробел для неверного индекса
    }}
    return &{font_name}_DATA[index];
}}
'''

    with open(f'src/{filename}', 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"Generated {filename} with {len([g for g in indexed_glyphs if g != empty_glyph])} glyphs (height={height})")

def main():
    print("Extracting glyphs from glyphs16.txt...")
    glyphs = extract_glyphs_font16()

    if glyphs:
        font_data = {'height': 16, 'glyphs': glyphs}
        generate_font_file('', font_data, 'font16.c')
    else:
        print("No glyphs found in glyphs16.txt")

if __name__ == '__main__':
    main()