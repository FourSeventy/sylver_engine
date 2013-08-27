package com.silvergobletgames.sylver.windowsystem;

import com.jogamp.newt.event.KeyEvent;
import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.Point;
import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 *
 * @author mike
 */
public class TextArea extends WindowComponent
{

    //background
    private Image background;
    //edges
    private Image topEdge;
    private Image bottomEdge;
    private Image leftEdge;
    private Image rightEdge;
    //cursor
    private Image cursor;
    private long cursorBlink = 0;
    private boolean cursorDraw = false;
    private int cursorTextPositionAdjustment;
    //focused
    private boolean focused = false;
    //the actual text of the area
    protected String text = "";
    //the labels we will be drawing
    private ArrayList<Label> lines = new ArrayList<>();

    //================
    // Constructors
    //================
    
    public TextArea(float x, float y, float width, float height) 
    {
        //set dimensions
        super(x, y);
        this.setWidth(width);
        this.setHeight(height);
        //construct edges
        topEdge = new Image("topBar.png");
        topEdge.setPosition(x, y + height - 2);
        topEdge.setDimensions(width, 2);

        bottomEdge = new Image("topBar.png");
        bottomEdge.setPosition(x, y);
        bottomEdge.setDimensions(width, 2);

        leftEdge = new Image("sideBar.png");
        leftEdge.setPosition(x, y);
        leftEdge.setDimensions(2, height);

        rightEdge = new Image("sideBar.png");
        rightEdge.setPosition(x + width - 2, y);
        rightEdge.setDimensions(2, height);

        //contruct background
        this.background = new Image("white.png");
        this.background.setPosition(x, y);
        this.background.setDimensions(width, height);

        //set blinking cursor image
        cursor = new Image("textBoxCursor.png");
        cursor.setPosition(x + 10, y + height + 5);
        cursor.setDimensions(2, 20);

    }

    
    //======================
    // Scene Object Methods
    //======================
    
    public void draw(GL2 gl) 
    {
        if (!hidden) 
        {
            //draw background
            this.background.draw(gl);

            //draw edges
            topEdge.draw(gl);
            bottomEdge.draw(gl);
            leftEdge.draw(gl);
            rightEdge.draw(gl);

            //draw all the text
            for(Label l: lines)
            {
                l.draw(gl); 
            }

            //draw the cursor
            if (focused && cursorDraw) {
                this.cursor.draw(gl);
            }

        }
    }

    public void update()
    {
        //handle cursor blinking
        if(!this.hidden)
        {     
            cursorBlink++;
            if (cursorBlink % 50 == 0) {
                cursorDraw = !cursorDraw;
            }
        }
        
        //================
        // Handle Input
        //================       
        boolean somethingTyped = false;
        if(!this.hidden)
        {
            InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();

            //checking to see if mouse clicked in order to set focus
            Point mouseLocation = input.getScreenMouseLocation();
            if (input.isMouseClicked()) 
            {
                if (mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                {
                    this.focused = true;
                    input.killMouseClick();
                } 
                else 
                    this.focused = false;

            } 
            else if (input.isMouseKilledLastTick()) 
            {
                this.focused = false;
            }

            //=================
            // Keyboard Input
            //=================

            
            //populating text with typed keys
            if (focused) 
            {
                for (Character typedCharacter : input.getTypedCharacters()) 
                {
                    somethingTyped = true;
                    
                    //backspace
                    if (typedCharacter == '\b') 
                    {
                        int insertPosition = text.length() + cursorTextPositionAdjustment;
                        if (insertPosition -1 > 0) 
                        {
                            text = new StringBuffer(text).deleteCharAt(insertPosition -1).toString();
                        }

                    } 
                    else
                    {
                        
                        if(typedCharacter == '\r')
                            typedCharacter = '\n';
                    
                        //insert the char to our text
                        int insertPosition = text.length() + cursorTextPositionAdjustment;

                        text = new StringBuffer(text).insert(insertPosition, typedCharacter).toString();
                    }  
                        
                    
                }
                
                //text adjustment
                if(input.isKeyReleased(KeyEvent.VK_LEFT))
                {
                    this.cursorTextPositionAdjustment--;
                    
                    if(this.cursorTextPositionAdjustment < -this.text.length())
                        cursorTextPositionAdjustment = -this.text.length();
                    
                    
                }
                if(input.isKeyReleased(KeyEvent.VK_RIGHT))
                {
                    this.cursorTextPositionAdjustment++;
                    
                    if(this.cursorTextPositionAdjustment > 0)
                        cursorTextPositionAdjustment = 0;
                }
                
                
                //===============
                // Create labels
                //===============

                //set up dialog
                if(somethingTyped)
                {
                    this.populateLabels();
                }

                //Adjust the cursor position
                if (focused) 
                {
                    //find out the current text position
                    int currentTextPosition = text.length() + this.cursorTextPositionAdjustment;
                    //find out the current line
                    int lineSum = 0;
                    int currentLineIndex = 0;
                    Label currentLine = null;
                    for(Label label :this.lines)      
                    {
                        lineSum += label.getText().toString().length() ; 
                        if(lineSum >= currentTextPosition)
                        {
                            currentLine = label;
                            currentLineIndex = this.lines.indexOf(currentLine);
                            break;
                        }
                        lineSum++; // for \n
                    }
                    
                    //if we are on a blank new line, hacks
                    boolean hacks = false;
                    if(currentLine == null && lineSum >= currentTextPosition && !this.lines.isEmpty())
                    {
                        currentLineIndex = this.lines.size() -1;
                        currentLine = this.lines.get(currentLineIndex);
                        currentLineIndex++;
                        hacks = true;
                    }
                    
                    float width = 0, height = 0;
                    
                    //determine height of a line   
                    if(currentLine != null)
                        height = currentLine.getHeight();
                                     
                    //determine width
                    if(currentLine != null && !hacks)
                    {
                        Label currentLabel = currentLine;

                        Text ehh = new Text(currentLabel.getText());
                        ehh.setText(currentLabel.getText().toString().substring(0,currentLabel.getText().toString().length() - (lineSum - currentTextPosition) ));                                
                        Label labelWithWidthAdjust = new Label(ehh,currentLabel.getPosition().x,currentLabel.getPosition().y);      
                        width = labelWithWidthAdjust.getWidth();
                    }                  
                   
                    this.cursor.setPosition(this.xPosition + width + 3, this.yPosition + this.height - 25 - (currentLineIndex * height));
                              
                    // up
                    if(input.isKeyReleased(KeyEvent.VK_UP))
                    {
                        if(currentLineIndex -1 >=0)
                        {
                            int amount = 1 +(currentLine.getText().toString().length() - (lineSum - currentTextPosition));
                            if(this.lines.get(currentLineIndex -1).getText().toString().length() > (currentLine.getText().toString().length() - (lineSum - currentTextPosition)))
                                amount += this.lines.get(currentLineIndex -1).getText().toString().length() - (currentLine.getText().toString().length() - (lineSum - currentTextPosition));
                           
                            this.cursorTextPositionAdjustment -=  amount;
                        }
                        
                        //reset cursor
                        this.cursorDraw = true;
                        this.cursorBlink = 0;
                    }
                    
                    if(input.isKeyReleased(KeyEvent.VK_DOWN))
                    {
                        if((lines.size() -1 ) >= (currentLineIndex +1))
                        {
                            int amount = 1 +(lineSum - currentTextPosition);
                            if(this.lines.get(currentLineIndex + 1).getText().toString().length() >  (currentLine.getText().toString().length() - (lineSum - currentTextPosition)))
                                amount += (currentLine.getText().toString().length() - (lineSum - currentTextPosition));
                            else
                                amount += this.lines.get(currentLineIndex + 1).getText().toString().length();
                            
                            this.cursorTextPositionAdjustment += amount;
                            
                            //reset cursor
                        this.cursorDraw = true;
                        this.cursorBlink = 0;
                            
                        }
                        
                    }
                }
            }

        }
        
        
        
        
        
    }

    public void setPosition(float x, float y) 
    {
        this.xPosition = x;
        this.yPosition = y;

        //set background and edge positions
        this.background.setPosition(x, y);
        topEdge.setPosition(x, y + height - 2);
        bottomEdge.setPosition(x, y);
        leftEdge.setPosition(x, y);
        rightEdge.setPosition(x + width - 2, y);

        //position the lines of text correctly
        for(int i = 0; i <lines.size(); i++)
        {
            lines.get(i).setPosition( x + 3,  y + this.height - lines.get(i).getText().getHeight() -(i * lines.get(i).getText().getHeight()) );
        }


    }

    //===============
    // Class Methods
    //===============
    
    public String getText()
    {
        return this.text;
    }
    
    public void setText(String newText)
    {
        this.text = newText;
        
        populateLabels();
        
    }
    
    /**
     * Clears the text area.
     */
    public void clear()
    {
        this.text = "";
        this.lines.clear();
    }
    
    private void populateLabels()
    {
        //=================
        // Populate Labels
        //==================
          ArrayList<String> rowList = new ArrayList<>();
            this.lines.clear();

            //initialize text
            Text inputText = new Text("");
            inputText.setTextType(Text.CoreTextType.CODE);
            inputText.setScale(1.3f);
            inputText.setColor(Color.black);
            
            //feeder strings
            String[] groups = text.split("\n");

            //build up our lines         
            for(String group: groups)
            {
                //building up our line
                String line = "";
                for(int i =0; i < group.length(); i++)
                {
                    line += group.charAt(i);
                    // if our line is too wide
                    inputText.setText(line);
                    if(inputText.getWidth() > this.width)
                    {
                        rowList.add(line);
                        line = "";
                    }
                }
                rowList.add(line); 

            }
            

            for(String s: rowList)
            {   
                Text t = new Text(inputText);
                t.setText(s);
                lines.add(new Label(t,0,0));
            }

            //position the lines of text correctly
            for(int i = 0; i <lines.size(); i++)
            {
                lines.get(i).setPosition( this.getPosition().x +3,  this.getPosition().y + this.height - lines.get(i).getText().getHeight() - (i * lines.get(i).getText().getHeight()) );
            }

            int tempHeight = 0;
            for(int i = 0; i <lines.size(); i++)
            {
                tempHeight += lines.get(i).getText().getHeight();
            }
            //this.height = tempHeight;
    }
}
