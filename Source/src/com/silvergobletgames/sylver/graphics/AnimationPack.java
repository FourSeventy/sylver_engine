package com.silvergobletgames.sylver.graphics;

import com.jogamp.opengl.util.texture.Texture;
import com.silvergobletgames.sylver.core.Game;
import java.util.ArrayList;
import java.util.HashMap;
import com.silvergobletgames.sylver.util.SylverVector2f;


/**
 * This class holds all of the textures and meta data that an Image needs to animate.
 * @author Mike
 */
public abstract class AnimationPack
{
    //the animationSet set 
    public HashMap<ImageAnimation, ArrayList<Texture>> animationSet = new HashMap();
    //the animation timing map
    protected HashMap<ImageAnimation, Integer> timingMap = new HashMap();
    //the animation positioning map
    protected HashMap<ImageAnimation, SylverVector2f> positionOffsetMap = new HashMap();
    //frames per texture map
    protected HashMap<ImageAnimation, Integer> fptMap = new HashMap();

    
    //===============
    // Class Methods
    //===============
    
    /**
     * Get the timing delay associated with the given ImageAnimation
     * @param animation ImageAnimation to get the diming delay for
     * @return 
     */
    public int getTimingDelay(ImageAnimation animation)
    {
        if(timingMap.containsKey(animation))
            return timingMap.get(animation);
        else
            return 0; 
    }
    
    /**
     * Get the frames per texture associated with the given ImageAnimation
     * @param animation ImageAnimation to geet the frames per texture for
     * @return 
     */
    public int getFPT(ImageAnimation animation)
    {
        if(fptMap.containsKey(animation))
            return fptMap.get(animation);
        else
            return 10; //default fpt value
    }
    
    /**
     * Get the position offset associated with a given ImageAnimation
     * @param animation
     * @return 
     */
    public SylverVector2f getPositionOffset(ImageAnimation animation)
    {
        if(positionOffsetMap.containsKey(animation))
            return positionOffsetMap.get(animation);
        else
            return new SylverVector2f(0,0); //default attack delay value
    }
    
    //open ended interface for extensible animation enum
    public interface ImageAnimation 
    {        
    }
    
    //core image animations
    public static enum CoreAnimations implements ImageAnimation
    {
        NONE, IDLE
    }
    
    
    //==============================
    // Default Animation Pack
    // used if a pack cant be found
    //==============================
    public static class DefaultAnimationPack extends AnimationPack
    {
        public DefaultAnimationPack()
        {
             //Idle
            ArrayList<Texture> idle = new ArrayList();
                idle.add(Game.getInstance().getAssetManager().getTextureLoader().getTexture("textureMissing.jpg"));
            this.animationSet.put(CoreAnimations.IDLE, idle);

        }
    }
}
