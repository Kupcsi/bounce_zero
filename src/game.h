#ifndef GAME_H
#define GAME_H

#include "types.h"

void game_init(void);
void game_update(void);
void game_render(void);
void game_shutdown(void);
void game_reset_camera(void);

// Анимация двери
void game_exit_open(void);
void game_exit_reset(void);
int game_exit_anim_offset(void);
bool game_exit_is_open(void);

// Проверка состояния сохраненной игры
bool game_can_continue(void);

extern Game g_game;

#endif