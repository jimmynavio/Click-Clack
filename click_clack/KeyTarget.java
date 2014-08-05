package click_clack;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.BasicStroke;
import java.awt.Color;

//Targets that appear slower and contain a letter(key)
public class KeyTarget extends Target{
  private char key;
  private char[] smartKeys = "QWERDF".toCharArray();
  private char[] keys = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  public KeyTarget(int difficulty, boolean moving, boolean smart){
    super(difficulty, moving);

    if(smart){
      key = smartKeys[Framework.random.nextInt(6)];
    }else{
      key = keys[Framework.random.nextInt(26)];
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
