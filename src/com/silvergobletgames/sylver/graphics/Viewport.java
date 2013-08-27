package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.Scene.Layer;
import com.silvergobletgames.sylver.core.SceneObject;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import com.silvergobletgames.sylver.util.SylverVector2f;


/**
 *This class is the viewport through which we view the game.
 * 
 */
public class Viewport 
{

    //viewport dimensions
    protected float xPos = 0;
    protected float yPos = 0;
    protected float width = 1600;
    protected float height = 900;
       
    //movement variables used for viewport movement
    public boolean canMoveUp = true;
    public boolean canMoveDown = true;
    public boolean canMoveLeft = true;
    public boolean canMoveRight = true;
    
    //panning variables
    private int panSpeed = 8;
    private boolean panningHorizontal = false;
    private boolean panningVertical = false;
    
    //================
    // Constructor
    //================
    
    /**
     * Constructs a viewport
     * 
     */
    public Viewport()
    {     
        
        
        
    }

    //===============
    // Class Methods
    //===============
    
    public float getHeight()
    {
        return this.height;
    }
    
    public float getWidth()
    {
        return this.width;
    }

    protected void setxPosition(float x)
    {        
        this.setPosition(x,this.yPos);
    }

    protected void setyPosition(float y)
    {
        this.setPosition(this.xPos,y);
    }
    
    protected void setPosition(float x, float y)
    {
        this.xPos = x;
        this.yPos = y;
    }
    
    /**
     * Gets the bottom left coordinates of the viewport
     * @return Point that is the bottom left coordinates of viewport.
     */
    public SylverVector2f getBottomLeftCoordinate()
    {
        return new SylverVector2f(this.xPos,this.yPos);
    }
    
    /**
     * Gets the center coordinates of the viewport.
     * @return Point that is the center coordinates of the viewport.
     */
    public SylverVector2f getCenterCoordinates()
    {
        return new SylverVector2f(this.xPos+ this.width/2, this.yPos + this.height/2);
    } 
    
    /**
     * Pans the viewport to center around the given point. Abides by the current pan speed.
     * @param point Point that you want the viewport to center on.
     */
    public void centerAroundPoint(SylverVector2f point)
    {      
        this.centerAroundXCoordinate(point.x);
        this.centerAroundYCoordinate(point.y);
    }
    
    /**
     * Pans the viewport to center around the given x coordinate. Abides by the current pan speed.
     * @param x X coordinate that the viewport should center on.
     */
    public void centerAroundXCoordinate(float x)
    {    
        //if the viewport is disjoint from the player, slowly pan
        if(Point.distance(x, 0, this.getCenterCoordinates().x, 0) > 10) 
        {   
            this.panningHorizontal = true;
            int direction;
            if (x >= this.getCenterCoordinates().x)
                direction = 1;
            else
                direction = -1;
            x = this.getCenterCoordinates().x + this.panSpeed * direction;
        }
        else
            this.panningHorizontal = false;
        
        this.setxPosition(x - this.getWidth()/2);
    }
    
    /**
     * Pans the viewport to center around the given y coordinate. Abides by the current pan speed.
     * @param y Y coordinate that the viewport should center on.
     */
    public void centerAroundYCoordinate(float y)
    {
         //if the viewport is disjoint from the player, slowly pan
        if(Point.distance(y, 0, this.getCenterCoordinates().y, 0) > 10) 
        {   
            this.panningVertical = true;
            int direction;
            if (y >= this.getCenterCoordinates().y)
                direction = 1;
            else
                direction = -1;
            
            y = this.getCenterCoordinates().y + this.panSpeed * direction;
        }
        else
            this.panningVertical = false;
        
        this.setyPosition(y - this.getHeight()/2);
    }
    
    /**
     * Sets the velocity at which the viewport will pan to its new coordinates
     * @param x New pan speed.
     */
    public void setPanSpeed(int x)
    {
        this.panSpeed = x;
    }
    
    public boolean isPanningHorizontal()
    {
        return this.panningHorizontal;
    }
    
    public boolean isPanningVertical()
    {
        return this.panningVertical;
    }
    
    public void setDimensions(float x, float y)
    {
        this.width = x;
        this.height = y;
        
        this.setPosition(this.xPos, this.yPos);
    }
    
    public void quickMoveToCoordinate(float x, float y)
    {
        //initially set position to given coordinates
        this.setxPosition(x - this.getWidth()/2);
        this.setyPosition(y - this.getHeight()/2);   
    }
     
    public boolean isSceneObjectVisible(SceneObject object, Layer layer)
    {
        SylverVector2f positionOfObj = new SylverVector2f(0,0);
        float widthOfObj =0;
        float heightOfObj =0;
        float maxDimension = 0;
        
        //determine position width and height for the various scene objects
        
        if(object instanceof Image)
        {
            positionOfObj = new SylverVector2f(((Image)object).getPosition());
            widthOfObj = ((Image)object).getWidth() * ((Image)object).getScale();
            heightOfObj = ((Image)object).getHeight() * ((Image)object).getScale();
        }
        else if(object instanceof Text)
        {
            positionOfObj = new SylverVector2f(object.getPosition());
            widthOfObj = ((Text)object).getWidth() * ((Text)object).getScale();
            heightOfObj = ((Text)object).getHeight() * ((Text)object).getScale();
        }
        else if(object instanceof ParticleEmitter)
        {
            positionOfObj = new SylverVector2f(object.getPosition());
            widthOfObj = 500;
            heightOfObj = 500; //TODO - more accurately figure this out
        }
        else if(object instanceof LightSource)
        {
            positionOfObj = new SylverVector2f(object.getPosition());           
            widthOfObj = ((LightSource)object).getSize();
            heightOfObj = widthOfObj;          
        }
        else if(object instanceof DarkSource)
        {
            positionOfObj = new SylverVector2f(object.getPosition());
            widthOfObj = ((DarkSource)object).getWidth();
            heightOfObj = ((DarkSource)object).getHeight();
        }
        
        
        //determine max dimension of obj
        maxDimension =(float)Math.sqrt(Math.pow(widthOfObj, 2) + Math.pow(heightOfObj, 2));
        
        //coordinate transform of position based on layer      
        SylverVector2f adjustedViewportPos = new SylverVector2f(this.xPos,this.yPos);
        adjustedViewportPos.scale(Layer.getLayerConversionFactor(Layer.MAIN, layer));
        SylverVector2f adjustedViewportDimensions = new SylverVector2f(this.getWidth(),this.getHeight());
        
        if(positionOfObj.x + maxDimension >= adjustedViewportPos.x && positionOfObj.x - maxDimension <= adjustedViewportPos.x + adjustedViewportDimensions.x&&
           positionOfObj.y + maxDimension >= adjustedViewportPos.y && positionOfObj.y - maxDimension  <= adjustedViewportPos.y + adjustedViewportDimensions.y)
            return true;
        else
            return false;
    }
   
}
