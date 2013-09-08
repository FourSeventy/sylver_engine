
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

    
    //=====================
    // SceneObject Methods
    //=====================
    @Override
    public void draw(GL2 gl) {
        if(!hidden)
        {
          text.draw(gl);
        }
    }

    @Override
    public void update() {
    }
    
    @Override
    public void setPosition(float x, float y)
    {
        super.setPosition(x,y);
        this.text.setPosition(x, y);
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
