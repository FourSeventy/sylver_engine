package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import java.util.HashMap;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 * This class handles special effects that take place in the scene such as scene 
 * fades, transitions, screen shake, etc.
 * @author Mike
 */
public class SceneEffectsManager
{
    
    //effect execution map
    private HashMap<String,PostEffectExecutor> effectMap = new HashMap();
       
    //scene lighting variables
    public Color sceneAmbientLight; 
    
    //variables for scene fading
    private Image blackImage;
    
    
    //=================
    // Constructor
    //=================
    
    public SceneEffectsManager()
    {
        //set ambient light color
        sceneAmbientLight = new Color(1,1,1,1);
        
        //set fade to black image
        blackImage = new Image("black.png");       
        blackImage.setColor(new Color(1,1,1,0));
        blackImage.setDimensions(1600, 900); //largest aspect ratio that the game supports
    }
        
    public void update()
    {
        if(this.effectMap.containsKey("fadeToBlack"))
        {
            blackImage.setColor(new Color(1,1,1,blackImage.getColor().a + .015f));
            if(blackImage.getColor().a >= 1)
            {
                blackImage.setColor(new Color(1,1,1,1));
                
                PostEffectExecutor executor = this.effectMap.get("fadeToBlack");
                if(executor != null)
                    executor.execute();
                
                this.effectMap.remove("fadeToBlack");                              
            }
        }
        
        if(this.effectMap.containsKey("fadeFromBlack"))
        {
            blackImage.setColor(new Color(1,1,1,blackImage.getColor().a - .015f));
            if(blackImage.getColor().a <= 0)
            {
                blackImage.setColor(new Color(1,1,1,0));
                
                PostEffectExecutor executor = this.effectMap.get("fadeFromBlack");
                if(executor != null)
                    executor.execute();
                
                this.effectMap.remove("fadeFromBlack");
            }
        }
    }
    
    public void render(GL2 gl)
    {
        if(this.effectMap.containsKey("fadeToBlack") || this.effectMap.containsKey("fadeFromBlack"))        
           blackImage.draw(gl);
    }
        
    //======================
    // Scene Effect Methods
    //======================
    
    public void fadeToBlack(PostEffectExecutor executor)
    {
        blackImage.setColor(new Color(1,1,1,0));
        this.effectMap.put("fadeToBlack", executor);
    }
    
    public void fadeFromBlack(PostEffectExecutor executor)
    {
        blackImage.setColor(new Color(1,1,1,1));
        this.effectMap.put("fadeFromBlack",executor);
    }
    
     
    public static class PostEffectExecutor
    {
        /**
         * Method that will be executed when the current scene effect finishes. 
         * This method is intended to be overwritten using an anonymouse class.
         */
        public void execute()
        {
        }
    }
}
