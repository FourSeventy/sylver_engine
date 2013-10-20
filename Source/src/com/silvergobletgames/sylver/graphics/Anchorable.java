package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.util.SylverVector2f;

/**
 * By default, Images and Text will be drawn using the SceneObject's getPosition()
 * method, and treating this position as the bottom left coordinate of the object in space.
 * 
 * The Anchorable enum will allow us to define a new "lock point" to use for these types of
 * scene objects.  We do this by calling the  getNewPosition function, passing in the Anchorable.
 * 
 * @author Justin Capalbo
 */
public interface Anchorable {
    
    public enum Anchor
    {
        TOPLEFT, TOPCENTER, TOPRIGHT, RIGHTCENTER, BOTTOMRIGHT, BOTTOMCENTER,
        BOTTOMLEFT, LEFTCENTER, CENTER;
        
        static SylverVector2f getNewPosition(Anchorable obj, float x, float y)
        {
            //Dimensions
            float width = obj.getDimensions().x;
            float height = obj.getDimensions().y;
            //Scale
            float scale = obj.getScale();
            float newX;
            float newY;
            switch (obj.getAnchor())
            {
                //X and Y halfway.
                case CENTER: newX = x - width*scale / 2; newY = y - height*scale / 2; break;
                
                //X none, Y halfway.    
                case LEFTCENTER: newX = x; newY = y - height*scale / 2; break;
                    
                //X none, Y full.
                case TOPLEFT: newX = x; newY = y - height * scale; break;
                    
                //X halfway, Y full.
                case TOPCENTER: newX = x - width*scale / 2; newY = y - height*scale; break;
                    
                //X and Y full.    
                case TOPRIGHT: newX = x - width*scale; newY = y - height*scale; break;
                
                //X full, Y halfway    
                case RIGHTCENTER: newX = x - width*scale; newY = y - height*scale / 2; break;
                
                //X full, Y none.    
                case BOTTOMRIGHT: newX = x - width*scale; newY = y; break;
                    
                //X half, Y none    
                case BOTTOMCENTER: newX = x - width*scale / 2; newY = y; break;
                
                //Bottom left
                default: return new SylverVector2f(x,y);
            }        
            return new SylverVector2f(newX, newY);
        }
    }
    
    /**
     * Should set the anchor, and then make a call to setPosition, assuming setPosition makes a call to getNewPosition
     * in order to correctly set its anchored position.
     * @param type 
     */
    public void setAnchor(Anchor type);
    public Anchor getAnchor();
    
    public SylverVector2f getDimensions();
    public float getScale();
   
    public void setPositionAnchored(float x, float y);

}

