package click_clack;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.imageio.ImageIO;

//Framework handles gamestate/draw loops and all global variables
public class Framework extends Canvas {
  public static int frameWidth;
  public static int frameHeight;

  public static final long secInNanosec = 1000000000L;
  public static final long milisecInNanosec = 1000000L;
  
  private final int GAME_FPS = 300;
  private final long GAME_UPDATE_PERIOD = secInNanosec / GAME_FPS;

  public static enum GameState{STARTING, VISUALIZING, GAME_CONTENT_LOADING, MAIN_MENU, PLAYING, GAMEOVER, DESTROYED}
  public static GameState gameState;
  public static Font mainFont;
  public static Random random = new Random();
  
  public static Color INITIAL_COLOR = new Color(0, 0, 255);
  public static Color FINAL_COLOR = new Color(0, 0, 0);
  public static int DELTA_RED = FINAL_COLOR.getRed() - INITIAL_COLOR.getRed();
  public static int DELTA_GREEN = FINAL_COLOR.getGreen() - INITIAL_COLOR.getGreen();
  public static int DELTA_BLUE = FINAL_COLOR.getBlue() - INITIAL_COLOR.getBlue();

  public static int targetType;
  public static int difficulty;
  public static boolean endurance;
  public static boolean moving;
  public static boolean ghostMouse;

  public static float soundVolume = -20.0f;
  
  private long gameTime;
  private long lastTime;  
  private Game game;

  private URL file;
  private BufferedImage menuImg;
  private BufferedImage[] loadingImgs;
  private int loadNum;

  static SoundController introSound;
  static SoundController gameBeginSound;
  static SoundController optionSound;
  static SoundController menuMusic;
  static SoundController gameMusic; 
  
  //Constructor
  public Framework (){
    super();        
    toVisualizing();

    Thread gameThread = new Thread() {
      public void run(){
        gameLoop();
      }
    };
    gameThread.start();
  }
  
  private void initialize(){
    targetType = 1;
    difficulty = 1;
    endurance = false;
  }

  //Load main menu graphics and sound resources
  private void loadContent(){
    try{
      if(frameWidth > 1200)
        file = this.getClass().getResource("resources/images/MainMenu.png");
      else
        file = this.getClass().getResource("resources/images/MainMenuSmall.png");

      menuImg = ImageIO.read(file);

      loadingImgs = new BufferedImage[8];
      String imagePath;
      
      for(int i=0; i<loadingImgs.length; i++){
        imagePath = "resources/images/loading0"+i+".jpg";
        file = this.getClass().getResource(imagePath);
        loadingImgs[i] = ImageIO.read(file);
      }
      
      menuMusic = new SoundController("resources/sounds/MainShort.wav", 1, soundVolume);
      gameBeginSound = new SoundController("resources/sounds/ShootTheTargets.wav", 1, soundVolume);
      optionSound = new SoundController("resources/sounds/Shot.wav", 5, soundVolume);
      gameMusic = new SoundController("resources/sounds/GamePlay.wav", 1, soundVolume);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  
  //Main gamestate loop for repeat operations
  //For one time operations use transition functions
  private void gameLoop(){
    long visualizingTime = 0, lastVisualizingTime = System.nanoTime();
    long beginTime, timeTaken, timeLeft;
    
    while(true){
      beginTime = System.nanoTime();
      
      switch (gameState){
        case PLAYING:
          gameTime += System.nanoTime() - lastTime;
          
          //Check target type and call correct update mode
          if(targetType == 1)
            game.updateStandardMode(gameTime, mousePosition());
          if(targetType == 2)
            game.updateKeyMode(gameTime, mousePosition());
          
          lastTime = System.nanoTime();
          break;
        case GAMEOVER:
          break;
        case MAIN_MENU:
          break;
        case GAME_CONTENT_LOADING:
          break;
        case STARTING:
          initialize();
          loadContent();
          toMainMenu();
          break;
        case VISUALIZING:
          if(this.getWidth() > 1 && visualizingTime > secInNanosec){
            frameWidth = this.getWidth();
            frameHeight = this.getHeight();
            
            toStarting();
          }else{
            visualizingTime += System.nanoTime() - lastVisualizingTime;
            lastVisualizingTime = System.nanoTime();
          }
          
          mainFont = new Font("monospaced", Font.BOLD, screenY(0.04));
          break;
      }
      repaint();
      
      //MATCH GAME_FPS
      timeTaken = System.nanoTime() - beginTime;
      timeLeft = (GAME_UPDATE_PERIOD - timeTaken) / milisecInNanosec;
      if(timeLeft < 5) 
        timeLeft = 5;
      try{
        Thread.sleep(timeLeft);
      }catch(InterruptedException ex){}
    }
  }
  
  //The draw loop - use for drawing only
  //Use transition functions and gamestate loop if its not graphics related
  public void draw(Graphics2D g2d){
    switch (gameState){
      case PLAYING:
        game.draw(g2d, mousePosition());
        break;
      case GAMEOVER:
        game.drawGameOver(g2d, mousePosition());
        break;
      case MAIN_MENU:
        g2d.drawImage(menuImg, 0, 0, null);

        g2d.setFont(mainFont);
        g2d.setColor(Color.white);        
        
        g2d.drawString("[1-10] Difficulty:  " + (endurance? "X": difficulty), 10, screenY(0.44));
        g2d.drawString("   [E] Endurance:   " + (endurance? "ON": "OFF"), 10, screenY(0.48));
        g2d.drawString("   [M] Moving:      " + (moving? "ON": "OFF"), 10, screenY(0.52));
        g2d.drawString("   [G] Ghost Mouse: " + (ghostMouse? "ON": "OFF"), 10, screenY(0.56));
        if(targetType == 1){
          g2d.drawString("   [T] Target Type: Standard", 10, screenY(0.60));
          g2d.drawString("Place the cross-hair over the target.", 10, screenY(0.68));
          g2d.drawString("Press the left mouse button to shoot.", 10, screenY(0.72));
        }
        if(targetType == 2){
          g2d.drawString("   [T] Target Type: Smart Casting", 10, screenY(0.60));
          g2d.drawString("Place the cross-hair over the target.", 10, screenY(0.68));
          g2d.drawString("Press the correct key on the keyboard to shoot.", 10, screenY(0.72));
        }

        g2d.drawString(" [ENTER] Start Game", 10, screenY(0.94));
        g2d.drawString("[ESCAPE] Exit Game", 10, screenY(0.98));
        
        String volumeLevel = "";
        switch((int)soundVolume){
          case -40: volumeLevel = "[-]|    [+] Volume"; break;
          case -30: volumeLevel = "[-] |   [+] Volume"; break;
          case -20: volumeLevel = "[-]  |  [+] Volume"; break;
          case -10: volumeLevel = "[-]   | [+] Volume"; break;
          case   0: volumeLevel = "[-]    |[+] Volume"; break;
        }

        int textWidth = g2d.getFontMetrics(mainFont).stringWidth(volumeLevel);
        g2d.drawString(volumeLevel, frameWidth - textWidth - 10, screenY(0.98)); 
        break;
      case GAME_CONTENT_LOADING:
        g2d.drawImage(loadingImgs[random.nextInt(8)], 0, 0, null);
      case STARTING:
        g2d.setFont(mainFont);

        g2d.setColor(Color.white);
        g2d.drawString("LOADING...", 10-1, screenY(0.98)-1);
        g2d.drawString("LOADING...", 10-1, screenY(0.98)+1);
        g2d.drawString("LOADING...", 10+1, screenY(0.98)+1);
        g2d.drawString("LOADING...", 10+1, screenY(0.98)-1);
        g2d.setColor(Color.blue);
        g2d.drawString("LOADING...", 10, screenY(0.98));
        break;
    }
  }
  
  //Begin transition functions//////////////////////////////////////////////////
  //Used for 1 time operations between states, like starting/stoping music
  public static void toVisualizing(){
    gameState = GameState.VISUALIZING;
  }

  public static void toStarting(){
    gameState = GameState.STARTING;
  }

  public static void toLoading(){
    gameState = GameState.GAME_CONTENT_LOADING;
  }

  public static void toMainMenu(){
    gameMusic.stopLoop();
    menuMusic.startLoop(true);

    if(endurance) difficulty = 1;
    gameState = GameState.MAIN_MENU;
  }

  public static void toGameOver(){
    gameMusic.stopLoop();

    gameState = Framework.GameState.GAMEOVER;
  }

  public static void toPlaying(){
    menuMusic.stopLoop();
    gameMusic.startLoop(true);
    gameBeginSound.playSound();

    gameState = Framework.GameState.PLAYING;
  }
  //End transition functions////////////////////////////////////////////////////

  private void newGame(){
    gameTime = 0;
    lastTime = System.nanoTime();

    loadNum = random.nextInt(8);
    game = new Game();
  }
  
  private void restartGame(){
    gameTime = 0;
    lastTime = System.nanoTime();  
    if(endurance) difficulty = 1;
    
    game.restartGame();
    toPlaying();
  }
  
  private Point mousePosition(){
    try{
      Point mp = this.getMousePosition();
      
      if(mp != null)
        return this.getMousePosition();
      else
        return new Point(0, 0);
    }
    catch (Exception e){
      return new Point(0, 0);
    }
  }
  
  //Key listener for main menu only
  public void keyReleasedFramework(KeyEvent e){
    int tempKey = e.getKeyCode();
    switch (gameState){
      case GAMEOVER:
        if(tempKey == KeyEvent.VK_ESCAPE) toMainMenu();     
        if(tempKey == KeyEvent.VK_ENTER) restartGame();
        break;
      case PLAYING:
        if(tempKey == KeyEvent.VK_ESCAPE) toMainMenu();
        break;
      case MAIN_MENU:        
        optionSound.playSound();

        if(tempKey == KeyEvent.VK_ENTER) newGame();
        if(tempKey == KeyEvent.VK_ESCAPE) System.exit(0);
        if(tempKey == KeyEvent.VK_E) endurance = endurance? false: true;
        if(tempKey == KeyEvent.VK_M) moving = moving? false: true;
        if(tempKey == KeyEvent.VK_G) ghostMouse = ghostMouse? false: true;
        if(tempKey == KeyEvent.VK_T) targetType = targetType<2? targetType+1: 1;
        if(tempKey == KeyEvent.VK_ADD){
          if(soundVolume < 0.0f){
            soundVolume += 10.0f;
            
            //introSound.setVolume(soundVolume);
            optionSound.setVolume(soundVolume);
            menuMusic.setVolume(soundVolume);
            gameMusic.setVolume(soundVolume);
            gameBeginSound.setVolume(soundVolume);
          }
        }

        if(tempKey == KeyEvent.VK_SUBTRACT){
          if(soundVolume > -40.0f){
            soundVolume -= 10.0f;
            
            //introSound.setVolume(soundVolume);
            optionSound.setVolume(soundVolume);
            menuMusic.setVolume(soundVolume);
            gameMusic.setVolume(soundVolume);
            gameBeginSound.setVolume(soundVolume);
          }
        }     

        if(endurance){
          difficulty = 1;
        }else{
          switch(tempKey){
            case KeyEvent.VK_1: difficulty =  1; break;
            case KeyEvent.VK_2: difficulty =  2; break;
            case KeyEvent.VK_3: difficulty =  3; break;
            case KeyEvent.VK_4: difficulty =  4; break;
            case KeyEvent.VK_5: difficulty =  5; break;
            case KeyEvent.VK_6: difficulty =  6; break;
            case KeyEvent.VK_7: difficulty =  7; break;
            case KeyEvent.VK_8: difficulty =  8; break;
            case KeyEvent.VK_9: difficulty =  9; break;
            case KeyEvent.VK_0: difficulty = 10; break;
          }
        }
        break;
    }
  }

  //Returns int based on percentage of screen width
  static int screenX(double percent){
    return (int)(Framework.frameWidth * percent);
  }

  //Returns int based on percentage of screen height
  static int screenY(double percent){
    return (int)(Framework.frameHeight * percent);
  }
}
