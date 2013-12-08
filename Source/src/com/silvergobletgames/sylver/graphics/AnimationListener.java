package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.graphics.AnimationPack.ImageAnimation;
import java.util.EventListener;

/**
 * Animation listener interface. Can be implemented by a class that wants to listen for animation finished events.
 * @author Mike
 */
public interface AnimationListener extends EventListener 
{
    /**
     * Notification that the given ImageAnimation just finished animating.
     * @param img Image that finished the animation.
     * @param animation animation that finished animating.
     */
    public void finishedAnimating(Image img, ImageAnimation animation);
}
