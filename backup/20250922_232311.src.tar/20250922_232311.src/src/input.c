#include "input.h"
#include <string.h>
#include <stdbool.h>

static SceCtrlData pad = {0};
static SceCtrlData old_pad = {0};

void input_init(void) {
    sceCtrlSetSamplingCycle(0);
    sceCtrlSetSamplingMode(PSP_CTRL_MODE_DIGITAL); // Только цифровые кнопки - Bounce не использует аналоговый стик по дизайну
}

void input_update(void) {
    old_pad = pad;
    
    // Обработка ошибок чтения контроллера для предотвращения залипания кнопок
    if (sceCtrlReadBufferPositive(&pad, 1) <= 0) {
        pad = old_pad; // сохранить предыдущее состояние при ошибке
    }
}

bool input_pressed(unsigned int button) {
    return (pad.Buttons & button) && !(old_pad.Buttons & button);
}

bool input_held(unsigned int button) {
    return (pad.Buttons & button);
}

bool input_released(unsigned int button) {
    return !(pad.Buttons & button) && (old_pad.Buttons & button);
}