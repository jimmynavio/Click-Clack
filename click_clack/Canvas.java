package click_clack;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

//Abstract class used by Framework
//Sets up main graphics canvas and keyboard/mouse listeners
public abstract class Canvas extends JPanel implements KeyListener, MouseListener {
  private static boolean[] keyboardState = new boolean[526];
  private static boolean[] mouseState = new boolean[3];
      
  public Canvas(){
    this.setDoubleBuffered(true);
    this.setFocusable(true);
    this.setBackground(Color.black);
    
    if(true){
      BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
      Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankCursorImg, new Point(0, 0), null);
      this.setCursor(blankCursor);
    }
    
    this.addKeyListener(this);
    this.addMouseListener(this);
  }
  
  public abstract void draw(Graphics2D g2d);
  
  public void paintComponent(Graphics g){
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);      
    super.paintComponent(g2d);        
    draw(g2d);
  }
     
  public static boolean keyboardKeyState(int key){
    return keyboardState[key];
  }
  
  public void keyPressed(KeyEvent e) {
    keyboardState[e.getKeyCode()] = true;
  }
  
  public void keyReleased(KeyEvent e){
    keyboardState[e.getKeyCode()] = false;
    keyReleasedFramework(e);
  }
  
  public void keyTyped(KeyEvent e) {}    
  public abstract void keyReleasedFramework(KeyEvent e);

  public static boolean mouseButtonState(int button){
    return mouseState[button - 1];
  }
  
  private void mouseKeyStatus(MouseEvent e, boolean status){
    if(e.getButton() == MouseEvent.BUTTON1)
      mouseState[0] = status;
    else if(e.getButton() == MouseEvent.BUTTON2)
      mouseState[1] = status;
    else if(e.getButton() == MouseEvent.BUTTON3)
      mouseState[2] = status;
  }
  
  public void mousePressed(MouseEvent e){
    mouseKeyStatus(e, true);
  }

  public void mouseReleased(MouseEvent e){
    mouseKeyStatus(e, false);
  }
  
  public void mouseClicked(MouseEvent e){}
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
}
