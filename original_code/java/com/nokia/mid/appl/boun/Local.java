package com.nokia.mid.appl.boun;

import java.io.DataInputStream;
import java.io.InputStream;

public class Local
{
  private static Local loc = null;
  
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_1 = 0;
  
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_2 = 1;
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_3 = 2;
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_4 = 3;
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_5 = 4;
  public static final short QHJ_BOUN_INSTRUCTIONS_PART_6 = 5;
  public static final short QTJ_BOUN_BACK = 6;
  public static final short QTJ_BOUN_CONGRATULATIONS = 7;
  public static final short QTJ_BOUN_CONTINUE = 8;
  public static final short QTJ_BOUN_EXIT = 9;
  public static final short QTJ_BOUN_GAME_NAME = 10;
  public static final short QTJ_BOUN_GAME_OVER = 11;
  public static final short QTJ_BOUN_HIGH_SCORES = 12;
  public static final short QTJ_BOUN_INSTRUCTIONS = 13;
  public static final short QTJ_BOUN_LEVEL = 14;
  public static final short QTJ_BOUN_LEVEL_COMPLETED = 15;
  public static final short QTJ_BOUN_NEW_GAME = 16;
  public static final short QTJ_BOUN_NEW_HIGH_SCORE = 17;
  public static final short QTJ_BOUN_NEXT = 18;
  public static final short QTJ_BOUN_OK = 19;
  public static final short QTJ_BOUN_PAUSE = 20;
  public static final String phoneLang = System.getProperty("microedition.locale");

  private static String replace(String paramString1, String paramString2, String paramString3) {
    int i = paramString1.indexOf(paramString2);

    
    return (i >= 0) ? (paramString1.substring(0, i) + paramString3 + paramString1.substring(i + paramString2.length())) : paramString1;
  }

  
  public static synchronized String getText(int paramInt) {
    return getText(paramInt, null);
  }


  public static synchronized String getText(int paramInt, String[] paramArrayOfString) {
    try {
      if (loc == null) {
        loc = new Local();
      }

      
      InputStream inputStream = loc.getClass().getResourceAsStream("/lang." + phoneLang);
      if (inputStream == null) {
        inputStream = loc.getClass().getResourceAsStream("/lang.xx");
      }
      if (inputStream == null) {
        return "NoLang";
      }
      DataInputStream dataInputStream = new DataInputStream(inputStream);



      
      dataInputStream.skipBytes(paramInt * 2);
      short s = dataInputStream.readShort();
      
      dataInputStream.skipBytes(s - paramInt * 2 - 2);
      String str = dataInputStream.readUTF();
      dataInputStream.close();

      
      if (paramArrayOfString != null) {
        if (paramArrayOfString.length == 1) {
          str = replace(str, "%U", paramArrayOfString[0]);
        } else {
          for (byte b = 0; b < paramArrayOfString.length; b++) {
            str = replace(str, "%" + b + "U", paramArrayOfString[b]);
          }
        } 
      }
      return str;
    } catch (Exception exception) {
      
      return "Err";
    } 
  }
}