#include "types.h"
#include <stdio.h>
#include <string.h>

// Файл сохранений в корне PSP
#define SAVE_FILE_PATH "bounce_save.dat"
#define SAVE_MAGIC 0x424F554E  // "BOUN" в hex

// Глобальные данные сохранений
static SaveData g_save_data = {0};
static bool g_save_initialized = false;

// Forward declaration
static void save_store_data(void);

void save_init(void) {
    if (g_save_initialized) return;
    
    // Попытка загрузить существующий файл
    FILE* file = fopen(SAVE_FILE_PATH, "rb");
    if (file) {
        size_t read = fread(&g_save_data, sizeof(SaveData), 1, file);
        fclose(file);
        
        // Проверка валидности файла
        if (read == 1 && g_save_data.magic == SAVE_MAGIC) {
            // Файл корректен - данные загружены
        } else {
            // Файл поврежден - сброс к значениям по умолчанию
            memset(&g_save_data, 0, sizeof(SaveData));
            g_save_data.magic = SAVE_MAGIC;
            g_save_data.best_level = 1;  // По умолчанию доступен уровень 1
        }
        g_save_initialized = true;
    } else {
        // Файл не существует - первый запуск
        memset(&g_save_data, 0, sizeof(SaveData));
        g_save_data.magic = SAVE_MAGIC;
        g_save_data.best_level = 1;  // По умолчанию доступен уровень 1

        // Сохранить начальные данные
        g_save_initialized = true; // Должно быть установлено до вызова save_store_data()
        save_store_data();
    }
}

void save_shutdown(void) {
    if (!g_save_initialized) return;
    
    // Сохранить данные при завершении
    save_store_data();
    g_save_initialized = false;
}

static void save_store_data(void) {
    if (!g_save_initialized) return;

    FILE* file = fopen(SAVE_FILE_PATH, "wb");
    if (file) {
        if (fwrite(&g_save_data, sizeof(SaveData), 1, file) != 1 ||
            fflush(file) != 0 ||
            fclose(file) != 0) {
            // Ошибка записи - данные могут быть потеряны
            // В будущем можно добавить логирование или уведомление пользователя
        }
    }
}

void save_update_records(int level, int score) {
    if (!g_save_initialized) return;
    
    int updated = 0;
    
    // Обновить максимальный уровень (как в оригинале BounceCanvas:179-180)
    if (level > g_save_data.best_level) {
        g_save_data.best_level = level;
        updated = 1;
    }
    
    // Обновить лучший счёт (как в оригинале BounceCanvas:184-185)  
    if (score > g_save_data.best_score) {
        g_save_data.best_score = score;
        updated = 1;
    }
    
    // Сохранить в файл если были изменения
    if (updated) {
        save_store_data();
    }
}

SaveData* save_get_data(void) {
    if (!g_save_initialized) {
        save_init();  // Автоматическая инициализация если забыли
    }
    return &g_save_data;
}