#ifndef LOCAL_H
#define LOCAL_H

#ifdef __cplusplus
extern "C" {
#endif

// Константы строк (точно как в оригинальном Local.java)
#define QHJ_BOUN_INSTRUCTIONS_PART_1  0
#define QHJ_BOUN_INSTRUCTIONS_PART_2  1
#define QHJ_BOUN_INSTRUCTIONS_PART_3  2
#define QHJ_BOUN_INSTRUCTIONS_PART_4  3
#define QHJ_BOUN_INSTRUCTIONS_PART_5  4
#define QHJ_BOUN_INSTRUCTIONS_PART_6  5
#define QTJ_BOUN_BACK                 6
#define QTJ_BOUN_CONGRATULATIONS      7
#define QTJ_BOUN_CONTINUE             8
#define QTJ_BOUN_EXIT                 9
#define QTJ_BOUN_GAME_NAME           10
#define QTJ_BOUN_GAME_OVER           11
#define QTJ_BOUN_HIGH_SCORES         12
#define QTJ_BOUN_INSTRUCTIONS        13
#define QTJ_BOUN_LEVEL               14
#define QTJ_BOUN_LEVEL_COMPLETED     15
#define QTJ_BOUN_NEW_GAME            16
#define QTJ_BOUN_NEW_HIGH_SCORE      17
#define QTJ_BOUN_NEXT                18
#define QTJ_BOUN_OK                  19
#define QTJ_BOUN_PAUSE               20

// Функция локализации (аналог Java Local.getText)
const char* local_get_text(int string_id);

// Дополнительные функции для строк, отсутствующих в lang файлах
const char* local_get_select_level_text(void);
const char* local_get_settings_text(void);


#ifdef __cplusplus
}
#endif

#endif // LOCAL_H