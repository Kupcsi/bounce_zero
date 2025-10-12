package com.nokia.mid.appl.boun;

import com.nokia.mid.sound.Sound;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class BounceCanvas
  extends TileCanvas
{
  public int mSplashIndex;
  public Image mSplashImage;
  private int mSplashTimer;
  protected Sound mSoundHoop;
  protected Sound mSoundPickup;
  protected Sound mSoundPop;
  private BounceUI mUI;
  public Ball mBall;
  public int numRings;
  public int numLives;
  public int mScore;
  public int bonusCntrValue;
  public int mLevelDisCntr;
  public boolean mExitFlag;
  public boolean mOpenExitFlag;
  public boolean mPaintUIFlag;
  public final Font TEXT_FONT = Font.getFont(32, 0, 8);

  public Image mFullScreenBuffer;
  
  public Graphics mFullScreenGraphics = null;
  
  public boolean mClearScreenFlag;
  
  private boolean mCheat = false;
  public boolean mInvincible = false;
  private int mCheatSeq = 0;

  private static final String[] SPLASH_NAME = new String[] { "/icons/nokiagames.png", "/icons/bouncesplash.png" };
  
  public long mRepaintTime = 0L;
  public int mRepaintCount = 0;

  public boolean mIncomingCall = true;

  public BounceCanvas(BounceUI paramBounceUI, int paramInt) {
    super(paramBounceUI.mDisplay);
    this.mUI = paramBounceUI;
    
    this.mSoundHoop = loadSound("/sounds/up.ott");
    this.mSoundPickup = loadSound("/sounds/pickup.ott");
    this.mSoundPop = loadSound("/sounds/pop.ott");
   
    try {
      this.mFullScreenBuffer = Image.createImage(128, 128);
    }
    catch (Exception exception) {}

    
    this.mSplashIndex = 1;
    try {
      this.mSplashImage = Image.createImage(SPLASH_NAME[this.mSplashIndex]);
    } catch (IOException iOException) {
      this.mSplashImage = Image.createImage(1, 1);
    } 
    
    start();
  }
  
  public void resetGame(int paramInt) {
    this.mLevelNum = paramInt;
    this.numRings = 0;
    this.numLives = 3;
    this.mScore = 0;
    this.mExitFlag = false;
    this.mOpenExitFlag = false;
    this.mClearScreenFlag = false;
    createNewLevel();
  }

  public void createNewLevel() {
    this.mBall = null;
    disposeLevel();
    
    loadLevel(this.mLevelNum);
    this.numRings = 0;
    this.mLevelDisCntr = 120;
    this.mPaintUIFlag = true;
    
    createBufferFocused(this.mStartPosX, this.mStartPosY);
  }
  
  public void createBufferFocused(int paramInt1, int paramInt2) {
    Runtime.getRuntime().gc();

    if (paramInt1 * 12 + 6 < 64) {

      this.scrollFlag = false;
      this.scrollOffset = 64;

      this.rightDrawEdge = 128;
      this.leftDrawEdge = 0;

      this.tileX = 0;
    
    }
    else if (paramInt1 * 12 + 6 > this.mTileMapWidth * 12 - 64) {

      this.scrollFlag = false;
      this.scrollOffset = 92;

      
      this.rightDrawEdge = 156;
      this.leftDrawEdge = 28;
      
      this.tileX = this.mTileMapWidth - 13;
    
    }
    else {
      
      this.scrollFlag = true;
      this.scrollOffset = 0;

      
      this.rightDrawEdge = 143;
      this.leftDrawEdge = 15;

      
      this.tileX = paramInt1 - 6;
    } 

    this.divisorLine = 156;
  
    this.tileY = paramInt2 / 7 * 7;
    
    this.divTileX = this.tileX + 13;
    this.divTileY = this.tileY;
    
    byte b = 0;
    
    if (this.mBallSize == 1) {
      
      b = 16;
    }
    else {
      
      b = 12;
    } 
  
    this.mBall = new Ball((paramInt1 - this.tileX) * 12 + (b >> 1), (paramInt2 - this.tileY) * 12 + (b >> 1), this.mBallSize, this);
    
    this.mBall.setRespawn(paramInt1, paramInt2);

    if (this.mBallSize == 1 && !this.mBall.collisionDetection(this.mBall.xPos, this.mBall.yPos)) {
      
      byte b1 = 4;
      
      if (this.mBall.collisionDetection(this.mBall.xPos - b1, this.mBall.yPos)) {
        
        this.mBall.xPos -= b1;
      }
      else if (this.mBall.collisionDetection(this.mBall.xPos, this.mBall.yPos - b1)) {
        
        this.mBall.yPos -= b1;
      }
      else if (this.mBall.collisionDetection(this.mBall.xPos - b1, this.mBall.yPos - b1)) {
        
        this.mBall.xPos -= b1;
        this.mBall.yPos -= b1;
      } 
    } 
   
    createNewBuffer();
  }
  
  public void checkData() {
    if (this.mLevelNum > this.mUI.mBestLevel) {
      this.mUI.mBestLevel = this.mLevelNum;
      this.mUI.setGameData(1);
    } 
    
    if (this.mScore > this.mUI.mBestScore) {
      this.mUI.mBestScore = this.mScore;
      this.mUI.mNewBestScore = true;
      this.mUI.setGameData(2);
    } 
    
    this.mUI.mLastScore = this.mScore;
  }
  
  public void screenFlip() {
    int i = this.mBall.xPos / 12;
    int j = this.mBall.xPos - i * 12 - 6;
    
    cleanBuffer(false);

    
    if (this.mBall.yPos < 0) {
      this.tileY -= 7;
      this.divTileY -= 7;
      this.mBall.yPos += 84;
    
    }
    else if (this.mBall.yPos > 96) {
      this.tileY += 7;
      this.divTileY += 7;
      this.mBall.yPos -= 84;
    } 

    
    if (!this.scrollFlag && this.tileX - 13 - this.divisorLine / 12 == 0) {
    
      if (this.divisorLine < this.mBall.xPos) {
        this.mBall.xPos -= this.divisorLine;
      } else {
        this.mBall.xPos = this.mBall.xPos - this.divisorLine + 156;
      } 

      this.tileX = 0;
      
      this.leftDrawEdge = 0;
      this.rightDrawEdge = 128;
      
      this.scrollOffset = 64;
   
    }
    else if (!this.scrollFlag) {

      this.tileX = this.mTileMapWidth - 13;
      
      this.leftDrawEdge = 28;
      this.rightDrawEdge = 156;
     
      if (this.mBall.xPos > this.divisorLine) {
        
        this.mBall.xPos = 156 - this.divisorLine + 156 - this.mBall.xPos;
      
      }
      else {
        
        this.mBall.xPos = 156 - this.divisorLine - this.mBall.xPos;
      } 
      
      this.scrollOffset = 92;
    
    }
    else {

      if (this.mBall.xPos > this.divisorLine) {
        
        this.tileX = this.tileX - 13 + i - 6;
      }
      else {
        
        this.tileX = this.tileX + i - 6;
      } 
      
      if (this.tileX < 0) {
        
        j += this.tileX * 12;
        this.tileX = 0;
      }
      else if (this.tileX > this.mTileMapWidth - 13 - 1) {
        
        j += (this.tileX - this.mTileMapWidth - 13 - 1) * 12;
        this.tileX = this.mTileMapWidth - 13 - 1;
      } 

      
      this.leftDrawEdge = 14 + j;
      this.rightDrawEdge = 142 + j;
      this.mBall.xPos = 78 + j;
    } 

    this.divTileX = this.tileX + 13;
    this.divisorLine = 156;
   
    createNewBuffer();
  }
  
  public void add2Score(int paramInt) {
    this.mScore += paramInt;
    this.mPaintUIFlag = true;
  }

  public void paint2Buffer() {
    if (this.mFullScreenGraphics == null) {
      this.mFullScreenGraphics = this.mFullScreenBuffer.getGraphics();
    }
    
    if (this.mBall != null)
    {
      this.mBall.paint(this.bufferGraphics);
    }
    
    while (!this.hoopImageList.isEmpty()) {
      
      Image image = this.hoopImageList.firstElement();
      Integer integer1 = this.hoopXPosList.firstElement();
      Integer integer2 = this.hoopYPosList.firstElement();
      
      this.bufferGraphics.drawImage(image, integer1.intValue(), integer2.intValue(), 20);
      
      this.hoopImageList.removeElementAt(0);
      this.hoopXPosList.removeElementAt(0);
      this.hoopYPosList.removeElementAt(0);
    } 
    
    if (this.buffer != null)
    {
      if (this.leftDrawEdge < this.rightDrawEdge) {
        
        this.mFullScreenGraphics.drawImage(this.buffer, 0 - this.leftDrawEdge, 0, 20);
      }
      else {
        
        this.mFullScreenGraphics.drawImage(this.buffer, 0 - this.leftDrawEdge, 0, 20);
        this.mFullScreenGraphics.drawImage(this.buffer, 156 - this.leftDrawEdge, 0, 20);
      } 
    }
    
    if (this.mLevelDisCntr != 0) {
      this.mFullScreenGraphics.setColor(16777214);
      this.mFullScreenGraphics.setFont(this.TEXT_FONT);
      this.mFullScreenGraphics.drawString(this.mLevelNumStr, 44, 84, 20);
    } 
 
    if (this.mPaintUIFlag) {
      
      this.mFullScreenGraphics.setColor(545706);
      this.mFullScreenGraphics.fillRect(0, 97, 128, 32);
      
      for (byte b1 = 0; b1 < this.numLives; b1++)
      {
        this.mFullScreenGraphics.drawImage(this.mUILife, 5 + b1 * (this.mUILife.getWidth() - 1), 99, 20);
      }
      
      for (byte b2 = 0; b2 < this.mTotalNumRings - this.numRings; b2++)
      {
        this.mFullScreenGraphics.drawImage(this.mUIRing, 5 + b2 * (this.mUIRing.getWidth() - 1), 112, 20);
      }
      
      this.mFullScreenGraphics.setColor(16777214);
      this.mFullScreenGraphics.drawString(zeroString(this.mScore), 64, 100, 20);
     
      if (this.bonusCntrValue != 0) {
        
        this.mFullScreenGraphics.setColor(16750611);
        this.mFullScreenGraphics.fillRect(1, 128 - 3 * this.bonusCntrValue / 30, 5, 128);
      } 
      
      this.mPaintUIFlag = false;
    } 
  }
  
  public void paint(Graphics paramGraphics) {
    if (this.mSplashIndex != -1) {
      if (this.mSplashImage != null) {
        paramGraphics.setColor(0);
        paramGraphics.fillRect(0, 0, this.mWidth, this.mHeight);
        paramGraphics.drawImage(this.mSplashImage, this.mWidth >> 1, this.mHeight >> 1, 3);
      
      }
    
    }
    else if (this.mClearScreenFlag) {
      paramGraphics.setColor(0);
      paramGraphics.fillRect(0, 0, this.mWidth, this.mHeight);
      this.mClearScreenFlag = false;
    } else {
      paramGraphics.drawImage(this.mFullScreenBuffer, 0, 0, 20);
    } 
  }
  
  public void run() {
    if (this.mLoadLevelFlag) {
      createNewLevel();
      repaint();
      
      return;
    } 
    if (this.mSplashIndex != -1) {
      if (this.mSplashImage == null || this.mSplashImage == null) {
        this.mIncomingCall = false;
        this.mUI.displayMainMenu();
      }
      else if (this.mSplashTimer > 30) {
        this.mSplashImage = null;
        Runtime.getRuntime().gc();
        
        switch (this.mSplashIndex) {
          case 0:
            this.mSplashIndex = 1;
            try {
              this.mSplashImage = Image.createImage(SPLASH_NAME[this.mSplashIndex]);
            } catch (IOException iOException) {
              this.mSplashImage = Image.createImage(1, 1);
            } 
            repaint();
            break;
          case 1:
            this.mSplashIndex = -1;
            this.mIncomingCall = false;
            this.mUI.displayMainMenu();
            break;
        } 
        this.mSplashTimer = 0;
      } else {
        this.mSplashTimer++;
      } 
      
      repaint();
      
      return;
    } 
    
    if (this.mLevelDisCntr != 0) {
      this.mLevelDisCntr--;
    }

    
    if (this.mBall.yPos < 0 || this.mBall.yPos > 96) {
      screenFlip();
    
    }
    else {
      
      cleanBuffer(true);

      this.mBall.update();
      
      testScroll(this.mBall.xPos, this.mBall.xOffset);
    } 

    
    if (this.mBall.ballState == 1) {
      if (this.numLives < 0) {
        checkData();
        stop();
        this.mIncomingCall = false;
        this.mUI.displayGameOver();
        
        return;
      } 
      createBufferFocused(this.mBall.respawnX, this.mBall.respawnY);
    } 

    
    if (this.mNumMoveObj != 0) {
      updateMovingSpikeObj();
    }

    
    if (this.numRings == this.mTotalNumRings) {
      this.mOpenExitFlag = true;
    }

    if (this.mOpenExitFlag && this.mExitPos != -1) {
      int i = this.leftDrawEdge;
      int j = this.rightDrawEdge;
      
      if (this.mExitPos <= this.divisorLine) {
        if (this.leftDrawEdge > this.divisorLine) {
          i = this.leftDrawEdge - 156;
        }
        
        if (this.rightDrawEdge > this.divisorLine) {
          j = this.rightDrawEdge - 156;
        }
      } 
      
      if (this.mExitPos > this.divisorLine) {
        if (this.leftDrawEdge < this.divisorLine) {
          i = this.leftDrawEdge + 156;
        }
        
        if (this.rightDrawEdge < this.divisorLine) {
          j = this.rightDrawEdge + 156;
        }
      } 
      
      if (this.mExitPos >= i && this.mExitPos <= j) {
        if (this.mOpenFlag) {
          this.mExitPos = -1;
          this.mOpenExitFlag = false;
        } else {
          
          openExit();
        } 
        
        this.tileMap[this.mTopLeftTileRow][this.mTopLeftTileCol] = (short)(this.tileMap[this.mTopLeftTileRow][this.mTopLeftTileCol] | 0x80);
        cleanBuffer(true);
      } 
    } 
    
    this.bonusCntrValue = 0;

    if (this.mBall.speedBonusCntr != 0 || this.mBall.gravBonusCntr != 0 || this.mBall.jumpBonusCntr != 0) {
      if (this.mBall.speedBonusCntr > this.bonusCntrValue) {
        this.bonusCntrValue = this.mBall.speedBonusCntr;
      }
      
      if (this.mBall.gravBonusCntr > this.bonusCntrValue) {
        this.bonusCntrValue = this.mBall.gravBonusCntr;
      }
      
      if (this.mBall.jumpBonusCntr > this.bonusCntrValue) {
        this.bonusCntrValue = this.mBall.jumpBonusCntr;
      }
      
      if (this.bonusCntrValue % 30 == 0 || this.bonusCntrValue == 1) {
        this.mPaintUIFlag = true;
      }
    } 

    scrollBuffer(this.mBall.xPos, this.mBall.xOffset, 16);
    paint2Buffer();
    
    repaint();

    if (this.mExitFlag) {
      this.mExitFlag = false;


      
      this.mOpenExitFlag = false;
      
      this.mLoadLevelFlag = true;
      this.mLevelNum++;
      
      add2Score(5000);
      
      checkData();
      
      if (this.mLevelNum >= 11) {
        this.mIncomingCall = false;
        this.mUI.displayGameOver();
      } else {
        
        this.mIncomingCall = false;
        this.mUI.displayLevelComplete();
        this.mClearScreenFlag = true;
        repaint();
      } 
    } 
  }
  
  public void keyPressed(int paramInt) {
    if (this.mSplashIndex != -1) {
      this.mSplashTimer = 31;
      
      return;
    } 
    if (this.mBall == null) {
      return;
    }
    
    switch (paramInt) {
      
      case 49:
        this.mLoadLevelFlag = true;
        if (this.mCheat && --this.mLevelNum < 1) {
          this.mLevelNum = 11;
        }

      case 51:
        this.mLoadLevelFlag = true;
        if (this.mCheat && ++this.mLevelNum > 11) {
          this.mLevelNum = 1;
        }

      case 55:
        if (this.mCheatSeq == 0 || this.mCheatSeq == 2) {
          this.mCheatSeq++;
        } else {
          this.mCheatSeq = 0;
        } 

      case 56:
        if (this.mCheatSeq == 1 || this.mCheatSeq == 3) {
          this.mCheatSeq++;
        } else if (this.mCheatSeq == 5) {
          this.mSoundHoop.play(1);
          this.mInvincible = true;
          this.mCheatSeq = 0;
        } else {
          this.mCheatSeq = 0;
        } 

      case 57:
        if (this.mCheatSeq == 4) {
          this.mCheatSeq++;
        } else if (this.mCheatSeq == 5) {
          this.mSoundPop.play(1);
          this.mCheat = true;
          this.mCheatSeq = 0;
        } else {
          this.mCheatSeq = 0;
        } 

      case -5:
        return;

      case -7:
      case -6:
        this.mIncomingCall = false;
        this.mUI.displayMainMenu();
    } 

    switch (getGameAction(paramInt)) {
      case 1:
        this.mBall.setDirection(8);

      case 6:
        this.mBall.setDirection(4);
      
      case 2:
        this.mBall.setDirection(1);

      
      case 5:
        this.mBall.setDirection(2);
    } 
  }

  public void keyReleased(int paramInt) {
    if (this.mBall == null) {
      return;
    }
    
    switch (getGameAction(paramInt)) {
      case 1:
        this.mBall.releaseDirection(8);
        break;
      case 6:
        this.mBall.releaseDirection(4);
        break;
      case 2:
        this.mBall.releaseDirection(1);
        break;
      case 5:
        this.mBall.releaseDirection(2);
        break;
    } 
  }

  public static String zeroString(int paramInt) {
    String str;
    if (paramInt < 100) {
      str = "0000000";
    } else if (paramInt < 1000) {
      str = "00000";
    } else if (paramInt < 10000) {
      str = "0000";
    } else if (paramInt < 100000) {
      str = "000";
    } else if (paramInt < 1000000) {
      str = "00";
    } else if (paramInt < 10000000) {
      str = "0";
    } else {
      str = "";
    } 
    return str + paramInt;
  }
 
  protected Sound loadSound(String paramString) {
    byte[] arrayOfByte = new byte[100];
    
    Sound sound = null;

    DataInputStream dataInputStream = new DataInputStream(getClass().getResourceAsStream(paramString));
    try {
      int i = dataInputStream.read(arrayOfByte);
      dataInputStream.close();
      byte[] arrayOfByte1 = new byte[i];
      System.arraycopy(arrayOfByte, 0, arrayOfByte1, 0, i);
      sound = new Sound(arrayOfByte1, 1);
    }
    catch (IOException iOException) {

      
      sound = new Sound(1000, 500L);
      sound.play(3);
    } 
    return sound;
  }
 
  public void hideNotify() {
    if (this.mIncomingCall) {
      if (this.mBall != null)
      {
        this.mBall.resetDirections();
      }
      this.mUI.displayMainMenu();
    } 
    this.mIncomingCall = true;
  }
}