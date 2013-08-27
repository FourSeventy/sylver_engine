package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import javax.media.opengl.GL3bc;
import java.awt.Point;
import javax.media.opengl.GL2;

/**
 *
 * @author mike
 */
public class Button extends WindowComponent  {
    
    //image on the button
    private Image image; 
    
    //text of the button
    public Text text;
    private float textPaddingX= 0;
    private float textPaddingY=0;
    public boolean dontKillClick = false;
     
    //mouse is hovering boolean
    private boolean mouseHovering = false;
    

    //================
    // Constructors
    //================
    
    public Button(String ref , float xPos, float yPos, float width, float height)
    {
        super(xPos,yPos);
        //set up image for button
        image = new Image(ref);
        image.setDimensions(width, height);
        image.setPosition(xPos, yPos);
       
        
        this.width = width;
        this.height = height;
        
    }
    
    public Button(Image img , float xPos, float yPos, float width, float height)
    {
        super(xPos,yPos);
        //set up image for button
        image = img;
        image.setDimensions(width, height);
        image.setPosition(xPos, yPos);
       
        
        this.width = width;
        this.height = height;
        
    }
       
    public Button(Text text, float xPos,float yPos)
    {       
        super(xPos,yPos);
        this.text = text;
        
        text.getPosition().x = xPos ;
        text.getPosition().y = yPos;              
        
        this.width = text.getWidth();
        this.height = text.getHeight();        
    }
    
    public Button(String ref, Text text, float xPos, float yPos, float width, float height)
    {
        super(xPos,yPos);
        //set button position and width     
        this.width = width;
        this.height = height;    
        
        //set text stuff
        this.text = text;        
        text.getPosition().x = xPos ;
        text.getPosition().y = yPos;
        
        //set image stuff
        image = new Image(ref);
        image.setDimensions(width, height);
        image.setPosition(xPos, yPos);
        
    }
    
    
    //=====================
    // Scene Object Methods
    //=====================
    
    public void update()
    {
        //update the image
        if(image != null)
        {
            image.setPosition(xPosition, yPosition);
            this.image.update();          
        }
        
        if(text != null)
        {
            this.text.getPosition().x = this.xPosition + this.textPaddingX;
            this.text.getPosition().y=  this.yPosition + this.textPaddingY;
            this.text.update();
        }
        
        //=========
        // Input
        //=========
        
        
            //set some position variables we will check for firing events 
            InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();

            Point mouseLocation = input.getScreenMouseLocation(); 
            if(!this.disabled && !this.hidden)
            {
                //determine if we need to fire a clicked event
                if(input.isMouseClicked())
                {                   
                    if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    {
                        if(!dontKillClick)
                            input.killMouseClick();

                        this.fireAction(this, "clicked");

                    }

                }

                if(input.isMouseDown())
                {
                    if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    {
                        if(!dontKillClick)
                            input.killMouseClick();

                        this.fireAction(this, "mouseDown");
                    }
                }

                if(!input.isMouseDown())
                {
                    if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    {
                        this.fireAction(this, "mouseUp");
                    }
                }

                //determine if we need to fire a mouseExited event
                if(!(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height) && mouseHovering == true)
                {

                    this.fireAction(this,"mouseExited");              
                }

                //determine if we need to fire a mouseEntered event
                if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height && mouseHovering == false)
                {
                    this.fireAction(this, "mouseEntered");
                }


                //update mouseHovered boolean
                if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    this.mouseHovering = true;
                else
                    this.mouseHovering = false;
            }
        
        
        
        
    }
    
    public void draw(GL2 gl)
    {
        if(!hidden)
        {
            //draw the background image
            if(image != null)
            {
                //if this is disabled draw the image with some alpha
                if(this.disabled)               
                    this.image.setAlphaBrightness(.5f);              
                else 
                    this.image.setAlphaBrightness(1f);
                
                //draw
                this.image.draw(gl);
                 
            }

            if(text != null)
            {
              //draw text    
              text.draw(gl);
            }
        }
    }
    
     public void setPosition(float x, float y)
    {
        this.xPosition = x;
        this.yPosition = y;
        
        if(image != null)
            image.setPosition(x, y);
        
        if(text != null)
        {
             text.getPosition().x = x + textPaddingX;
             text.getPosition().y = y + textPaddingY;
        }
                 
    }
   
     
     //=====================
     //Class Methods
     //====================
     
    
    /**
     * Gets if the mouse cursor is currently on top of this button
     * @return if mouse is hovering on this button
     */
    public boolean isMouseHovering()
    {
        return this.mouseHovering;
    }
    
    
    public void setTextPadding(float x, float y)
    {
        if(text != null)
        {
            textPaddingX = x;
            textPaddingY = y;
        }
    }

   
    public Image getImage()
    {
        return this.image;
    }
    
    public Text getText()
    {
        return this.text;
    }
    
    public void setText(String s){
        this.text.setText(s);
    }
    
    public void setText(Text t){
        this.text = t;
    }
    
    public void setImage(Image image)
    {
        this.image = image;
        image.setDimensions(width, height);
        image.setPosition(this.xPosition, this.yPosition);
    }
}
