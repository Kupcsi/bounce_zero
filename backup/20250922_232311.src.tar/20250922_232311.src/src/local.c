#include "local.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pspkernel.h>
#include <psputility.h>

static char g_phone_lang[8] = "xx"; // Язык PSP (по умолчанию английский)


// Определение языка PSP в соответствии с файлами
static void detect_phone_language(void) {
    int language;
    if (sceUtilityGetSystemParamInt(PSP_SYSTEMPARAM_ID_INT_LANGUAGE, &language) != 0) {
        strcpy(g_phone_lang, "xx"); // Английский по умолчанию
        return;
    }

    switch (language) {
        case PSP_SYSTEMPARAM_LANGUAGE_RUSSIAN:
            strcpy(g_phone_lang, "ru-RU");
            break;
        case PSP_SYSTEMPARAM_LANGUAGE_GERMAN:
            strcpy(g_phone_lang, "de");
            break;
        case PSP_SYSTEMPARAM_LANGUAGE_ENGLISH:
        default:
            strcpy(g_phone_lang, "xx");
            break;
    }
}

// Основная функция получения текста (аналог Java getText)
const char* local_get_text(int string_id) {
    static char error_msg[] = "NoLang";
    static char err_msg[] = "Err";

    // Определяем язык если еще не определили
    if (strcmp(g_phone_lang, "xx") == 0) {
        detect_phone_language();
    }

    // Формируем имя файла
    char filename[64];
    snprintf(filename, sizeof(filename), "lang/lang.%s", g_phone_lang);

    // Пробуем открыть файл для нужного языка
    FILE* file = fopen(filename, "rb");
    if (!file) {
        // Если не найден, пробуем английский
        file = fopen("lang/lang.xx", "rb");
        if (!file) {
            return error_msg;
        }
    }

    // Получаем размер файла
    fseek(file, 0, SEEK_END);
    long file_size = ftell(file);
    fseek(file, 0, SEEK_SET);

    // Читаем весь файл
    unsigned char* buffer = malloc(file_size);
    if (!buffer) {
        fclose(file);
        return err_msg;
    }

    fread(buffer, 1, file_size, file);
    fclose(file);

    // Реализуем алгоритм Java версии:
    // 1. Пропускаем string_id * 2 байт
    size_t offset = string_id * 2;

    if (offset + 2 > (size_t)file_size) {
        free(buffer);
        return err_msg;
    }

    // 2. Читаем short (offset к строке)
    unsigned short string_offset = (buffer[offset] << 8) | buffer[offset + 1];

    // 3. Пропускаем до позиции строки
    offset = string_offset;

    // 4. Читаем длину строки (2 байта, big-endian)
    if (offset + 2 > (size_t)file_size) {
        free(buffer);
        return err_msg;
    }

    unsigned short length = (buffer[offset] << 8) | buffer[offset + 1];
    offset += 2;

    if (offset + length > (size_t)file_size) {
        free(buffer);
        return err_msg;
    }

    // 5. Читаем строку (Java Modified UTF-8 format)
    char* result = NULL;
    if (length > 0) {
        // В Java DataInputStream.readUTF() строки уже в правильной UTF-8
        // Просто копируем байты как есть
        result = malloc(length + 1);
        if (result) {
            memcpy(result, buffer + offset, length);
            result[length] = '\0';

        }
    }

    free(buffer);

    if (!result) {
        return err_msg;
    }

    // TODO: реализовать замену %U параметров если нужно
    // Пока возвращаем как есть

    if (result) {
        if (strlen(result) == 0) {
            free(result);
            return "";
        }
        return result; // TODO: утечка памяти! Нужен кэш строк
    }

    return err_msg;
}

// Дополнительные функции для строк, отсутствующих в lang файлах
const char* local_get_select_level_text(void) {
    if (strncmp(g_phone_lang, "ru", 2) == 0) {
        return "Выбор уровня";
    }
    return "Select level";
}

const char* local_get_settings_text(void) {
    if (strncmp(g_phone_lang, "ru", 2) == 0) {
        return "Настройки";
    }
    return "Settings";
}


