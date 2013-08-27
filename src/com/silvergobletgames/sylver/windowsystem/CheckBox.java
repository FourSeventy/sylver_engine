package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import java.awt.Point;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 *
 * @author mike
 */
public class CheckBox extends WindowComponent {

    private Image checkedImage = new Image("checkBoxChecked.png");
    private Image uncheckedImage = new Image("checkBoxUnchecked.png");
    
    private boolean checked = false;
    
    
    //================
    // Constructors
    //================
    
    public CheckBox(float x, float y, boolean c)
    {
        super(x,y);
        this.width = 20;
        this.height = 20;
        checkedImage.setDimensions(20, 20);
        uncheckedImage.setDimensions(20, 20);
        this.checked = c;
    }
    
    
    //====================
    // SceneObject Methods
    //=====================
    
    public void draw(GL2 gl) 
    {
        if(!this.hidden)
        {
            if(checked)   
            {
                //if this is checked draw the image with some alpha
                if(this.disabled)               
                    this.checkedImage.setColor(new Color(1f,1f,1f,.5f));              
                else 
                    this.checkedImage.setColor(new Color(1f,1f,1f,1f));
                
                //draw
                this.checkedImage.draw(gl); 
            }
            else
            {
                //if this is disabled draw the image with some alpha
                if(this.disabled)               
                    this.uncheckedImage.setColor(new Color(1f,1f,1f,.5f));              
                else 
                    this.uncheckedImage.setColor(new Color(1f,1f,1f,1f));
                
                //draw
                this.uncheckedImage.draw(gl); 
            }
        }
    }
    
    public void update() 
    {
       
       
        InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();
        
        if(!this.hidden && !this.disabled)
            {
                //set some position variables we will check for firing events 
                Point mouseLocation = input.getScreenMouseLocation(); 

                //determine if we need to fire a clicked event
                if(input.isMouseClicked())
                {                   
                    if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    {             
                        //toggle the checkbox
                        this.checked = !this.checked;

                        //fire action and kill mouse click
                        this.fireAction(this, "clicked");
                        input.killMouseClick();
                    }

                }
            } 
       
               
    }
      
    public void setPosition(float x, float y)
    {
       this.xPosition = x;
       this.yPosition = y;
        
        checkedImage.setPosition(x, y);
        uncheckedImage.setPosition(x, y);
        
    }
    
    
    //================
    // Class Methods
    //================
    
    /**
     * Returns if this checkbox is checked
     * @return True if the checkbox is checked
     */
    public boolean isChecked()
    {
        return this.checked;
    }
    
    /**
     * Sets the checkbox to be checked or not
     * @param value 
     */
    public void setChecked(boolean value)
    {
        checked = value;
        this.fireAction(this, "clicked");
    }
}
