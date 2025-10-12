#ifndef GAME_H
#define GAME_H

#include "types.h"

void game_init(void);
void game_update(void);
void game_render(void);
void game_cleanup(void);
void game_reset_camera(void);

// Анимация двери
void game_open_exit(void);
void game_reset_exit(void);
int game_get_exit_animation_offset(void);
bool game_is_exit_open(void);

// Проверка состояния сохраненной игры
bool game_can_continue(void);

extern Game g_game;

#endif