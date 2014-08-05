package click_clack;

import java.net.URL;
import javax.sound.sampled.*;

public class SoundController{
  private AudioInputStream audioIn;
  private Clip[] clips;
  private FloatControl[] controls;
  private URL soundFile;
  int count, currentClip;
  FloatControl gainControl;

  //Constructor
  //Creates an array of the same sound
  //Necessary for repeating sounds (see playSound)
  public SoundController(String filePath, int count, float volume){
    AudioSystem.getAudioFileTypes();
    try{
      clips = new Clip[count];
      controls = new FloatControl[count];
      soundFile = this.getClass().getResource(filePath);

      for(int i=0; i<count; i++){
        audioIn = AudioSystem.getAudioInputStream(soundFile);
        clips[i] = AudioSystem.getClip();
        clips[i].open(audioIn);
        
        controls[i] = (FloatControl)clips[i].getControl(FloatControl.Type.MASTER_GAIN);
        controls[i].setValue(volume);
      }
    }catch(Exception ex){}

    this.currentClip = 0;
    this.count = count;
  }

  public void setVolume(float volume){
    for(int i=0; i<count; i++){
      controls[i].setValue(volume);
    }
  }
  
  //Cycles through array to play sounds
  //Necessary if same sound will play more than once & overlap
  void playSound(){
    if(clips[currentClip].isRunning()){
      clips[currentClip].stop();
    }
    clips[currentClip].setFramePosition(0);
    clips[currentClip].start();

    currentClip++;

    if(currentClip >= count)
      currentClip = 0;   
  }

  //Plays sound in a loop, used primarily for music
  void startLoop(boolean restart){
    if(clips[currentClip].isRunning())
      clips[currentClip].stop(); 

    if(restart)
      clips[currentClip].setFramePosition(0);

    clips[currentClip].loop(Clip.LOOP_CONTINUOUSLY);
  }

  void stopLoop(){
    if(clips[currentClip].isRunning())
      clips[currentClip].stop();      
  }
}