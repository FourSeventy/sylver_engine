
package com.silvergobletgames.sylver.windowsystem;

import com.jogamp.newt.event.KeyEvent;
import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.Point;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;


/**
 *
 * @author mike
 */
public class TextBox extends WindowComponent {
       
    //background
    private Image background;
    private boolean hideBackground = false;
    
    //cursor
    private Image cursor;
    private long cursorBlink = 0;
    private boolean cursorDraw = false;
    
    
    //text
    private Text text; 
    private int maxCharacters = 100;
    private boolean alphaNumericOnly = false;
    
    private String lastText;
    
    //focused
    private boolean focused = false;
       
    
    //===============
    // Constructors
    //===============
    
    public TextBox( float x, float y)
    {
        super(x,y);
        this.width = 100;
        this.height = 30;
        
        //set background image
        background = new Image("textBoxBackground.png");
        background.setPosition(x, y);
        background.setDimensions(100, 30);
        
        //set blinking cursor image
        cursor = new Image("textBoxCursor.png");
        cursor.setPosition(x+ 10, y + 5);
        cursor.setDimensions(2,20);
        
        text = new Text("");
        text.setTextType(Text.CoreTextType.DEFAULT);
        text.setScale(.6f);
        text.setColor(new Color(Color.black));
    }
    
    public TextBox( String string, float x, float y)
    {
        this(x,y);
        this.text = new Text(string);
        text.setTextType(Text.CoreTextType.DEFAULT);
        text.setScale(.6f);
        text.setColor(new Color(Color.black));
        
        this.lastText = string;
    }

    public TextBox(Text text, float x, float y)
    {
        this(x,y);
        this.text = text;
        
        this.lastText = text.toString();
    }
    
    
    //======================
    // SceneObject Methods
    //======================
    
    public void draw(GL2 gl)
    {
        if(!hidden)
        {
            //draw background image
            if(!this.hideBackground)
            {
                this.background.draw(gl);
            }
            
            //draw text
           text.draw(gl);

            //draw cursor
           if( focused && cursorDraw)
               this.cursor.draw(gl);       
        }
        
    }
    
    public void update()
    { 
        
        if(!this.hidden)
        {
            //set text and cursor position
             this.text.setPosition(this.getPosition().x + 10, this.getPosition().y + 10);
             if (focused)
                 this.cursor.setPosition(this.getPosition().x + 11 + text.getWidth(), this.getPosition().y + 5);  

            //handle cursor blinking
            cursorBlink++;
            if(cursorBlink % 50 == 0)
                cursorDraw = !cursorDraw;
        }
        
        //==============
        // Handle Input
        //==============
        
         //checking to see if mouse clicked in order to set focus
        if(!this.disabled && !this.hidden)
        {
           
               InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();

                Point mouseLocation = input.getScreenMouseLocation(); 
                if(input.isMouseClicked())
                {                   
                    if(  mouseLocation.x >= this.getPosition().x && mouseLocation.x <= this.getPosition().x + this.width && mouseLocation.y >= this.getPosition().y && mouseLocation.y <= this.getPosition().y + this.height)
                    {
                    this.focused = true;
                    input.killMouseClick();
                    }        
                    else
                    this.focused = false;
                }       
                else if(input.isMouseKilledLastTick())
                {
                    this.focused = false;
                }

                //populating text with typed keys
                if(focused)
                {
                    String builder = "";
                    for(Character c: input.getTypedCharacters())
                    {
                        if(c == '\b')
                        {
                            if(builder.length()> 0)
                            {
                                builder = builder.substring(0,builder.length() -1);
                            }
                            if(text.toString().length() > 0)
                            {
                                text.setText(text.toString().substring(0,text.toString().length() -1));
                            }
                        }
                        else if(text.toString().length() < this.maxCharacters)
                        {
                            //check for alphanumeric
                            if(this.alphaNumericOnly)
                            {

                                int asciiCode = (int)c;
                                if(!((asciiCode>= 48 && asciiCode <= 57) ||(asciiCode >=65 && asciiCode <=90) || (asciiCode >= 97 && asciiCode <= 122) || (asciiCode == 95) || (asciiCode == 45) || (asciiCode == 33) || (asciiCode == 64)) )
                                {
                                    continue;
                                }
                            }
                           builder += c;
                        }
                    }
                    text.setText(text.toString() + builder);
                    text.setPosition(this.getPosition().x + 10, this.getPosition().y + 10);
                    if (focused)
                        this.cursor.setPosition(this.getPosition().x + 11 + text.getWidth(), this.getPosition().y + 5);  
                }

                if (!text.toString().equals(this.lastText))
                {
                    this.fireAction(text, "textChanged");
                    this.lastText = text.toString();
                }
            
        }
    }
    
    public void setPosition(float x, float y)
    {
        super.setPosition(x,y);
        this.background.setPosition(x, y);
        if (focused)
            this.cursor.setPosition(x + 11 + text.getWidth(), y + 5);     
        this.text.setPosition(x + 10, y + 10);
    }
    
    public void setText(String string)
    {
        text.setText(string);
    }
    
    public void setHideBackground(boolean value)
    {
        this.hideBackground = value;
    }
    
    public void setMaxCharacters(int maxCharacters)
    {
        this.maxCharacters = maxCharacters;
    }
    
    public void setAlphaNumericRestriction(boolean value)
    {
        this.alphaNumericOnly = value;
    }
    
    public void setDimensions(float x, float y){
        this.width = x;
        this.height = y;
        this.background.setDimensions(x, y);
    }
    
    public void setCursorScale(float scale)
    {
        this.cursor.setScale(scale);
    }
    
    public String getText(){
        return text.toString();
    }
    
    public Text getTextObj(){
        return this.text;
    }
}
