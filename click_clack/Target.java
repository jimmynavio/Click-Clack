package click_clack;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.BasicStroke;
import java.awt.Color;

public class Target{
  protected int difficulty;
  protected int diameter;
  protected int radius;
  protected int x;
  protected int y;

  protected int xDirection;
  protected int yDirection;
  protected boolean moving; 

  protected Ellipse2D.Double circle;
  protected Color color;

  protected int duration;
  protected int timeLeft;
  protected int points, startPoints;

  //Constructor, set initial values relative to difficulty
  public Target(int difficulty, boolean moving){
    this.difficulty = difficulty;
    this.diameter = Framework.screenY(0.2) - (difficulty*10);
    this.radius = (int)(diameter/2);
    this.x = Framework.random.nextInt(Framework.frameWidth - diameter);
    this.y = Framework.random.nextInt(Framework.frameHeight - diameter);
    
    this.xDirection = Framework.random.nextInt(2)*2 -1;
    this.yDirection = Framework.random.nextInt(2)*2 -1;
    this.moving = moving;
    
    this.color = Framework.INITIAL_COLOR;
    this.circle = new Ellipse2D.Double(x, y, diameter, diameter);

    this.duration = this.timeLeft = 1500/difficulty;
    this.points = this.startPoints = 10*difficulty;
  }
  
  //Reduce color & point value of target over time
  public void update(){      
    int timeDifference = duration - timeLeft;
    float timeRatio;

    if(duration != 0)
      timeRatio = (float) timeDifference / duration;
    else
      timeRatio = 0;

    int newRed   = (int)((Framework.DELTA_RED * timeRatio) + Framework.INITIAL_COLOR.getRed());
    int newGreen = (int)((Framework.DELTA_GREEN * timeRatio) + Framework.INITIAL_COLOR.getGreen());
    int newBlue  = (int)((Framework.DELTA_BLUE * timeRatio) + Framework.INITIAL_COLOR.getBlue());
    color = new Color(newRed, newGreen, newBlue);
    
    points = (int)(startPoints - (timeRatio*startPoints) + 1);
    timeLeft--;

    if(moving){
      if(x > Framework.frameWidth - diameter || x < 1)
        xDirection *= -1;
      if(y > Framework.frameHeight - diameter || y < 1)
        yDirection *= -1;

      x += xDirection;
      y += yDirection;

      circle.x = x;
      circle.y = y;
    }
  }

  public void draw(Graphics2D g2d){
    g2d.setColor(color);
    g2d.fill(circle);
    g2d.setColor(Color.WHITE);
    g2d.setStroke(new BasicStroke(5));
    g2d.draw(circle);
  }

  public Ellipse2D.Double getCircle() {return circle;}
  public int getTimeLeft(){return timeLeft;}
  public int getPoints(){return points;}
  public char getKey(){return 'X';}
}
