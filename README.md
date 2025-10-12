# Bounce Zero

Порт оригинальной игры **Bounce (2002, Java/Sun для Nokia 7210)** на платформу **PlayStation Portable (PSP)**.  
Игровая логика полностью переписана на **C**, при этом используются только оригинальные игровые ассеты из версии для телефонов Nokia.  
Проект создан в исследовательских целях, без модификации контента.

## Особенности
- Чтение и использование оригинальных игровых данных из JAR-версии Bounce 2002  
- Полная переимплементация игрового цикла и физики  
- Совместимость с реальным устройством PSP и эмулятором PPSSPP  
- Минимальные системные требования, отсутствие внешних зависимостей  

## Сборка
Необходим установленный [PSP SDK (pspdev)](https://github.com/pspdev/pspdev).

```bash
sudo apt-get update
sudo apt-get install build-essential cmake pkgconf libreadline8 libusb-0.1 libgpgme11 libarchive-tools fakeroot wget
wget https://github.com/pspdev/pspdev/releases/latest/download/pspdev-ubuntu-latest-x86_64.tar.gz
tar -xvf pspdev-ubuntu-latest-x86_64.tar.gz -C $HOME
export PSPDEV="$HOME/pspdev"
export PATH="$PATH:$PSPDEV/bin"
make
```

Собранный файл `EBOOT.PBP` появится в каталоге `release/`.

## Запуск
Скопируйте содержимое папки `release/` на карту памяти PSP:

```
/PSP/GAME/BounceZero/
```

или откройте `EBOOT.PBP` через эмулятор PPSSPP.

## Совместимость
- PlayStation Portable 6.00 и выше
- Эмулятор PPSSPP

## Лицензия
Исходный код распространяется под лицензией **MIT**.  
Все оригинальные материалы (*Bounce, 2002*) принадлежат **Nokia** и/или **Sun Microsystems** и используются исключительно в исследовательских целях.

## Автор
**Андрей Шмонин**  
[GitHub: amdray](https://github.com/amdray)
