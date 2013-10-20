package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.graphics.AnimationPack.ImageAnimation;
import java.util.EventListener;

/**
 *
 * @author Justin Capalbo
 */
public interface AnimationListener extends EventListener {
    public void finishedAnimating(ImageAnimation a);
}
