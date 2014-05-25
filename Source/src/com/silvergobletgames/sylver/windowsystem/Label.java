
package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.graphics.Text;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 *
 * @author mike
 */
public class Label extends WindowComponent {
    
    private Text text;
    private boolean dontSetTextPosition = false;
    
    //================
    // Constructors
    //================
    
    public Label(String text, float x, float y)
    {
        super(x,y);
        
        this.text = new Text(text);
        this.text.setPosition(x, y);

    }
    
    public Label(Text text, float x, float y)
    {
        super(x,y);
        
        this.text =text;
        this.text.setPosition(x, y);      
        this.width = text.getWidth();
        this.height = text.getHeight();
    }
    
    public Label(Text text, float x, float y, boolean dontSetTextPosition)
    {
        super(x,y);
        
        this.text =text;
        this.dontSetTextPosition = dontSetTextPosition;
         if(!this.dontSetTextPosition)
        {
            this.text.setPosition(x, y);
        }      
        this.width = text.getWidth();
        this.height = text.getHeight();
    }

    
    //=====================
    // SceneObject Methods
    //=====================
    @Override
    public void draw(GL2 gl)
    {
        if(!hidden)
        {
          text.draw(gl);
        }
    }

    @Override
    public void update() 
    {
        this.text.update();
    }
    
    @Override
    public void setPosition(float x, float y)
    {
        super.setPosition(x,y);
        
        if(!this.dontSetTextPosition)
        {
            this.text.setPosition(x, y);
        }
    }
    
    public void setDontSetTextPosition(boolean value)
    {
        this.dontSetTextPosition = value;
    }
    
    
    //===============
    // Class Methods
    //===============
    public Text getText()
    {
        return this.text;
    }
    
    public void setText(Text t)
    {
        this.text = t;
        this.width = text.getWidth();
        this.height = text.getHeight();
    }
}
