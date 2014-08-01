package click_clack;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Game {
  private Font scoreFont;
  private Font gameOverFont;
  private Font hitStreakFont;
  
  private ArrayList<Target> targets;
  private int targetCount;
  private int lives;
  private int targetsHit;
  private int hitStreak;
  private boolean hit;
  
  private int score;
  private int totalShots;
  private long lastShotTime;    
  private long timeBetweenShots;
  private boolean mouseVisible;

  private URL file;
  private BufferedImage backgroundImg;
  private BufferedImage sightImg;

  private SoundController shotSound;
  private SoundController hitSound;
  private SoundController missSound;
  private SoundController failureSound;
  private SoundController successSound;
  private SoundController upgradeSound;
  private SoundController gameOverSound;
  private SoundController[] streakSounds;
  
  private int sightImgMiddleWidth;
  private int sightImgMiddleHeight;
  private char[] keyStates;

  //Constructor
  //Sets initial values, loads resources, and starts game thread
  public Game(){
    this.keyStates = new char[]{'Q','W','E','R','D','F'};
    Framework.toLoading();
    
    Thread threadForInitGame = new Thread() {
      public void run(){
        initialize();
        loadContent();                      
        Framework.toPlaying();
      }
    };
    threadForInitGame.start();
  }

  //Set initial game values
  private void initialize(){       
    scoreFont = new Font("monospaced", Font.BOLD, Framework.screenY(0.02));
    hitStreakFont = new Font("monospaced", Font.BOLD, Framework.screenY(0.1));
    gameOverFont = new Font("monospaced", Font.BOLD, Framework.screenY(0.08));

    targets = new ArrayList<Target>();
    targetCount = 0;
    lives = 10;
    targetsHit = 0;
    hitStreak = 0;

    score = 0;
    totalShots = 0;
    
    lastShotTime = 0;
    timeBetweenShots = Framework.secInNanosec / 5;
    mouseVisible = true;
  }

  //Load game graphics and sound resources
  private void loadContent(){
    try{
      if(Framework.frameWidth > 1200)
        file = this.getClass().getResource("resources/images/Background.jpg");
      else
        file = this.getClass().getResource("resources/images/BackgroundSmall.jpg");

      backgroundImg = ImageIO.read(file);
      
      file = this.getClass().getResource("resources/images/Cursor.png");
      sightImg = ImageIO.read(file);
      sightImgMiddleWidth = sightImg.getWidth() / 2;
      sightImgMiddleHeight = sightImg.getHeight() / 2;

      shotSound = new SoundController("resources/sounds/Shot.wav", 5, Framework.soundVolume);
      hitSound = new SoundController("resources/sounds/Hit.wav", 5, Framework.soundVolume);
      missSound = new SoundController("cresources/sounds/Miss.wav", 5, Framework.soundVolume);
      failureSound = new SoundController("resources/sounds/Failure.wav", 1, Framework.soundVolume);
      successSound = new SoundController("resources/sounds/Success.wav", 1, Framework.soundVolume);
      upgradeSound = new SoundController("resources/sounds/Upgrade.wav", 5, Framework.soundVolume);
      gameOverSound = new SoundController("resources/sounds/GameOver.wav", 5, Framework.soundVolume);
      
      streakSounds = new SoundController[8];
      String soundPath;
      for(int i=0; i<streakSounds.length; i++){
        soundPath = "resources/sounds/KillStreak"+i+".wav";
        streakSounds[i] = new SoundController(soundPath, 1, Framework.soundVolume);
      }

    }catch (Exception ex){
      ex.printStackTrace();
    }
  }

  public void restartGame(){
    targets.clear();
    
    targetCount = 0;
    lives = 10;
    targetsHit = 0;
    hitStreak = 0;
    
    score = 0;
    totalShots = 0;    
    lastShotTime = 0;
  }

  public void nextRound(){
    targets.clear(); 
    targetCount = 0;
    upgradeSound.playSound();
  }
  
  //Standard game update
  public void updateStandardMode(long gameTime, Point mousePosition){
    if(gameTime <= Framework.secInNanosec*2)
      return;

    if(ghostMouse())
      mouseVisible = false;

    if(targetCount < 50){
      addTarget();
    }else if(targets.size() < 1){
      checkEndurance();
    }

    updateTargets();
    checkMissed();
    
    //CHECK PLAYER
    if(Canvas.mouseButtonState(MouseEvent.BUTTON1)){
      if(!mouseVisible) mouseVisible = true;
      
      if(System.nanoTime() - lastShotTime >= timeBetweenShots){
        int targetIndex = getTarget(mousePosition);
        hitTarget(targetIndex);
      }
    }
  }

  //Key game update
  public void updateKeyMode(long gameTime, Point mousePosition){
    if(gameTime <= Framework.secInNanosec*2)
      return;

    if(ghostMouse())
      mouseVisible = false;

    if(targetCount < 50){
      addKeyTarget();
    }else if(targets.size() < 1){
      checkEndurance();
    }
    
    updateTargets();
    checkMissed();
    
    //CHECK PLAYER
    int keyPressed = getKeyPress();
    if(keyPressed != -1){
      if(!mouseVisible) mouseVisible = true;

      if(System.nanoTime() - lastShotTime >= timeBetweenShots){
        int targetIndex = getKeyTarget(mousePosition, (char)keyPressed);
        hitTarget(targetIndex);
      }
    }    
  }

  //Begin update helper functions///////////////////////////////////////////////
  boolean ghostMouse(){
    return (Framework.ghostMouse && targetCount > 25);      
  }

  void addTarget(){
    if(System.nanoTime() - Target.lastTargetTime >= Target.timeBetween){
      targets.add(new Target(Framework.difficulty, Framework.moving));
      Target.lastTargetTime = System.nanoTime();
      targetCount++;
    }
  }

  void addKeyTarget(){
    if(System.nanoTime() - KeyTarget.lastTargetTime >= KeyTarget.timeBetween){
      targets.add(new KeyTarget(Framework.difficulty, Framework.moving));
      KeyTarget.lastTargetTime = System.nanoTime();
      targetCount++;
    }
  }

  void checkEndurance(){
    if(Framework.endurance && Framework.difficulty < 10){  
      Framework.difficulty++;
      nextRound();
    }else{
      Framework.toGameOver();
      successSound.playSound(); 
    }
  }

  void updateTargets(){
    for(int i = 0; i < targets.size(); i++){
      targets.get(i).update();
      
      if(targets.get(i).getTimeLeft() <= 0){
        targets.remove(i);
        lives--;
        hitStreak = 0;
        missSound.playSound();
      }
    }
  }

  void checkMissed(){
    if(lives <= 0){
      Framework.toGameOver();
      failureSound.playSound();
      gameOverSound.playSound();
    }
  }

  int getKeyPress(){
    for(int i=0; i<keyStates.length; i++){
      if(Canvas.keyboardKeyState(keyStates[i]))
        return keyStates[i];
    }

    return -1;
  }

  int getTarget(Point mousePosition){
    for(int i = 0; i < targets.size(); i++){
      if(targets.get(i).getCircle().contains(mousePosition)){
        return i;           
      }
    }

    return -1;
  }

  int getKeyTarget(Point mousePosition, char keyPressed){
    for(int i = 0; i < targets.size(); i++){
      if(targets.get(i).getCircle().contains(mousePosition)
      && targets.get(i).getKey() == keyPressed)
        return i;
    }

    return -1;
  }

  //Check for a hit target and handle accordingly
  void hitTarget(int targetIndex){
    shotSound.playSound();
    totalShots++;

    if(targetIndex != -1){
      hitSound.playSound();
      
      int points = targets.get(targetIndex).getPoints();
      
      if(ghostMouse())
        points += points/2;

      if(Framework.moving)
        points += points/2;

      score += points + hitStreak;
      
      targetsHit++;
      hitStreak++;
      streakSound();

      targets.remove(targetIndex);
    }else{
      hitStreak = 0;
    }

    lastShotTime = System.nanoTime();
  }

  //Check streak and play correct sound
  void streakSound(){
    switch(hitStreak){
      case   5: streakSounds[0].playSound(); break;
      case  10: streakSounds[1].playSound(); break;
      case  25: streakSounds[2].playSound(); break;
      case  50: streakSounds[3].playSound(); break;
      case  75: streakSounds[4].playSound(); break;
      case 100: streakSounds[5].playSound(); break;
      case 150: streakSounds[6].playSound(); break;
      case 200: streakSounds[7].playSound(); break;
    }
  }
  //End update helper functions/////////////////////////////////////////////////

  //Draw main game screen
  public void draw(Graphics2D g2d, Point mousePosition){
    int textWidth;
    g2d.drawImage(backgroundImg, 0, 0, null);
    
    g2d.setColor(new Color(0, 0, 0, 150));
    g2d.fillRect(0, 0, Framework.frameWidth, Framework.screenY(0.10));

    g2d.setColor(Color.white);
    g2d.setFont(scoreFont);
    g2d.drawString("LIVES: " + lives, 10, Framework.screenY(0.02));
    g2d.drawString("SHOTS: " + totalShots, 10, Framework.screenY(0.04));
    g2d.drawString("HITS: " + targetsHit, 10, Framework.screenY(0.06));
    g2d.drawString("SCORE: " + score, 10, Framework.screenY(0.08));
    
    g2d.setFont(hitStreakFont);
    textWidth = g2d.getFontMetrics(hitStreakFont).stringWidth("+" + hitStreak);
    g2d.drawString("+" + hitStreak, Framework.frameWidth - textWidth - 10, Framework.screenY(0.08));

    g2d.setColor(new Color(0, 0, 0, 150));
    g2d.fillRect(0, Framework.screenY(0.90), Framework.frameWidth, Framework.screenY(0.10));

    g2d.setColor(Color.white);
    g2d.setFont(Framework.mainFont);
    textWidth = g2d.getFontMetrics(Framework.mainFont).stringWidth("DIFFICULTY: " + Framework.difficulty);
    g2d.drawString("DIFFICULTY: " + Framework.difficulty, Framework.frameWidth - textWidth - 10, Framework.screenY(0.98));

    for(int i = targets.size()-1; i >= 0; i--){
      targets.get(i).draw(g2d);
    }

    if(mouseVisible)
      g2d.drawImage(sightImg, mousePosition.x - sightImgMiddleWidth, mousePosition.y - sightImgMiddleHeight, null);
  }
  
  //Draw game over screen
  public void drawGameOver(Graphics2D g2d, Point mousePosition){
    draw(g2d, mousePosition);
    
    g2d.setColor(new Color(20, 0, 0, 150));
    g2d.fillRect(0, 0, Framework.frameWidth, Framework.frameHeight);

    g2d.setFont(gameOverFont);
    g2d.setColor(Color.white);
    g2d.drawString("Game Over", Framework.screenX(0.34)-1, Framework.screenY(0.52)-1);
    g2d.drawString("Game Over", Framework.screenX(0.34)-1, Framework.screenY(0.52)+1);
    g2d.drawString("Game Over", Framework.screenX(0.34)+1, Framework.screenY(0.52)+1);
    g2d.drawString("Game Over", Framework.screenX(0.34)+1, Framework.screenY(0.52)-1);
    g2d.setColor(Color.blue);
    g2d.drawString("Game Over", Framework.screenX(0.34), Framework.screenY(0.52));

    g2d.setFont(Framework.mainFont);
    g2d.setColor(Color.white);
    g2d.drawString(" [ENTER] Restart", 10, Framework.screenY(0.94));
    g2d.drawString("[ESCAPE] Main Menu", 10, Framework.screenY(0.98));
  }
}
