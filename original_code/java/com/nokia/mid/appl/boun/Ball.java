package com.nokia.mid.appl.boun;

import com.nokia.mid.sound.Sound;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Ball
{
  private boolean mDebugCD = false;
  public int xPos;
  public int yPos;
  public int globalBallX;
  public int globalBallY;
  public int xOffset;
  public int xSpeed;
  public int ySpeed;
  public int direction;
  public int ballSize;
  public int mHalfBallSize;
  public int respawnX;
  public int respawnY;
  public int ballState;
  public int jumpOffset;
  public int speedBonusCntr;
  public int gravBonusCntr;
  public int jumpBonusCntr;
  public boolean mGroundedFlag;
  public boolean mCDRubberFlag;
  public boolean mCDRampFlag;
  public int slideCntr;
  public static final byte[][] TRI_TILE_DATA = new byte[][] { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };

  public static final byte[][] SMALL_BALL_DATA = new byte[][] { { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 } };

  public static final byte[][] LARGE_BALL_DATA = new byte[][] { { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 } };

  public BounceCanvas mCanvas;

  public Image ballImage;

  public Image poppedImage;

  public Image largeBallImage;

  public Image smallBallImage;

  private int popCntr;

  public Ball(int paramInt1, int paramInt2, int paramInt3, BounceCanvas paramBounceCanvas) {
    this.xPos = paramInt1;
    this.yPos = paramInt2;
    
    this.globalBallX = 0;
    this.globalBallY = 0;

    
    this.xSpeed = 0;
    this.ySpeed = 0;
    
    this.xOffset = 0;
    
    this.mCanvas = paramBounceCanvas;
    
    this.jumpOffset = 0;
    
    this.mGroundedFlag = false;
    this.mCDRubberFlag = false;
    this.mCDRampFlag = false;
    
    this.popCntr = 0;
    
    this.speedBonusCntr = 0;
    this.gravBonusCntr = 0;
    this.jumpBonusCntr = 0;
    
    this.slideCntr = 0;
    
    this.ballState = 0;

    
    this.direction = 0;
    
    this.mCanvas.setBallImages(this);
    
    if (paramInt3 == 0) {
      this.ballSize = 12;
      this.mHalfBallSize = 6;
      this.ballImage = this.smallBallImage;
    }
    else if (paramInt3 == 1) {
      this.ballSize = 16;
      this.mHalfBallSize = 8;
      this.ballImage = this.largeBallImage;
    } 
  }

  public void setRespawn(int paramInt1, int paramInt2) {
    this.respawnX = paramInt1;
    this.respawnY = paramInt2;
    
    if (this.ballSize == 16) {
      this.mCanvas.mBallSize = 1;
    } else {
      
      this.mCanvas.mBallSize = 0;
    } 
  }

  public void setDirection(int paramInt) {
    if (paramInt == 8 || paramInt == 4 || paramInt == 2 || paramInt == 1) {
      this.direction |= paramInt;
    }
  }

  public void releaseDirection(int paramInt) {
    if (paramInt == 8 || paramInt == 4 || paramInt == 2 || paramInt == 1) {
      this.direction &= paramInt ^ 0xFFFFFFFF;
    }
  }

  public void resetDirections() {
    this.direction &= 0xFFFFFFF0;
  }

  public boolean collisionDetection(int paramInt1, int paramInt2) {
    byte b = 0;
    
    if (paramInt2 < 0)
    {

      b = 12;
    }

    
    int i = (paramInt1 - this.mHalfBallSize) / 12;
    int j = (paramInt2 - b - this.mHalfBallSize) / 12;

    
    this.globalBallX = paramInt1 - this.mHalfBallSize;
    this.globalBallY = paramInt2 - this.mHalfBallSize;
    
    if (this.xPos < this.mCanvas.divisorLine) {
      this.globalBallX += this.mCanvas.tileX * 12;
      this.globalBallY += this.mCanvas.tileY * 12;
    }
    else {
      
      this.globalBallX += (this.mCanvas.divTileX - 13) * 12 - this.mCanvas.divisorLine;
      this.globalBallY += this.mCanvas.divTileY * 12;
    } 

    
    int k = (paramInt1 - 1 + this.mHalfBallSize) / 12 + 1;
    int m = (paramInt2 - b - 1 + this.mHalfBallSize) / 12 + 1;
    
    boolean bool = true;

    
    for (int n = i; n < k; n++) {
      for (int i1 = j; i1 < m; i1++) {
        
        if (n * 12 > 156) {
          bool = testTile(this.mCanvas.tileY + i1, this.mCanvas.tileX + n - 13, bool);
        } else if (this.xPos < this.mCanvas.divisorLine) {
          
          bool = testTile(this.mCanvas.tileY + i1, this.mCanvas.tileX + n, bool);
        } else {
          
          int i2 = this.mCanvas.divTileX - 13 - this.mCanvas.divisorLine / 12;
          bool = testTile(this.mCanvas.divTileY + i1, i2 + n, bool);
        } 
      } 
    } 

    return bool;
  }

  public void enlargeBall() {
    byte b = 2;
    this.ballSize = 16;
    this.mHalfBallSize = 8;
    this.ballImage = this.largeBallImage;
 
    boolean bool = false;

    while (!bool) {
      bool = true;
      if (collisionDetection(this.xPos, this.yPos - b)) {
        this.yPos -= b; continue;
      }  if (collisionDetection(this.xPos - b, this.yPos - b)) {
        this.xPos -= b;
        this.yPos -= b; continue;
      }  if (collisionDetection(this.xPos + b, this.yPos - b)) {
        this.xPos += b;
        this.yPos -= b; continue;
      }  if (collisionDetection(this.xPos, this.yPos + b)) {
        this.yPos += b; continue;
      }  if (collisionDetection(this.xPos - b, this.yPos + b)) {
        this.xPos -= b;
        this.yPos += b; continue;
      }  if (collisionDetection(this.xPos + b, this.yPos + b)) {
        this.xPos += b;
        this.yPos += b; continue;
      } 
      bool = false;
      b++;
    } 
  }

  public void shrinkBall() {
    byte b = 2;
    this.ballSize = 12;
    this.mHalfBallSize = 6;
    this.ballImage = this.smallBallImage;

    
    if (collisionDetection(this.xPos, this.yPos + b)) {
      this.yPos += b;
    } else if (collisionDetection(this.xPos, this.yPos - b)) {
      this.yPos -= b;
    } 
  }

  public void popBall() {
    if (!this.mCanvas.mInvincible) {
      this.popCntr = 5;
      this.ballState = 2;
      this.xOffset = 0;
      this.mCanvas.numLives--;
      
      this.speedBonusCntr = 0;
      this.gravBonusCntr = 0;
      this.jumpBonusCntr = 0;
      
      this.mCanvas.mPaintUIFlag = true;
      this.mCanvas.mSoundPop.play(1);
    } 
  }

  public void addRing() {
    this.mCanvas.add2Score(500);
    this.mCanvas.numRings++;
    this.mCanvas.mPaintUIFlag = true;
  }

  public void redirectBall(int paramInt) {
    int i = this.xSpeed;
    
    switch (paramInt) {
      case 35:
      case 37:
        this.xSpeed = this.ySpeed;
        this.ySpeed = i;
        break;
      
      case 31:
      case 33:
        this.xSpeed = this.ySpeed >> 1;
        this.ySpeed = i >> 1;
        break;
      
      case 34:
      case 36:
        this.xSpeed = -this.ySpeed;
        this.ySpeed = -i;
        break;
      
      case 30:
      case 32:
        this.xSpeed = -(this.ySpeed >> 1);
        this.ySpeed = -(i >> 1);
        break;
    } 
  }

  public boolean squareCollide(int paramInt1, int paramInt2) {
    byte b1;
    int n;
    byte b2;
    int i1;
    byte[][] arrayOfByte;
    int i = paramInt2 * 12;
    int j = paramInt1 * 12;

    
    int k = this.globalBallX - i;
    int m = this.globalBallY - j;

    if (k >= 0) {
      b1 = k;
      n = 12;
    } else {
      b1 = 0;
      n = this.ballSize + k;
    } 

    
    if (m >= 0) {
      b2 = m;
      i1 = 12;
    } else {
      b2 = 0;
      i1 = this.ballSize + m;
    } 

    if (this.ballSize == 16) {
      arrayOfByte = LARGE_BALL_DATA;
    } else {
      arrayOfByte = SMALL_BALL_DATA;
    } 

    
    if (n > 12) {
      n = 12;
    }
    
    if (i1 > 12) {
      i1 = 12;
    }

    
    for (byte b3 = b1; b3 < n; b3++) {
      for (byte b = b2; b < i1; b++) {
        
        if (arrayOfByte[b - m][b3 - k] != 0) {
          return true;
        }
      } 
    } 

    
    return false;
  }

  public boolean triangleCollide(int paramInt1, int paramInt2, int paramInt3) {
    byte b3;
    int n;
    byte b4;
    int i1;
    byte[][] arrayOfByte;
    int i = paramInt2 * 12;
    int j = paramInt1 * 12;

    
    int k = this.globalBallX - i;
    int m = this.globalBallY - j;

    
    byte b1 = 0;
    byte b2 = 0;
    
    switch (paramInt3) {
      case 30:
      case 34:
        b2 = 11;
        b1 = 11;
        break;

      
      case 31:
      case 35:
        b2 = 11;
        break;

      
      case 33:
      case 37:
        b1 = 11;
        break;
    } 

    if (k >= 0) {
      b3 = k;
      n = 12;
    } else {
      
      b3 = 0;
      n = this.ballSize + k;
    } 

    
    if (m >= 0) {
      b4 = m;
      i1 = 12;
    } else {
      
      b4 = 0;
      i1 = this.ballSize + m;
    } 



    
    if (this.ballSize == 16) {
      arrayOfByte = LARGE_BALL_DATA;
    } else {
      arrayOfByte = SMALL_BALL_DATA;
    } 

    
    if (n > 12) {
      n = 12;
    }
    
    if (i1 > 12) {
      i1 = 12;
    }

    
    for (byte b5 = b3; b5 < n; b5++) {
      for (byte b = b4; b < i1; b++) {
        
        if ((TRI_TILE_DATA[Math.abs(b - b2)][Math.abs(b5 - b1)] & arrayOfByte[b - m][b5 - k]) != 0) {
          if (!this.mGroundedFlag) {
            redirectBall(paramInt3);
          }
          return true;
        } 
      } 
    } 
    return false;
  }

  public boolean thinCollide(int paramInt1, int paramInt2, int paramInt3) {
    int i = paramInt2 * 12;
    int j = paramInt1 * 12;
    int k = i + 12;
    int m = j + 12;

    
    switch (paramInt3) {
      
      case 3:
      case 5:
      case 9:
      case 13:
      case 14:
      case 17:
      case 18:
      case 21:
      case 22:
      case 43:
      case 45:
        i += 4;
        k -= 4;
        break;

      
      case 4:
      case 6:
      case 15:
      case 16:
      case 19:
      case 20:
      case 23:
      case 24:
      case 44:
      case 46:
        j += 4;
        m -= 4;
        break;
    } 

    
    if (TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m))
    {
      return true;
    }
    
    return false;
  }

  public boolean edgeCollide(int paramInt1, int paramInt2, int paramInt3) {
    int i = paramInt2 * 12;
    int j = paramInt1 * 12;
    int k = i + 12;
    int m = j + 12;
    boolean bool = false;
    
    switch (paramInt3) {
      case 15:
      case 19:
      case 23:
      case 27:
        j += 6;
        m -= 6;
        k -= 11;
        bool = TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m);
        break;


      
      case 16:
      case 20:
      case 24:
      case 28:
        j += 6;
        m -= 6;
        i += 11;
        bool = TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m);
        break;


      
      case 13:
      case 17:
        i += 6;
        k -= 6;
        m -= 11;
        bool = TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m);
        break;


      
      case 21:
      case 25:
        m = j;
        j--;
        i += 6;
        k -= 6;
        bool = TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m);
        break;


      
      case 14:
      case 18:
      case 22:
      case 26:
        i += 6;
        k -= 6;
        j += 11;
        bool = TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, i, j, k, m);
        break;
    } 


    
    return bool;
  }

  public boolean testTile(int paramInt1, int paramInt2, boolean paramBoolean) {
    int k;
    if (paramInt1 >= this.mCanvas.mTileMapHeight || paramInt1 < 0 || paramInt2 >= this.mCanvas.mTileMapWidth || paramInt2 < 0) {
      
      paramBoolean = false;
      return false;
    } 

    if (this.ballState == 2) {
      return false;
    }

    int i = this.mCanvas.tileMap[paramInt1][paramInt2] & 0x40;
    int j = this.mCanvas.tileMap[paramInt1][paramInt2] & 0xFFFFFFBF & 0xFFFFFF7F;
    
    Sound sound = null;

    switch (j) {

      case 1:
        if (squareCollide(paramInt1, paramInt2)) {
          paramBoolean = false;
          
          break;
        } 
        this.mCDRampFlag = true;
        break;


      
      case 2:
        if (squareCollide(paramInt1, paramInt2)) {
          this.mCDRubberFlag = true;
          paramBoolean = false;
          
          break;
        } 
        
        this.mCDRampFlag = true;
        break;

      case 34:
      case 35:
      case 36:
      case 37:
        if (triangleCollide(paramInt1, paramInt2, j)) {
          this.mCDRubberFlag = true;
          paramBoolean = false;
          this.mCDRampFlag = true;
        } 
        break;

      case 30:
      case 31:
      case 32:
      case 33:
        if (triangleCollide(paramInt1, paramInt2, j)) {
          paramBoolean = false;
          this.mCDRampFlag = true;
        } 
        break;

      case 10:
        k = this.mCanvas.findSpikeIndex(paramInt2, paramInt1);
        
        if (k != -1) {
          
          int m = this.mCanvas.mMOTopLeft[k][0] * 12 + this.mCanvas.mMOOffset[k][0];
          int n = this.mCanvas.mMOTopLeft[k][1] * 12 + this.mCanvas.mMOOffset[k][1];

          
          if (TileCanvas.rectCollide(this.globalBallX, this.globalBallY, this.globalBallX + this.ballSize, this.globalBallY + this.ballSize, m, n, m + 24, n + 24)) {
            
            paramBoolean = false;
            popBall();
          } 
        } 
        break;

      
      case 3:
      case 4:
      case 5:
      case 6:
        if (thinCollide(paramInt1, paramInt2, j)) {
          paramBoolean = false;
          popBall();
        } 
        break;

      
      case 7:
        this.mCanvas.add2Score(200);
        this.mCanvas.tileMap[this.respawnY][this.respawnX] = 128;
        setRespawn(paramInt2, paramInt1);
        this.mCanvas.tileMap[paramInt1][paramInt2] = 136;
        sound = this.mCanvas.mSoundPickup;
        break;

      
      case 23:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false; break;
          } 
          addRing();


          
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x9B | i);
          this.mCanvas.tileMap[paramInt1][paramInt2 + 1] = (short)(0x9C | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;

      
      case 15:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.ballSize == 16) {
            paramBoolean = false;
            
            break;
          } 
          
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x93 | i);
          this.mCanvas.tileMap[paramInt1][paramInt2 + 1] = (short)(0x94 | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;

      
      case 24:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x9C | i);
          this.mCanvas.tileMap[paramInt1][paramInt2 - 1] = (short)(0x9B | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;
      
      case 16:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.ballSize == 16) {
            paramBoolean = false; break;
          } 
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x94 | i);
          this.mCanvas.tileMap[paramInt1][paramInt2 - 1] = (short)(0x93 | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;

      
      case 21:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x99 | i);
          this.mCanvas.tileMap[paramInt1 + 1][paramInt2] = (short)(0x9A | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;
      
      case 13:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.ballSize == 16) {
            paramBoolean = false; break;
          } 
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x91 | i);
          this.mCanvas.tileMap[paramInt1 + 1][paramInt2] = (short)(0x92 | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;

      
      case 22:
        if (thinCollide(paramInt1, paramInt2, j)) {
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x9A | i);
          this.mCanvas.tileMap[paramInt1 - 1][paramInt2] = (short)(0x99 | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;
      
      case 14:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.ballSize == 16) {
            paramBoolean = false; break;
          } 
          addRing();
          this.mCanvas.tileMap[paramInt1][paramInt2] = (short)(0x92 | i);
          this.mCanvas.tileMap[paramInt1 - 1][paramInt2] = (short)(0x91 | i);
          sound = this.mCanvas.mSoundHoop;
        } 
        break;

      
      case 17:
      case 19:
      case 20:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.ballSize == 16) {
            paramBoolean = false; break;
          } 
          if (edgeCollide(paramInt1, paramInt2, j)) {
            paramBoolean = false;
          }
        } 
        break;

      
      case 25:
      case 27:
      case 28:
        if (edgeCollide(paramInt1, paramInt2, j)) {
          paramBoolean = false;
        }
        break;
      
      case 18:
        if (thinCollide(paramInt1, paramInt2, j) && 
          this.ballSize == 16) {
          paramBoolean = false;
        }
        break;


      
      case 9:
        if (thinCollide(paramInt1, paramInt2, j)) {
          if (this.mCanvas.mOpenFlag) {
            this.mCanvas.mExitFlag = true;
            sound = this.mCanvas.mSoundPickup;

            
            break;
          } 
          
          paramBoolean = false;
        } 
        break;

      case 29:
        this.mCanvas.add2Score(1000);
        
        if (this.mCanvas.numLives < 5) {
          this.mCanvas.numLives++;
          this.mCanvas.mPaintUIFlag = true;
        } 
        
        this.mCanvas.tileMap[paramInt1][paramInt2] = 128;
        sound = this.mCanvas.mSoundPickup;
        break;


      
      case 39:
      case 40:
      case 41:
      case 42:
        paramBoolean = false;
        
        if (this.ballSize == 16) {
          shrinkBall();
        }
        break;

      
      case 43:
      case 44:
      case 45:
      case 46:
        if (thinCollide(paramInt1, paramInt2, j)) {
          paramBoolean = false;
          
          if (this.ballSize == 12) {
            enlargeBall();
          }
        } 
        break;

      
      case 47:
      case 48:
      case 49:
      case 50:
        this.gravBonusCntr = 300;
        sound = this.mCanvas.mSoundPickup;
        paramBoolean = false;
        break;

      
      case 51:
      case 52:
      case 53:
      case 54:
        this.jumpBonusCntr = 300;
        sound = this.mCanvas.mSoundPickup;
        paramBoolean = false;
        break;

      
      case 38:
        this.speedBonusCntr = 300;
        sound = this.mCanvas.mSoundPickup;
        paramBoolean = false;
        break;
    } 

    
    if (sound != null) {
      sound.play(1);
    }

    return paramBoolean;
  }

  public void update() {
    int i = this.xPos;
    
    int j = 0;
    int k = 0;
    byte b1 = 0;
    
    boolean bool = false;

    
    if (this.ballState == 2) {
      this.xOffset = 0;
      this.popCntr--;
      if (this.popCntr == 0) {
        this.ballState = 1;
        if (this.mCanvas.numLives < 0) {
          this.mCanvas.mExitFlag = true;
        }
      } 
      
      return;
    } 
    
    int m = this.xPos / 12;
    int n = this.yPos / 12;

    
    if (this.xPos >= 156) {
      m = this.mCanvas.tileX + m - 13;
      n = this.mCanvas.tileY + n;
    } else if (this.xPos < this.mCanvas.divisorLine) {
      
      m = this.mCanvas.tileX + m;
      n = this.mCanvas.tileY + n;
    } else {
      m = this.mCanvas.divTileX - 13 - this.mCanvas.divisorLine / 12 + m;
      n = this.mCanvas.divTileY + n;
    } 

    
    if ((this.mCanvas.tileMap[n][m] & 0x40) != 0) {
      
      if (this.ballSize == 16) {
        k = -30;
        j = -2;
        if (this.mGroundedFlag) {
          this.ySpeed = -10;
        }
      } else {
        k = 42;
        j = 6;
      
      }

    
    }
    else if (this.ballSize == 16) {
      k = 38;
      j = 3;
    } else {
      k = 80;
      j = 4;
    } 

    
    if (this.gravBonusCntr != 0) {
      bool = true;
      k *= -1;
      j *= -1;
      this.gravBonusCntr--;
      if (this.gravBonusCntr == 0) {
        bool = false;
        this.mGroundedFlag = false;
        k *= -1;
        j *= -1;
      } 
    } 
    
    if (this.jumpBonusCntr != 0) {
      if (-1 * Math.abs(this.jumpOffset) > -80) {
        if (bool) {
          this.jumpOffset = 80;
        } else {
          this.jumpOffset = -80;
        } 
      }
      this.jumpBonusCntr--;
    } 
    
    this.slideCntr++;
    if (this.slideCntr == 3) {
      this.slideCntr = 0;
    }

    if (this.ySpeed < -150) {
      this.ySpeed = -150;
    } else if (this.ySpeed > 150) {
      this.ySpeed = 150;
    } 
    if (this.xSpeed < -150) {
      this.xSpeed = -150;
    } else if (this.xSpeed > 150) {
      this.xSpeed = 150;
    } 

    for (byte b2 = 0; b2 < Math.abs(this.ySpeed) / 10; b2++) {
      byte b = 0;

      
      if (this.ySpeed != 0) {
        b = (this.ySpeed < 0) ? -1 : 1;
      }

      
      if (collisionDetection(this.xPos, this.yPos + b)) {
        
        this.yPos += b;
        this.mGroundedFlag = false;

        
        if (k == -30) {
          n = this.mCanvas.tileY + this.yPos / 12;
          if ((this.mCanvas.tileMap[n][m] & 0x40) == 0)
          {
            this.ySpeed >>= 1;
            if (this.ySpeed <= 10 && this.ySpeed >= -10) {
              this.ySpeed = 0;
            
            }
          }
        
        }
      
      }
      else {
        
        if (this.mCDRampFlag && this.xSpeed < 10 && this.slideCntr == 0) {
          
          byte b4 = 1;
          if (collisionDetection(this.xPos + b4, this.yPos + b)) {
            this.xPos += b4;
            this.yPos += b;
            
            this.mCDRampFlag = false;
          } else if (collisionDetection(this.xPos - b4, this.yPos + b)) {
            this.xPos -= b4;
            this.yPos += b;
            
            this.mCDRampFlag = false;
          } 
        } 
        
        if (b > 0 || (bool && b < 0)) {

          
          this.ySpeed = this.ySpeed * -1 / 2;
          this.mGroundedFlag = true;
          if (this.mCDRubberFlag && (this.direction & 0x8) != 0) {
            this.mCDRubberFlag = false;
            if (bool) {
              this.jumpOffset += 10;
            } else {
              this.jumpOffset += -10;
            }
          
          } else if (this.jumpBonusCntr == 0) {
            this.jumpOffset = 0;
          } 


          
          if (this.ySpeed < 10 && this.ySpeed > -10) {
            if (bool) {
              this.ySpeed = -10; break;
            } 
            this.ySpeed = 10;
          } 

          
          break;
        } 
        
        if (b < 0 || (bool && b > 0))
        {


          
          if (bool) {
            this.ySpeed = -20;
          } else {
            this.ySpeed = 20;
          } 
        }
      } 
    } 

    if (bool) {
      
      if (j == -2 && this.ySpeed < k) {
        this.ySpeed += j;
        if (this.ySpeed > k) {
          this.ySpeed = k;
        }
      } else if (!this.mGroundedFlag && this.ySpeed > k) {
        this.ySpeed += j;
        if (this.ySpeed < k) {
          this.ySpeed = k;
        }
      }
    
    }
    else if (j == -2 && this.ySpeed > k) {
      this.ySpeed += j;
      if (this.ySpeed < k) {
        this.ySpeed = k;
      }
    } else if (!this.mGroundedFlag && this.ySpeed < k) {
      this.ySpeed += j;
      if (this.ySpeed > k) {
        this.ySpeed = k;
      }
    } 

    
    if (this.speedBonusCntr != 0) {
      b1 = 100;
      this.speedBonusCntr--;
    } else {
      b1 = 50;
    } 

    
    if ((this.direction & 0x2) != 0 && this.xSpeed < b1) {
      this.xSpeed += 6;
    } else if ((this.direction & 0x1) != 0 && this.xSpeed > -b1) {
      
      this.xSpeed -= 6;
    } else if (this.xSpeed > 0) {
      this.xSpeed -= 4;
    } else if (this.xSpeed < 0) {
      this.xSpeed += 4;
    } 

    if (this.ballSize == 16 && this.jumpBonusCntr == 0) {
      if (bool) {
        this.jumpOffset += 5;
      } else {
        this.jumpOffset += -5;
      } 
    }

    if (this.mGroundedFlag && (this.direction & 0x8) != 0) {
      if (bool) {
        this.ySpeed = 67 + this.jumpOffset;
      } else {
        this.ySpeed = -67 + this.jumpOffset;
      } 
      this.mGroundedFlag = false;
    } 

    int i1 = Math.abs(this.xSpeed);
    int i2 = i1 / 10;
    for (byte b3 = 0; b3 < i2; b3++) {
      byte b = 0;
      if (this.xSpeed != 0) {
        b = (this.xSpeed < 0) ? -1 : 1;
      }
      if (collisionDetection(this.xPos + b, this.yPos)) {
        this.xPos += b;
      } else if (this.mCDRampFlag) {
        this.mCDRampFlag = false;
        byte b4 = 0;
        if (bool) {
          b4 = 1;
        } else {
          b4 = -1;
        } 
        if (collisionDetection(this.xPos + b, this.yPos + b4)) {
          this.xPos += b;
          this.yPos += b4;
        } else if (collisionDetection(this.xPos + b, this.yPos - b4)) {
          this.xPos += b;
          this.yPos -= b4;
        
        }
        else {

          
          this.xSpeed = -(this.xSpeed >> 1);
        } 
      } 
    } 

    
    this.xOffset = this.xPos - i;

    if (this.xPos > 156 + this.ballSize) {
      this.xPos -= 156;


      
      if (this.mCanvas.scrollOffset - 10 > 156 + this.ballSize) {
        this.mCanvas.scrollOffset -= 156;
      }
    } 
    if (this.xPos - this.ballSize < 0) {
      this.xPos += 156;


      
      if (this.mCanvas.scrollOffset - this.ballSize < 10)
      {

        
        this.mCanvas.scrollOffset += 156;
      }
    } 
  }

  public void paint(Graphics paramGraphics) {
    if (this.ballState == 2) {
      
      paramGraphics.drawImage(this.poppedImage, this.xPos - 6, this.yPos - 6, 20);

      
      if (this.xPos > 144) {
        paramGraphics.drawImage(this.poppedImage, this.xPos - 156 - 6, this.yPos - 6, 20);
      }
    } else {
      
      paramGraphics.drawImage(this.ballImage, this.xPos - this.mHalfBallSize, this.yPos - this.mHalfBallSize, 20);

      if (this.xPos > 156 - this.ballSize) {
        paramGraphics.drawImage(this.ballImage, this.xPos - 156 - this.mHalfBallSize, this.yPos - this.mHalfBallSize, 20);
      }
    } 

    
    dirtyTiles();
  }

  public void dirtyTiles() {
    int i = (this.xPos - this.mHalfBallSize) / 12;
    int j = (this.yPos - this.mHalfBallSize) / 12;

    
    int k = (this.xPos - 1 + this.mHalfBallSize) / 12 + 1;
    int m = (this.yPos - 1 + this.mHalfBallSize) / 12 + 1;
    
    if (j < 0) {
      j = 0;
    }
    
    if (m > 8) {
      m = 8;
    }
    
    int n = 0;
    int i1 = 0;

    for (int i2 = i; i2 < k; i2++) {
      for (int i3 = j; i3 < m; i3++) {
        
        if (i2 * 12 >= 156) {
          n = this.mCanvas.tileX + i2 - 13;
          i1 = this.mCanvas.tileY + i3;
        
        }
        else if (this.xPos < this.mCanvas.divisorLine) {
          n = this.mCanvas.tileX + i2;
          i1 = this.mCanvas.tileY + i3;
        } else {
          
          n = this.mCanvas.divTileX - 13 - this.mCanvas.divisorLine / 12 + i2;
          i1 = this.mCanvas.divTileY + i3;
        } 
        
        this.mCanvas.tileMap[i1][n] = (short)(this.mCanvas.tileMap[i1][n] | 0x80);
      } 
    } 
  }
}