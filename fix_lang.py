#!/usr/bin/env python3
# Скрипт для исправления "Поздрав- ления!" -> "Поздравления!" в UTF-16 LE файле

with open('lang/lang.ru-RU', 'rb') as f:
    data = f.read()

# UTF-16 LE представление "- " это: 2d 00 20 00
# Ищем и заменяем "Поздрав- ления!" на "Поздравления!"
data_fixed = data.replace(
    'Поздрав- ления!'.encode('utf-16-le'),
    'Поздравления!'.encode('utf-16-le')
)

# Сохраняем обратно
with open('lang/lang.ru-RU', 'wb') as f:
    f.write(data_fixed)

print("Fixed! Replaced 'Поздрав- ления!' with 'Поздравления!'")
