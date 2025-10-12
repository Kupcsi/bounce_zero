# Bounce Zero

Порт оригинальной игры **Bounce (2002, Java/Sun для Nokia 7210)** на платформу **PlayStation Portable (PSP)**.  
Игровая логика полностью переписана на **C**, при этом используются только оригинальные игровые ассеты из версии для телефонов Nokia.  
Проект создан в исследовательских целях, без модификации контента.


## Скриншоты
Первый экран
![BOUN01179_00000](docs/screenshots/BOUN01179_00000.jpg)
Главное меню/меню паузы
![BOUN01179_00001](docs/screenshots/BOUN01179_00001.jpg)
Первый уровень с HUD 
![BOUN01179_00002](docs/screenshots/BOUN01179_00002.jpg)
Выбор уровней 
![BOUN01179_00003](docs/screenshots/BOUN01179_00003.jpg)
Третий уровень
![BOUN01179_00004](docs/screenshots/BOUN01179_00004.jpg)



## Особенности
- Чтение и использование оригинальных игровых данных из JAR-версии Bounce 2002, а так же использован оригинальный шрифт Nokia, вытащенный из прошивки телефона Nokia 7210, для аутентичности.
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

## Об эксперименте

Весь исходный код написан с использованием модели **Claude AI**  
в рамках эксперимента по **vibe-coding** — формированию игрового движка  
на основе описаний поведения и логики, без ручного программирования.  
Проект создан исключительно в исследовательских целях.
