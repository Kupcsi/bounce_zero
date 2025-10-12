#include <pspkernel.h>
#include <pspdisplay.h>
#include <stdio.h>
#include <string.h>
#include "graphics.h"
#include "input.h"
#include "game.h"
#include "sound.h"
#include "types.h"

PSP_MODULE_INFO("2D Platformer", 0, 1, 0);
PSP_MAIN_THREAD_ATTR(PSP_THREAD_ATTR_USER);

// Макрос для подавления предупреждений о неиспользуемых параметрах
#define UNUSED(x) ((void)(x))

// Делитель частоты обновления физики (60 FPS -> 30 FPS)
// Инпут буферизуется промежуточно, эффекты применяются на каждом втором кадре
#define PHYSICS_TICK_DIVIDER 2


// Параметры потока колбэков (из образца PSPSDK)
#define CALLBACK_PRIO 0x11
#define CALLBACK_STACK 0xFA0

/**
 * Open file with fallback paths
 * Tries different path prefixes to find the file
 */
FILE* util_open_file(const char* path, const char* mode) {
    if (!path || !mode) return NULL;

    const char* prefixes[] = { "", "ms0:/", "./" };
    char full_path[512];

    for (int i = 0; i < 3; i++) {
        snprintf(full_path, sizeof(full_path), "%s%s", prefixes[i], path);
        FILE* file = fopen(full_path, mode);
        if (file) return file;
    }

    // Additional fallback: try with/without leading slash
    if (path[0] == '/') {
        FILE* file = fopen(path + 1, mode);
        if (file) return file;
    } else {
        snprintf(full_path, sizeof(full_path), "/%s", path);
        FILE* file = fopen(full_path, mode);
        if (file) return file;
    }

    return NULL;
}

/**
 * Callback для выхода из приложения при нажатии HOME
 */
int main_exit_callback(int arg1, int arg2, void *common) {
    UNUSED(arg1); UNUSED(arg2); UNUSED(common);
    g_game.state = STATE_EXIT;  // Элегантное завершение через игровой цикл
    return 0;
}

/**
 * Поток для обработки системных callback'ов PSP
 */
int main_callback_thread(SceSize args, void *argp) {
    UNUSED(args); UNUSED(argp);
    int cbid = sceKernelCreateCallback("Exit Callback", main_exit_callback, NULL);
    sceKernelRegisterExitCallback(cbid);
    sceKernelSleepThreadCB();
    return 0;
}

int main_setup_callbacks(void) {
    int thid = sceKernelCreateThread("callback_thread", main_callback_thread, CALLBACK_PRIO, CALLBACK_STACK, 0, 0);
    if(thid >= 0)
        sceKernelStartThread(thid, 0, 0);
    return thid;
}

int main(void) {
    int callback_thid = main_setup_callbacks();
    
    graphics_init();
    input_init();
    sound_init();
    save_init();      // Загрузить сохранённые рекорды
    game_init();
    
    // Счётчик для замедления физики до оригинальной частоты
    int physics_tick_counter = 0;
    // Универсальные буферы для input'ов между тиками физики  
    
    while(g_game.state != STATE_EXIT) {
        input_update();
        
        // === ОБНОВЛЕНИЕ МЕНЮ И ИНТЕРФЕЙСА (каждый кадр) ===
        if(g_game.state == STATE_SPLASH_NOKIA || g_game.state == STATE_SPLASH || g_game.state == STATE_MENU || g_game.state == STATE_LEVEL_SELECT || g_game.state == STATE_HIGH_SCORE || g_game.state == STATE_INSTRUCTIONS || g_game.state == STATE_GAME_OVER || g_game.state == STATE_LEVEL_COMPLETE) {
            game_update();  // Все UI состояния обновляются на полной частоте 60 FPS для отзывчивости
        }
        
        // === ОБНОВЛЕНИЕ ИГРОВОЙ ФИЗИКИ (замедленно) ===
        else if(g_game.state == STATE_GAME) {
            // ВАЖНО: Кнопки меню обрабатываются на полной частоте 60 FPS
            // (не в game_update который вызывается через PHYSICS_TICK_DIVIDER = 30 FPS)  
            // чтобы избежать пропусков нажатий
            if(input_pressed(PSP_CTRL_START)) {
                // Сохраняем состояние игры для Continue
                g_game.saved_game_state = SAVED_GAME_IN_PROGRESS;
                g_game.state = STATE_MENU;
            }

            // Чистые буферы input'ов - main.c не знает о игровой логике
            if(input_pressed(PSP_CTRL_CROSS)) {
                g_game.buffered_jump = 1;
            }
            if(input_released(PSP_CTRL_CROSS)) {
                g_game.buffered_jump_released = 1;
            }
            
            // Обновляем физику только каждый N-й кадр для оптимизации
            physics_tick_counter++;
            if(physics_tick_counter >= PHYSICS_TICK_DIVIDER) {
                game_update();

                // Сбрасываем все буферы после обработки
                g_game.buffered_jump = 0;
                g_game.buffered_jump_released = 0;
                physics_tick_counter = 0;
            }
        }
        
        // Рендеринг всегда на полной частоте для плавности
        graphics_start_frame();
        game_render();
        graphics_end_frame();
        
    }
    
    game_shutdown();
    save_shutdown();   // Сохранить рекорды перед выходом
    sound_shutdown();
    graphics_shutdown();
    
    // Явно завершить callback thread перед выходом (PSPSDK best practices)
    if (callback_thid >= 0) {
        sceKernelTerminateDeleteThread(callback_thid);
    }
    
    sceKernelExitGame();
    return 0;
}