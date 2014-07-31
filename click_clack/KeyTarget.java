package click_clack;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.BasicStroke;
import java.awt.Color;

//Targets that appear slower and contain a letter(key)
public class KeyTarget extends Target{
  public static long timeBetween = Framework.secInNanosec / 1;
  private char key;

  public KeyTarget(int difficulty, boolean moving){
    super(difficulty, moving);

    switch(Framework.random.nextInt(6)){
      case 0:
        key = 'Q';
        break;
      case 1:
        key = 'W';
        break;
      case 2:
        key = 'E';
        break;
      case 3:
        key = 'R';
        break;
      case 4:
        key = 'D'; 
        break;
      case 5:
        key = 'F';
        break;
    }
  }
  
  public void update(){      
    super.update();
  }
  
  public void draw(Graphics2D g2d){
    super.draw(g2d);
    g2d.setFont(Framework.mainFont);
    g2d.drawString(""+key, x+radius-9, y+radius+9);
  }

  public char getKey(){return key;}
}
