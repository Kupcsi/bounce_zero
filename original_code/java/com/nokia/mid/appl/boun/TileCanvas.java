package com.nokia.mid.appl.boun;

import com.nokia.mid.ui.DirectGraphics;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.FullCanvas;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public abstract class TileCanvas
  extends FullCanvas
{
  public int tileX;
  public int tileY;
  public int divTileX;
  public int divTileY;
  public int divisorLine;
  public int rightDrawEdge;
  public int leftDrawEdge;
  public boolean scrollFlag;
  public int scrollOffset;
  public int mExitPos;
  protected Image buffer;
  public Graphics bufferGraphics = null;
  
  private Image[] tileImages;
  
  private Image tmpTileImage;
  
  private Graphics tmpTileImageG;
  
  Vector hoopImageList;
  
  Vector hoopXPosList;
  
  Vector hoopYPosList;
  
  public int mLevelNum;
  
  public String mLevelNumStr;
  
  public String mLevelCompletedStr;
  
  public boolean mLoadLevelFlag;
  
  public int mStartPosX;
  
  public int mStartPosY;
  
  public int mBallSize;
  
  public int mExitPosX;
  
  public int mExitPosY;
  
  public short[][] tileMap;
  
  public int mTileMapWidth;
  
  public int mTileMapHeight;
  
  public int mTotalNumRings;
  
  public int mNumMoveObj;
  
  public short[][] mMOTopLeft;
  
  public short[][] mMOBotRight;
  
  public short[][] mMODirection;
  
  public short[][] mMOOffset;
  
  public Image[] mMOImgPtr;
  
  public Graphics[] mMOImgGraphics;
  
  public Image mSpikeImgPtr;
  
  public Image mUILife;
  public Image mUIRing;
  public int mTopLeftTileCol;
  public int mTopLeftTileRow;
  public int mBotRightTileCol;
  public int mBotRightTileRow;
  public Image mExitTileImage;
  public Image mImgPtr;
  public int mImageOffset;
  public boolean mOpenFlag;
  protected int mWidth = 0;
  protected int mHeight = 0;
  
  protected Display mDisplay;
  private GameTimer mGameTimer = null;

  public TileCanvas(Display paramDisplay) {
    this.mDisplay = paramDisplay;
    this.mWidth = getWidth();
    this.mHeight = getHeight();

    this.scrollFlag = true;

    this.divisorLine = 156;
    this.rightDrawEdge = 142;
    this.leftDrawEdge = 14;

    
    this.buffer = Image.createImage(156, 96);

    
    this.tmpTileImage = Image.createImage(12, 12);
    this.tmpTileImageG = this.tmpTileImage.getGraphics();

    
    loadTileImages();

    this.mLoadLevelFlag = false;

    this.tileX = 0;
    this.tileY = 0;

    this.mExitPos = -1;
    
    this.divTileX = this.tileX + 13;
    this.divTileY = this.tileY;
    
    this.tileMap = null;

    
    this.hoopImageList = new Vector();
    this.hoopXPosList = new Vector();
    this.hoopYPosList = new Vector();
  }

  public void loadLevel(int paramInt) {
    InputStream inputStream = null;
    DataInputStream dataInputStream = null;
    this.mLoadLevelFlag = false;
    
    String str = "";
    
    String[] arrayOfString = new String[1];
    arrayOfString[0] = (new Integer(this.mLevelNum)).toString();
    this.mLevelNumStr = Local.getText(14, arrayOfString);
    this.mLevelCompletedStr = Local.getText(15, arrayOfString);
    arrayOfString[0] = null;
    arrayOfString = null;
    
    if (paramInt < 10) {
      str = "00" + paramInt;
    }
    else if (paramInt < 100) {
      str = "0" + paramInt;
    } 

    try {
      inputStream = getClass().getResourceAsStream("/levels/J2MElvl." + str);
      dataInputStream = new DataInputStream(inputStream);
      
      this.mStartPosX = dataInputStream.read();
      this.mStartPosY = dataInputStream.read();
      
      this.mBallSize = dataInputStream.read();
      
      this.mExitPosX = dataInputStream.read();
      this.mExitPosY = dataInputStream.read();

      
      createExitTileObject(this.mExitPosX, this.mExitPosY, this.mExitPosX + 1, this.mExitPosY + 2, this.tileImages[12]);

      
      this.mTotalNumRings = dataInputStream.read();

      
      this.mTileMapWidth = dataInputStream.read();
      this.mTileMapHeight = dataInputStream.read();


      
      this.tileMap = new short[this.mTileMapHeight][this.mTileMapWidth];
      
      for (byte b = 0; b < this.mTileMapHeight; b++) {
        for (byte b1 = 0; b1 < this.mTileMapWidth; b1++) {
          this.tileMap[b][b1] = (short)dataInputStream.read();
        }
      } 

      
      this.mNumMoveObj = dataInputStream.read();

      
      if (this.mNumMoveObj != 0) {
        createMovingObj(dataInputStream);
      }
      
      dataInputStream.close();
    } catch (IOException iOException) {
      
      System.out.println("Error trying to read file: " + iOException);
    } 
  }

  public static Image manipulateImage(Image paramImage, int paramInt) {
    Image image = DirectUtils.createImage(paramImage.getWidth(), paramImage.getHeight(), 0);
    if (image == null) {
      image = Image.createImage(paramImage.getWidth(), paramImage.getHeight());
    }

    
    Graphics graphics = image.getGraphics();
    DirectGraphics directGraphics = DirectUtils.getDirectGraphics(graphics);
    
    switch (paramInt)
    { case 0:
        directGraphics.drawImage(paramImage, 0, 0, 20, 8192);

        return image;case 1: directGraphics.drawImage(paramImage, 0, 0, 20, 16384); return image;case 2: directGraphics.drawImage(paramImage, 0, 0, 20, 24576); return image;case 3: directGraphics.drawImage(paramImage, 0, 0, 20, 90); return image;case 4: directGraphics.drawImage(paramImage, 0, 0, 20, 180); return image;case 5: directGraphics.drawImage(paramImage, 0, 0, 20, 270); return image; }  graphics.drawImage(paramImage, 0, 0, 20); return image;
  }

  public void createMovingObj(DataInputStream paramDataInputStream) throws IOException {
    this.mMOTopLeft = new short[this.mNumMoveObj][2];
    this.mMOBotRight = new short[this.mNumMoveObj][2];
    this.mMODirection = new short[this.mNumMoveObj][2];
    this.mMOOffset = new short[this.mNumMoveObj][2];
    this.mMOImgPtr = new Image[this.mNumMoveObj];
    this.mMOImgGraphics = new Graphics[this.mNumMoveObj];

    for (byte b = 0; b < this.mNumMoveObj; b++) {
      
      this.mMOTopLeft[b][0] = (short)paramDataInputStream.read();
      this.mMOTopLeft[b][1] = (short)paramDataInputStream.read();
      
      this.mMOBotRight[b][0] = (short)paramDataInputStream.read();
      this.mMOBotRight[b][1] = (short)paramDataInputStream.read();

      
      this.mMODirection[b][0] = (short)paramDataInputStream.read();
      this.mMODirection[b][1] = (short)paramDataInputStream.read();

      
      int i = paramDataInputStream.read();
      int j = paramDataInputStream.read();
      
      this.mMOOffset[b][0] = (short)i;
      this.mMOOffset[b][1] = (short)j;
    } 

    this.mSpikeImgPtr = Image.createImage(24, 24);
    Graphics graphics = this.mSpikeImgPtr.getGraphics();
    graphics.drawImage(this.tileImages[46], 0, 0, 20);
    graphics.drawImage(manipulateImage(this.tileImages[46], 0), 12, 0, 20);
    graphics.drawImage(manipulateImage(this.tileImages[46], 4), 12, 12, 20);
    graphics.drawImage(manipulateImage(this.tileImages[46], 1), 0, 12, 20);
    graphics = null;
  }
  
  public void disposeLevel() {
    for (byte b = 0; b < this.mNumMoveObj; b++) {
      this.mMOImgPtr[b] = null;
      this.mMOImgGraphics[b] = null;
    } 
    this.mMOImgPtr = null;
    this.mMOImgGraphics = null;
    this.tileMap = null;
    Runtime.getRuntime().gc();
  }

  public void updateMovingSpikeObj() {
    for (byte b = 0; b < this.mNumMoveObj; b++) {
      short s1 = this.mMOTopLeft[b][0];
      short s2 = this.mMOTopLeft[b][1];
      
      short s3 = this.mMOOffset[b][0];
      short s4 = this.mMOOffset[b][1];

      this.mMOOffset[b][0] = (short)(this.mMOOffset[b][0] + this.mMODirection[b][0]);

      
      int n = (this.mMOBotRight[b][0] - s1 - 2) * 12;
      int i1 = (this.mMOBotRight[b][1] - s2 - 2) * 12;
      
      if (this.mMOOffset[b][0] < 0) {
        this.mMOOffset[b][0] = 0;
      } else if (this.mMOOffset[b][0] > n) {
        this.mMOOffset[b][0] = (short)n;
      } 

      
      if (this.mMOOffset[b][0] == 0 || this.mMOOffset[b][0] == n) {
        this.mMODirection[b][0] = (short)-this.mMODirection[b][0];
      }

      
      this.mMOOffset[b][1] = (short)(this.mMOOffset[b][1] + this.mMODirection[b][1]);
      
      if (this.mMOOffset[b][1] < 0) {
        this.mMOOffset[b][1] = 0;
      } else if (this.mMOOffset[b][1] > i1) {
        this.mMOOffset[b][1] = (short)i1;
      } 

      
      if (this.mMOOffset[b][1] == 0 || this.mMOOffset[b][1] == i1) {
        this.mMODirection[b][1] = (short)(this.mMODirection[b][1] * -1);
      }

      
      short s5 = this.mMOOffset[b][0];
      short s6 = this.mMOOffset[b][1];
      
      if (s5 < s3) {
        short s = s5;
        s5 = s3;
        s3 = s;
      } 
      
      if (s6 < s4) {
        short s = s6;
        s6 = s4;
        s4 = s;
      } 
      
      s5 += 23;
      s6 += 23;
      
      int i = s3 / 12;
      int j = s4 / 12;
      
      int k = s5 / 12 + 1;
      int m = s6 / 12 + 1;
      
      for (int i2 = i; i2 < k; i2++) {
        for (int i3 = j; i3 < m; i3++) {
          this.tileMap[s2 + i3][s1 + i2] = (short)(this.tileMap[s2 + i3][s1 + i2] | 0x80);
        }
      } 
    } 
  }

  public int findSpikeIndex(int paramInt1, int paramInt2) {
    for (byte b = 0; b < this.mNumMoveObj; b++) {
      
      if (this.mMOTopLeft[b][0] <= paramInt1 && this.mMOBotRight[b][0] > paramInt1 && this.mMOTopLeft[b][1] <= paramInt2 && this.mMOBotRight[b][1] > paramInt2)
      {
        return b;
      }
    } 
    
    return -1;
  }

  public void drawTile(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    int j, k;
    if (this.bufferGraphics == null) {
      this.bufferGraphics = this.buffer.getGraphics();
    }

    
    if ((this.tileMap[paramInt2][paramInt1] & 0x80) != 0) {
      this.tileMap[paramInt2][paramInt1] = (short)(this.tileMap[paramInt2][paramInt1] & 0xFF7F);
    }
    
    int i = this.tileMap[paramInt2][paramInt1];
    boolean bool = ((i & 0x40) != 0) ? true : false;
    if (bool) {
      i = i & 0xFFFFFFBF;
    }
    this.bufferGraphics.setColor(bool ? 1073328 : 11591920);

    
    switch (i) {
      case 1:
        this.bufferGraphics.drawImage(this.tileImages[0], paramInt3, paramInt4, 20);
        break;
      
      case 0:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        break;
      
      case 2:
        this.bufferGraphics.drawImage(this.tileImages[1], paramInt3, paramInt4, 20);
        break;
      
      case 3:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[6], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[2], paramInt3, paramInt4, 20);
        break;

      
      case 4:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[9], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[5], paramInt3, paramInt4, 20);
        break;

      
      case 5:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[7], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[3], paramInt3, paramInt4, 20);
        break;

      
      case 6:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[8], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[4], paramInt3, paramInt4, 20);
        break;

      
      case 7:
        this.bufferGraphics.drawImage(this.tileImages[10], paramInt3, paramInt4, 20);
        break;
      
      case 8:
        this.bufferGraphics.drawImage(this.tileImages[11], paramInt3, paramInt4, 20);
        break;

      
      case 23:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[13], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[14], paramInt3, paramInt4);
        break;
      case 24:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[15], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[16], paramInt3, paramInt4);
        break;
      
      case 15:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[17], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[18], paramInt3, paramInt4);
        break;
      case 16:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[19], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[20], paramInt3, paramInt4);
        break;
      
      case 27:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[21], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[22], paramInt3, paramInt4);
        break;
      case 28:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[23], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[24], paramInt3, paramInt4);
        break;
      
      case 19:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[25], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[26], paramInt3, paramInt4);
        break;
      case 20:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[27], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[28], paramInt3, paramInt4);
        break;
      
      case 21:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[31], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[29], paramInt3, paramInt4);
        break;
      case 22:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[32], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[30], paramInt3, paramInt4);
        break;
      
      case 13:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[35], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[33], paramInt3, paramInt4);
        break;
      case 14:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[36], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[34], paramInt3, paramInt4);
        break;
      
      case 25:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[39], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[37], paramInt3, paramInt4);
        break;
      case 26:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[40], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[38], paramInt3, paramInt4);
        break;
      
      case 17:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[43], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[41], paramInt3, paramInt4);
        break;
      case 18:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[44], paramInt3, paramInt4, 20);
        add2HoopList(this.tileImages[42], paramInt3, paramInt4);
        break;
      
      case 9:
        j = (paramInt1 - this.mTopLeftTileCol) * 12;
        k = (paramInt2 - this.mTopLeftTileRow) * 12;
        
        this.bufferGraphics.drawImage(this.mExitTileImage, paramInt3 - j, paramInt4 - k, 20);
        this.mExitPos = paramInt3 - j + 12 - 1;
        break;


      
      case 10:
        j = findSpikeIndex(paramInt1, paramInt2);
        
        if (j != -1) {

          k = (paramInt1 - this.mMOTopLeft[j][0]) * 12;
          int m = (paramInt2 - this.mMOTopLeft[j][1]) * 12;
          int n = this.mMOOffset[j][0] - k;
          int i1 = this.mMOOffset[j][1] - m;
          if ((n > -36 && n < 12) || (i1 > -36 && i1 < 12)) {
            
            this.tmpTileImageG.setColor(11591920);
            this.tmpTileImageG.fillRect(0, 0, 12, 12);
            this.tmpTileImageG.drawImage(this.mSpikeImgPtr, n, i1, 20);
            this.bufferGraphics.drawImage(this.tmpTileImage, paramInt3, paramInt4, 20); break;
          } 
          this.bufferGraphics.setColor(11591920);
          this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        } 
        break;


      
      case 29:
        this.bufferGraphics.drawImage(this.tileImages[45], paramInt3, paramInt4, 20);
        break;
      
      case 30:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[61], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[57], paramInt3, paramInt4, 20);
        break;

      
      case 31:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[60], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[56], paramInt3, paramInt4, 20);
        break;

      
      case 32:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[59], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[55], paramInt3, paramInt4, 20);
        break;

      
      case 33:
        if (bool) {
          this.bufferGraphics.drawImage(this.tileImages[62], paramInt3, paramInt4, 20); break;
        } 
        this.bufferGraphics.drawImage(this.tileImages[58], paramInt3, paramInt4, 20);
        break;

      
      case 34:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[65], paramInt3, paramInt4, 20);
        break;
      
      case 35:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[64], paramInt3, paramInt4, 20);
        break;
      
      case 36:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[63], paramInt3, paramInt4, 20);
        break;
      
      case 37:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[66], paramInt3, paramInt4, 20);
        break;
      
      case 39:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[50], paramInt3, paramInt4, 20);
        break;
      
      case 40:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[50], 5), paramInt3, paramInt4, 20);
        break;
      
      case 41:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[50], 4), paramInt3, paramInt4, 20);
        break;
      
      case 42:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[50], 3), paramInt3, paramInt4, 20);
        break;
      
      case 43:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(this.tileImages[51], paramInt3, paramInt4, 20);
        break;
      
      case 44:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[51], 5), paramInt3, paramInt4, 20);
        break;
      
      case 45:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[51], 4), paramInt3, paramInt4, 20);
        break;
      
      case 46:
        this.bufferGraphics.fillRect(paramInt3, paramInt4, 12, 12);
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[51], 3), paramInt3, paramInt4, 20);
        break;
      
      case 47:
        this.bufferGraphics.drawImage(this.tileImages[52], paramInt3, paramInt4, 20);
        break;
      
      case 48:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[52], 5), paramInt3, paramInt4, 20);
        break;
      
      case 49:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[52], 4), paramInt3, paramInt4, 20);
        break;
      
      case 50:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[52], 3), paramInt3, paramInt4, 20);
        break;
      
      case 38:
        this.bufferGraphics.drawImage(this.tileImages[53], paramInt3, paramInt4, 20);
        break;
      
      case 51:
        this.bufferGraphics.drawImage(this.tileImages[54], paramInt3, paramInt4, 20);
        break;
      
      case 52:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[54], 5), paramInt3, paramInt4, 20);
        break;
      
      case 53:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[54], 4), paramInt3, paramInt4, 20);
        break;
      
      case 54:
        this.bufferGraphics.drawImage(manipulateImage(this.tileImages[54], 3), paramInt3, paramInt4, 20);
        break;
    } 
  }

  public void add2HoopList(Image paramImage, int paramInt1, int paramInt2) {
    this.hoopImageList.addElement(paramImage);
    this.hoopXPosList.addElement(new Integer(paramInt1));
    this.hoopYPosList.addElement(new Integer(paramInt2));
  }

  public void createNewBuffer() {
    for (byte b = 0; b < 13; b++) {
      for (byte b1 = 0; b1 < 8; b1++) {
        drawTile(this.tileX + b, this.tileY + b1, b * 12, b1 * 12);
      }
    } 
  }

  public void cleanBuffer(boolean paramBoolean) {
    int i = this.tileX;
    int j = this.tileY;
    
    for (byte b = 0; b < 13; b++) {
      
      if (b * 12 >= this.divisorLine && i >= this.tileX) {
        i = this.divTileX - 13;
      }
      
      for (byte b1 = 0; b1 < 8; b1++) {
        
        if ((this.tileMap[j][i] & 0x80) != 0) {
          this.tileMap[j][i] = (short)(this.tileMap[j][i] & 0xFF7F);

          
          if (paramBoolean) {
            drawTile(i, j, b * 12, b1 * 12);
          }
        } 
        
        j++;
      } 
      
      j = this.tileY;
      i++;
    } 
  }

  public void scrollBuffer(int paramInt1, int paramInt2, int paramInt3) {
    if (this.rightDrawEdge < 0) {
      this.rightDrawEdge += 156;
    }

    
    if (this.rightDrawEdge > this.divisorLine && this.rightDrawEdge <= this.divisorLine + 12) {
      
      if (this.tileX + this.divisorLine / 12 >= this.mTileMapWidth) {
        
        this.leftDrawEdge -= paramInt2;
        this.rightDrawEdge -= paramInt2;
        
        if (this.rightDrawEdge < 0) {
          this.rightDrawEdge += 156;
        }

        
        if (this.scrollFlag) {
          this.scrollFlag = false;
          this.scrollOffset = this.rightDrawEdge - 64;
          if (this.scrollOffset < paramInt3) {
            this.scrollOffset += 156;
          }
        }
      
      } else {
        
        if (this.divisorLine >= 156) {
          this.divisorLine = 0;
          this.tileX += 13;
        } 

        
        if (this.rightDrawEdge >= 156) {
          this.rightDrawEdge -= 156;
        }
        
        int i = this.divisorLine;
        this.divisorLine += 12;
        this.divTileX++;

        
        for (byte b = 0; b < 8; b++) {
          drawTile(this.tileX + i / 12, this.tileY + b, i, b * 12);
        }
      }
    
    }
    else if (this.rightDrawEdge > 156) {
      this.rightDrawEdge -= 156;
    } 

    
    if (this.leftDrawEdge >= 156) {
      this.leftDrawEdge -= 156;
    }
    
    if (this.leftDrawEdge < 0) {
      this.leftDrawEdge += 156;
    }

    
    if (this.leftDrawEdge < this.divisorLine && this.leftDrawEdge >= this.divisorLine - 12)
    {
      if (this.tileX - 13 - this.divisorLine / 12 <= 0) {

        
        this.leftDrawEdge -= paramInt2;
        this.rightDrawEdge -= paramInt2;
        
        if (this.leftDrawEdge >= 156) {
          this.leftDrawEdge -= 156;
        }

        
        if (this.scrollFlag) {
          this.scrollFlag = false;
          this.scrollOffset = (this.leftDrawEdge + 64) % 156;
          
          if (this.scrollOffset < paramInt3) {
            this.scrollOffset += 156;

          
          }

        
        }

      
      }
      else {

        
        this.divisorLine -= 12;
        int i = this.divisorLine;
        this.divTileX--;

        
        if (this.divisorLine <= 0) {
          this.divisorLine = 156;
          this.tileX -= 13;
        } 

        
        for (byte b = 0; b < 8; b++) {
          drawTile(this.divTileX - 13, this.divTileY + b, i, b * 12);
        }
      } 
    }
  }

  void testScroll(int paramInt1, int paramInt2) {
    if (!this.scrollFlag) {

      
      if (this.tileX - 13 - this.divisorLine / 12 <= 0 && paramInt1 >= this.scrollOffset && paramInt1 < this.scrollOffset + 10) {


        
        this.scrollFlag = true;
        paramInt2 = paramInt1 - this.scrollOffset;
      } 

      
      if (this.tileX + this.divisorLine / 12 >= this.mTileMapWidth && paramInt1 <= this.scrollOffset && paramInt1 > this.scrollOffset - 10) {




        
        this.scrollFlag = true;
        paramInt2 = paramInt1 - this.scrollOffset;
      } 
    } 

    
    if (this.scrollFlag) {
      this.leftDrawEdge += paramInt2;
      this.rightDrawEdge += paramInt2;
    } 
  }

  public Image createLargeBallImage(Image paramImage) {
    Image image = DirectUtils.createImage(16, 16, 0);
    if (image == null) {
      image = Image.createImage(16, 16);
    }
    
    Graphics graphics = image.getGraphics();
    DirectGraphics directGraphics = DirectUtils.getDirectGraphics(graphics);
    
    graphics.drawImage(paramImage, -4, -4, 20);
    directGraphics.drawImage(paramImage, 8, -4, 20, 8192);
    directGraphics.drawImage(paramImage, -4, 8, 20, 16384);
    directGraphics.drawImage(paramImage, 8, 8, 20, 180);
    
    return image;
  }

  public Image createExitImage(Image paramImage) {
    Image image = Image.createImage(24, 48);
    
    Graphics graphics = image.getGraphics();
    
    graphics.setColor(11591920);
    graphics.fillRect(0, 0, 24, 48);

    
    graphics.setColor(16555422);
    graphics.fillRect(4, 0, 16, 48);

    
    graphics.setColor(14891583);
    graphics.fillRect(6, 0, 10, 48);

    
    graphics.setColor(12747918);
    graphics.fillRect(10, 0, 4, 48);
    
    graphics.drawImage(paramImage, 0, 0, 20);
    graphics.drawImage(manipulateImage(paramImage, 0), 12, 0, 20);
    graphics.drawImage(manipulateImage(paramImage, 1), 0, 12, 20);
    graphics.drawImage(manipulateImage(paramImage, 2), 12, 12, 20);
    
    return image;
  }

  public void loadTileImages() {
    Image image = loadImage("/icons/objects_nm.png");
    this.tileImages = new Image[67];

    
    this.tileImages[0] = extractImage(image, 1, 0);
    this.tileImages[1] = extractImage(image, 1, 2);

    
    this.tileImages[2] = extractImageBG(image, 0, 3, -5185296);
    this.tileImages[3] = manipulateImage(this.tileImages[2], 1);
    this.tileImages[4] = manipulateImage(this.tileImages[2], 3);
    this.tileImages[5] = manipulateImage(this.tileImages[2], 5);
    
    this.tileImages[6] = extractImageBG(image, 0, 3, -15703888);
    this.tileImages[7] = manipulateImage(this.tileImages[6], 1);
    this.tileImages[8] = manipulateImage(this.tileImages[6], 3);
    this.tileImages[9] = manipulateImage(this.tileImages[6], 5);

    
    this.tileImages[10] = extractImage(image, 0, 4);
    this.tileImages[11] = extractImage(image, 3, 4);

    
    this.tileImages[12] = createExitImage(extractImage(image, 2, 3));



    
    this.tileImages[14] = extractImage(image, 0, 5);
    this.tileImages[13] = manipulateImage(this.tileImages[14], 1);
    this.tileImages[15] = manipulateImage(this.tileImages[13], 0);
    this.tileImages[16] = manipulateImage(this.tileImages[14], 0);
    
    this.tileImages[18] = extractImage(image, 1, 5);
    this.tileImages[17] = manipulateImage(this.tileImages[18], 1);
    this.tileImages[19] = manipulateImage(this.tileImages[17], 0);
    this.tileImages[20] = manipulateImage(this.tileImages[18], 0);
    
    this.tileImages[22] = extractImage(image, 2, 5);
    this.tileImages[21] = manipulateImage(this.tileImages[22], 1);
    this.tileImages[23] = manipulateImage(this.tileImages[21], 0);
    this.tileImages[24] = manipulateImage(this.tileImages[22], 0);
    
    this.tileImages[26] = extractImage(image, 3, 5);
    this.tileImages[25] = manipulateImage(this.tileImages[26], 1);
    this.tileImages[27] = manipulateImage(this.tileImages[25], 0);
    this.tileImages[28] = manipulateImage(this.tileImages[26], 0);
    
    this.tileImages[29] = manipulateImage(this.tileImages[14], 5);
    this.tileImages[30] = manipulateImage(this.tileImages[29], 1);
    this.tileImages[31] = manipulateImage(this.tileImages[29], 0);
    this.tileImages[32] = manipulateImage(this.tileImages[30], 0);
    
    this.tileImages[33] = manipulateImage(this.tileImages[18], 5);
    this.tileImages[34] = manipulateImage(this.tileImages[33], 1);
    this.tileImages[35] = manipulateImage(this.tileImages[33], 0);
    this.tileImages[36] = manipulateImage(this.tileImages[34], 0);
    
    this.tileImages[37] = manipulateImage(this.tileImages[22], 5);
    this.tileImages[38] = manipulateImage(this.tileImages[37], 1);
    this.tileImages[39] = manipulateImage(this.tileImages[37], 0);
    this.tileImages[40] = manipulateImage(this.tileImages[38], 0);
    
    this.tileImages[41] = manipulateImage(this.tileImages[26], 5);
    this.tileImages[42] = manipulateImage(this.tileImages[41], 1);
    this.tileImages[43] = manipulateImage(this.tileImages[41], 0);
    this.tileImages[44] = manipulateImage(this.tileImages[42], 0);

    
    this.tileImages[45] = extractImage(image, 3, 3);
    
    this.tileImages[46] = extractImage(image, 1, 3);

    
    this.tileImages[47] = extractImage(image, 2, 0);
    this.tileImages[48] = extractImage(image, 0, 1);
    this.tileImages[49] = createLargeBallImage(extractImage(image, 3, 0));

    
    this.tileImages[50] = extractImage(image, 3, 1);
    this.tileImages[51] = extractImage(image, 2, 4);
    this.tileImages[52] = extractImage(image, 3, 2);
    this.tileImages[53] = extractImage(image, 1, 1);
    this.tileImages[54] = extractImage(image, 2, 2);

    
    this.tileImages[55] = extractImageBG(image, 0, 0, -5185296);
    this.tileImages[56] = manipulateImage(this.tileImages[55], 3);
    this.tileImages[57] = manipulateImage(this.tileImages[55], 4);
    this.tileImages[58] = manipulateImage(this.tileImages[55], 5);
    
    this.tileImages[59] = extractImageBG(image, 0, 0, -15703888);
    this.tileImages[60] = manipulateImage(this.tileImages[59], 3);
    this.tileImages[61] = manipulateImage(this.tileImages[59], 4);
    this.tileImages[62] = manipulateImage(this.tileImages[59], 5);
    
    this.tileImages[63] = extractImage(image, 0, 2);
    this.tileImages[64] = manipulateImage(this.tileImages[63], 3);
    this.tileImages[65] = manipulateImage(this.tileImages[63], 4);
    this.tileImages[66] = manipulateImage(this.tileImages[63], 5);

    
    this.mUILife = extractImage(image, 2, 1);
    this.mUIRing = extractImage(image, 1, 4);
  }

  public void setBallImages(Ball paramBall) {
    paramBall.smallBallImage = this.tileImages[47];
    paramBall.poppedImage = this.tileImages[48];
    paramBall.largeBallImage = this.tileImages[49];
  }

  public static Image extractImage(Image paramImage, int paramInt1, int paramInt2) {
    return extractImageBG(paramImage, paramInt1, paramInt2, 0);
  }

  public static Image extractImageBG(Image paramImage, int paramInt1, int paramInt2, int paramInt3) {
    Image image = DirectUtils.createImage(12, 12, paramInt3);


    
    if (image == null) {
      image = Image.createImage(12, 12);
      Graphics graphics1 = image.getGraphics();
      graphics1.setColor(paramInt3);
      graphics1.fillRect(0, 0, 12, 12);
    } 
    Graphics graphics = image.getGraphics();
    graphics.drawImage(paramImage, -paramInt1 * 12, -paramInt2 * 12, 20);
    return image;
  }

  public static Image loadImage(String paramString) {
    Image image = null;
    
    try {
      image = Image.createImage(paramString);
    }
    catch (IOException iOException) {}


    
    return image;
  }

  public Image getImage(int paramInt) {
    if (paramInt < 67) {
      return this.tileImages[paramInt];
    }
    
    return null;
  }

  public void createExitTileObject(int paramInt1, int paramInt2, int paramInt3, int paramInt4, Image paramImage) {
    this.mTopLeftTileCol = paramInt1;
    this.mTopLeftTileRow = paramInt2;
    
    this.mBotRightTileCol = paramInt3;
    this.mBotRightTileRow = paramInt4;
    
    this.mImgPtr = paramImage;

    
    this.mExitTileImage = Image.createImage(24, 24);
    this.mImageOffset = 0;
    repaintExitTile();
    
    this.mOpenFlag = false;
  }

  public void repaintExitTile() {
    Graphics graphics = this.mExitTileImage.getGraphics();
    
    graphics.drawImage(this.mImgPtr, 0, 0 - this.mImageOffset, 20);
  }

  public void openExit() {
    this.mImageOffset += 4;
    
    if (this.mImageOffset >= 24) {
      this.mImageOffset = 24;
      this.mOpenFlag = true;
    } 
    
    repaintExitTile();
  }

  public static boolean rectCollide(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, int paramInt7, int paramInt8) {
    if (paramInt1 <= paramInt7 && paramInt2 <= paramInt8 && paramInt5 <= paramInt3 && paramInt6 <= paramInt4)
    {
      return true;
    }
    
    return false;
  }

  public abstract void run();
  
  public synchronized void start() {
    if (this.mGameTimer != null) {
      return;
    }
    
    this.mGameTimer = new GameTimer(this, this);
  }
  
  public synchronized void stop() {
    if (this.mGameTimer == null) {
      return;
    }
    
    this.mGameTimer.stop();
    this.mGameTimer = null;
  }

  protected void timerTrigger() {
    run();
  }

  
  protected class GameTimer
    extends TimerTask
  {
    TileCanvas parent;
    Timer timer;
    private final TileCanvas this$0;
    
    public GameTimer(TileCanvas this$0, TileCanvas param1TileCanvas1) {
      this.this$0 = this$0;
      this.parent = param1TileCanvas1;
      
      this.timer = new Timer();
      this.timer.schedule(this, 0L, 30L);
    }

    public void run() {
      this.parent.timerTrigger();
    }

    void stop() {
      if (this.timer == null) {
        return;
      }
      
      cancel();
      this.timer.cancel();
      this.timer = null;
    }
  }
}