package com.nokia.mid.appl.boun;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStore;


public class BounceUI
  implements CommandListener
{
  public static final String RMS_NAME = "bounceRMS";
  public static final int RMS_LEVEL_DATA_RECORD_ID = 1;
  public static final int RMS_SCORE_DATA_RECORD_ID = 2;
  private static final int STATE_GAME_PLAYING = 1;
  private static final int STATE_START = 2;
  private static final int STATE_GAME_OVER = 3;
  private static final int STATE_GAME_STARTING = 4;
  private static boolean RESTART_IS_REQUIRED = true;
  private static boolean RESTART_NOT_REQUIRED = false;
  private static final boolean OLD_HIGH_SCORE_STYLE = true;
  public Bounce mMidlet;
  public Display mDisplay;
  public BounceCanvas mCanvas;
  public int mState = 2;
  
  public int mBestLevel;
  
  public int mBestScore;
  public boolean mNewBestScore;
  public int mLastScore;
  private Command mOkayCmd;
  private Command mBackCmd;
  private Command mContinueCmd;
  private List mMainMenu;
  private Form mTextPage;
  private int mSavedMenuItem;
  private static int MAIN_MENU_CONTINUE = 0;
  private static int MAIN_MENU_NEW_GAME = 1;
  private static int MAIN_MENU_HIGH_SCORE = 2;
  private static int MAIN_MENU_INSTRUCTIONS = 3;
  private static int MAIN_MENU_COUNT = 4;
  
  private String[] mMainMenuItems = new String[MAIN_MENU_COUNT];
  
  public BounceUI(Bounce paramBounce) {
    this.mMidlet = paramBounce;
    getGameData();
    this.mCanvas = new BounceCanvas(this, 1);
    this.mCanvas.start();
    
    this.mDisplay = Display.getDisplay(this.mMidlet);
    this.mDisplay.setCurrent((Displayable)this.mCanvas);
    
    this.mMainMenuItems[MAIN_MENU_CONTINUE] = Local.getText(8);
    this.mMainMenuItems[MAIN_MENU_NEW_GAME] = Local.getText(16);
    this.mMainMenuItems[MAIN_MENU_HIGH_SCORE] = Local.getText(12);
    this.mMainMenuItems[MAIN_MENU_INSTRUCTIONS] = Local.getText(13);
  }
  
  public void displayMainMenu() {
    this.mMainMenu = new List(Local.getText(10), 3);
    if (this.mBackCmd == null) {
      this.mBackCmd = new Command(Local.getText(6), 2, 1);
    }
    
    if (this.mState == 1 || this.mBestLevel > 1) {
      this.mMainMenu.append(this.mMainMenuItems[0], null);
    }
    for (byte b = 1; b < this.mMainMenuItems.length; b++) {
      this.mMainMenu.append(this.mMainMenuItems[b], null);
    }
    this.mMainMenu.addCommand(this.mBackCmd);
    this.mMainMenu.setCommandListener(this);
    if (this.mCanvas.mSplashIndex != -1) {
      this.mCanvas.mSplashIndex = -1;
      this.mCanvas.mSplashImage = null;
    } 
    this.mMainMenu.setSelectedIndex(this.mSavedMenuItem, true);
    this.mCanvas.stop();
    this.mDisplay.setCurrent((Displayable)this.mMainMenu);
  }
  
  public void displayGame(boolean paramBoolean, int paramInt) {
    this.mDisplay.setCurrent((Displayable)this.mCanvas);
    if (paramBoolean) {
      this.mCanvas.resetGame(paramInt);
    }
    this.mCanvas.start();
    this.mState = 1;
  }

  
  public void displayHighScore() {
    this.mTextPage = new Form(Local.getText(12));
    this.mTextPage.append(String.valueOf(this.mBestScore));
    this.mTextPage.addCommand(this.mBackCmd);
    this.mTextPage.setCommandListener(this);
    this.mDisplay.setCurrent((Displayable)this.mTextPage);
  }

  
  public void displayInstructions() {
    this.mTextPage = new Form(Local.getText(13));
    this.mTextPage.append(Local.getText(0));
    this.mTextPage.append(Local.getText(1));
    this.mTextPage.append(Local.getText(2));
    this.mTextPage.append(Local.getText(3));
    this.mTextPage.append(Local.getText(4));
    this.mTextPage.append(Local.getText(5));
    this.mTextPage.addCommand(this.mBackCmd);
    this.mTextPage.setCommandListener(this);
    this.mDisplay.setCurrent((Displayable)this.mTextPage);
    this.mTextPage = null;
  }
  
  public void displayGameOver() {
    this.mState = 3;
    this.mCanvas.stop();
    
    if (this.mOkayCmd == null) {
      this.mOkayCmd = new Command(Local.getText(19), 4, 1);
    }
    this.mTextPage = new Form(Local.getText(11));
    this.mTextPage.append(Local.getText(11));
    this.mTextPage.append("\n\n");
    
    if (this.mNewBestScore) {
      this.mTextPage.append(Local.getText(17));
      this.mTextPage.append("\n\n");
    } 
    
    this.mTextPage.append(String.valueOf(this.mLastScore));
    this.mTextPage.addCommand(this.mOkayCmd);
    this.mTextPage.setCommandListener(this);
    this.mDisplay.setCurrent((Displayable)this.mTextPage);
    this.mTextPage = null;
  }
  
  public void displayLevelComplete() {
    this.mCanvas.stop();
    
    if (this.mContinueCmd == null) {
      this.mContinueCmd = new Command(Local.getText(8), 4, 1);
    }
    this.mTextPage = new Form("");
    this.mTextPage.append(this.mCanvas.mLevelCompletedStr);
    this.mTextPage.append("\n\n");
    this.mTextPage.append("" + this.mLastScore + "\n");

  
    this.mTextPage.addCommand(this.mContinueCmd);
    this.mTextPage.setCommandListener(this);
    this.mDisplay.setCurrent((Displayable)this.mTextPage);
    this.mTextPage = null;
  }
  
  public void commandAction(Command paramCommand, Displayable paramDisplayable) {
    if (paramCommand == List.SELECT_COMMAND) {
      String str = this.mMainMenu.getString(this.mMainMenu.getSelectedIndex());
      this.mSavedMenuItem = this.mMainMenu.getSelectedIndex();
      if (str.equals(this.mMainMenuItems[MAIN_MENU_CONTINUE])) {


        
        boolean bool = RESTART_NOT_REQUIRED;
        int i = 0;
        if (this.mState != 1) {
          bool = RESTART_IS_REQUIRED;
          if (this.mState == 3) {
            i = this.mCanvas.mLevelNum;
          } else {
            i = this.mBestLevel;
          } 
        } 
        displayGame(bool, i);
      } else if (str.equals(this.mMainMenuItems[MAIN_MENU_NEW_GAME])) {
       
        if (this.mState != 4) {
          this.mState = 4;
          this.mNewBestScore = false;
          displayGame(RESTART_IS_REQUIRED, 1);
        } 
      } else if (str.equals(this.mMainMenuItems[MAIN_MENU_HIGH_SCORE])) {


        
        displayHighScore();
      } else if (str.equals(this.mMainMenuItems[MAIN_MENU_INSTRUCTIONS])) {


        
        displayInstructions();
      }
    
    } else if (paramCommand == this.mBackCmd || paramCommand == this.mOkayCmd) {
      if (this.mDisplay.getCurrent() == this.mMainMenu) {
        this.mMidlet.destroyApp(true);
        this.mMidlet.notifyDestroyed();
      } else {
        displayMainMenu();
      } 
    } else if (paramCommand == this.mContinueCmd) {
      displayGame(RESTART_NOT_REQUIRED, 0);
    } 
  }

  
  public void getGameData() {
    byte[] arrayOfByte1 = new byte[1];
    byte[] arrayOfByte2 = new byte[8];


    
    try {
      RecordStore recordStore = RecordStore.openRecordStore("bounceRMS", true);
      
      if (recordStore.getNumRecords() != 2) {
        recordStore.addRecord(arrayOfByte1, 0, arrayOfByte1.length);
        recordStore.addRecord(arrayOfByte2, 0, arrayOfByte2.length);
      } 
      
      arrayOfByte1 = recordStore.getRecord(1);
      arrayOfByte2 = recordStore.getRecord(2);
      this.mBestLevel = arrayOfByte1[0];
      
      for (byte b = 0; b < 8; b++) {
        int i = 0;
        switch (b) {
          case 0:
            i = 10000000;
            break;
          case 1:
            i = 1000000;
            break;
          case 2:
            i = 100000;
            break;
          case 3:
            i = 10000;
            break;
          case 4:
            i = 1000;
            break;
          case 5:
            i = 100;
            break;
          case 6:
            i = 10;
            break;
          case 7:
            i = 1;
            break;
        } 
        this.mBestScore += arrayOfByte2[b] * i;
      }
    
    }
    catch (Exception exception) {}
  }

  
  public void setGameData(int paramInt) {
    byte[] arrayOfByte = null;
    
    try {
      RecordStore recordStore = RecordStore.openRecordStore("bounceRMS", true);
      
      if (paramInt == 1) {
        arrayOfByte = new byte[1];
        arrayOfByte[0] = (byte)this.mBestLevel;
      }
      else if (paramInt == 2) {
        arrayOfByte = new byte[8];
        
        for (byte b = 0; b < 8; b++) {
          int i = 1;
          
          switch (b) {
            case 0:
              i = 10000000;
              break;
            case 1:
              i = 1000000;
              break;
            case 2:
              i = 100000;
              break;
            case 3:
              i = 10000;
              break;
            case 4:
              i = 1000;
              break;
            case 5:
              i = 100;
              break;
            case 6:
              i = 10;
              break;
            case 7:
              i = 1;
              break;
          } 
          
          arrayOfByte[b] = (byte)(this.mBestScore / i % 10);
        } 
      } 
      
      recordStore.setRecord(paramInt, arrayOfByte, 0, arrayOfByte.length);
    }
    catch (Exception exception) {}
  }
}