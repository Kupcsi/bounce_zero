#include "input.h"
#include <string.h>
#include <stdbool.h>

static SceCtrlData s_current_pad = {0};
static SceCtrlData s_prev_pad = {0};

void input_init(void) {
    sceCtrlSetSamplingCycle(0);
    sceCtrlSetSamplingMode(PSP_CTRL_MODE_DIGITAL); // Только цифровые кнопки - Bounce не использует аналоговый стик по дизайну
}

void input_update(void) {
    s_prev_pad = s_current_pad;

    // Обработка ошибок чтения контроллера для предотвращения залипания кнопок
    if (sceCtrlReadBufferPositive(&s_current_pad, 1) <= 0) {
        s_current_pad = s_prev_pad; // сохранить предыдущее состояние при ошибке
    }
}

bool input_pressed(unsigned int button) {
    return (s_current_pad.Buttons & button) && !(s_prev_pad.Buttons & button);
}

bool input_held(unsigned int button) {
    return (s_current_pad.Buttons & button);
}

bool input_released(unsigned int button) {
    return !(s_current_pad.Buttons & button) && (s_prev_pad.Buttons & button);
}