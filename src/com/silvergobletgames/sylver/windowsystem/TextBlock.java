
package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.graphics.Text;
import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 *
 * @author mike
 */
public class TextBlock extends WindowComponent {
    
    
    //lines of text
    private ArrayList<Label> lines = new ArrayList<>();
    
    
    //===============
    // Constructors
    //===============
    
    public TextBlock(float x, float y, float width, Text inputText)
    {
        super(x,y);
        
        this.width = width;
        
        //set up dialog
        ArrayList<String> list = new ArrayList<>();
        
        //break up the input into an array seperateed by spaces
        String[] spaces = inputText.toString().split(" ");
        
        //concatinate these words together untill they are length 90
        String line = "";
        for(String part: spaces)
        {
            line += part + " ";
            
            inputText.setText(line); 
            if(inputText.getWidth() > width)
            {
               int index = line.lastIndexOf(part);
               line = line.substring(0, index);
               list.add(line);
               line = part + " ";
            }
            
        }
        list.add(line);
        
        for(String s: list)
        {   Text t = new Text(inputText);
            t.setText(s);
            lines.add(new Label(t,0,0));
        }
        
        //position the lines of text correctly
        for(int i = 0; i <lines.size(); i++)
        {
            lines.get(i).setPosition( x,  y - (i * lines.get(i).getText().getHeight()) );
        }
        
        int tempHeight = 0;
        for(int i = 0; i <lines.size(); i++)
        {
            tempHeight += lines.get(i).getText().getHeight();
        }
        this.height = tempHeight;
    }

   
    //======================
    // SceneObject Methods
    //======================
    
    public void draw(GL2 gl) 
    {
        for(Label l: lines)
        {
            l.draw(gl); 
        }
    }

    public void update() 
    {
        for(Label l: lines)
        {
            l.update(); 
        }
    }
    
    public void setPosition(float x, float y)
    {
        //position the lines of text correctly
        for(int i = 0; i <lines.size(); i++)
        {
            lines.get(i).setPosition( x,  y - (i * lines.get(i).getText().getHeight()) );
        }
    }
    
}
