
package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.core.Scene;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

public class DropDown<E> extends WindowComponent
{

    //backgrounds
    private Image background;
    private Image openBackground= new Image("dropDownOpenBack.png");
    private float openHeight = 200;
    
    //selected text
    private Text selectedText = new Text("");
 
    //edges
    private Image bottomEdge;
    private Image leftEdge;
    private Image rightEdge;
    private Image dropDownArrow;
    
    //up/down arrows
    private Image openedUpArrow;
    private Image openedDownArrow;
    
    //opened
    private boolean opened = false;
    
    //Open direction
    public static byte POPULATE_UPWARDS = -1;
    public static byte POPULATE_DOWNWARDS = 1;
    private byte expandDir = POPULATE_DOWNWARDS;
    
    //the hashmap holding all of the elements
    private HashMap<String,E> elements = new HashMap<>();
    private String selectedKey;
    
    //arraylist of buttons
    private ArrayList<Button> buttons = new ArrayList<>();
    
    
    //==============
    // Constructor
    //==============
    
    public DropDown(float x, float y)
    {
        super(x,y);
        this.width = 120;
        this.height = 30;
        
        //build background images
        background = new Image("textBoxBackground.png");
        background.setDimensions(this.width, this.height);     
        openBackground.setDimensions(this.width, this.openHeight); 
        
        //construct edges     
        bottomEdge = new Image("topBar.png");
        bottomEdge.setDimensions(width,3);
        
        leftEdge = new Image("sideBar.png");
        leftEdge.setDimensions(3, openHeight);
        
        rightEdge = new Image("sideBar.png");
        rightEdge.setDimensions(3, openHeight);
        
        //drop down arrow
        dropDownArrow = new Image("dropDownArrow.png");
        dropDownArrow.setScale(1.2f);
        dropDownArrow.setPosition(x + width - 15,y + 11);
        
        //selected text
        selectedText.setTextType(Text.CoreTextType.DEFAULT);
        selectedText.setScale(.6f);
        selectedText.setColor(new Color(Color.black));
    }
    
    //=====================
    // SceneObject Methods
    //=====================
    
    @Override
    public void draw(GL2 gl) 
    {
        if(!this.hidden)
        {
            //draw background stuff
            if(this.disabled)
            {
                background.setColor(new Color(1f,1f,1f,.5f));
                background.draw(gl);
                dropDownArrow.setColor(new Color(1f,1f,1f,.5f));
                dropDownArrow.draw(gl);
            }
            else
            {              
                background.setColor(new Color(1f,1f,1f,1f));
                background.draw(gl);
                dropDownArrow.setColor(new Color(1f,1f,1f,1f));
                dropDownArrow.draw(gl);
            }
                     
            if(selectedKey != null)
            {
                selectedText.setText(selectedKey); 
                selectedText.draw(gl);
            }
            


            //if opened draw opened background and buttons
            if(opened)
            {           
                //draw background stuff
                openBackground.draw(gl);
                bottomEdge.draw(gl);
                leftEdge.draw(gl);
                rightEdge.draw(gl);

                //draw buttons
                for(Button b: buttons)
                {
                    b.draw(gl);
                }
            }
        }
    }

    @Override
    public void update() 
    {
        
        if(!this.hidden && !this.disabled)
        {
           
            //update all of the buttons!
            if(this.opened)
            {
                for(Button b: buttons)
                {
                    b.update();
                }
            }
            
            //adjust background height
            this.openHeight = buttons.size() * 23; 
            this.openBackground.setDimensions(this.width, this.openHeight);
        }
        
        
            InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();
            if(!this.hidden && !this.disabled)
            {

                //set some position variables we will check for firing events 
                Point mouseLocation = input.getScreenMouseLocation(); 

                //determine if we need to fire a clicked event
                if(input.isMouseClicked())
                {                   
                    //if you are in the main area
                    if(mouseLocation.x >= this.getPosition().x && mouseLocation.x <= this.getPosition().x + this.width && mouseLocation.y >= this.getPosition().y && mouseLocation.y <= this.getPosition().y + this.height)
                    {
                        if(this.opened)
                            closeDropDown();
                        else
                            openDropDown();

                        this.fireAction(this, "clicked");
                        input.killMouseClick();
                    }
                    //if you are not the dropped down area
                    else if (this.opened && !mouseInsideExpandedDropdown(mouseLocation))
                        this.closeDropDown();

                }
                else if(input.isMouseKilledLastTick() && !this.opened)
                {
                    this.closeDropDown();
                }
            
           }
            
            
        
    }
    
    public void setPosition(float x, float y)
    {
 
        super.setPosition(x,y);
        
        this.background.setPosition(x, y);
        
        dropDownArrow.setPosition(x + width - 15,y + 11);
        
        this.selectedText.setPosition(x + 4, y + 10); 

        if (this.expandDir == DropDown.POPULATE_DOWNWARDS)
        {
            openBackground.setPosition(x, y - this.openHeight);
            bottomEdge.setPosition(x ,y - openHeight  );
            leftEdge.setPosition(x , y - openHeight);
            rightEdge.setPosition(x + width - 3, y - openHeight);
            leftEdge.setDimensions(3, openHeight);
            rightEdge.setDimensions(3, openHeight); 
            
            //position buttons
            for(Button b : buttons)
            {
                b.setPosition(x + 5, y - openHeight + 10 + (b.getText().getHeight() + 5)*buttons.indexOf(b)); 
            }
        }    
        else
        {
            openBackground.setPosition(x, y + this.height + 3);
            bottomEdge.setPosition(x ,y + this.height + openHeight );
            leftEdge.setPosition(x , y + this.height + 3);
            rightEdge.setPosition(x + width - 3, y + this.height + 3);
            leftEdge.setDimensions(3, openHeight);
            rightEdge.setDimensions(3, openHeight); 
            
            //position buttons
            for(Button b : buttons)
            {
                b.setPosition(x + 5, y + this.height + 10 + (b.getText().getHeight() + 5)*buttons.indexOf(b)); 
            }
        }
        
    }
    
    public void setOwningScene(Scene scene)
    {
        this.owningScene = scene;
        
        for(Button button: this.buttons)
            button.setOwningScene(scene);
    }
    
    //===============
    // Class Methods
    //===============
    
    private boolean mouseInsideExpandedDropdown(Point mouseLocation){
        if (expandDir == DropDown.POPULATE_DOWNWARDS)
        {
            return mouseLocation.x >= this.getPosition().x && mouseLocation.x <= this.getPosition().x + this.width && mouseLocation.y >= this.getPosition().y - this.openHeight && mouseLocation.y <= this.getPosition().y + this.height;
        }
        else
        {
            return mouseLocation.x >= this.getPosition().x && mouseLocation.x <= this.getPosition().x + this.width && mouseLocation.y >= this.getPosition().y + this.height && mouseLocation.y <= this.getPosition().y + this.height + this.openHeight;
        }
    }
    
    public void setExpandDirection(byte dir)
    {
        this.expandDir = dir;
    }
       
    private void openDropDown()
    {  
        this.opened = true;

    }
    
    private void closeDropDown()
    {       
        this.opened = false;
        
    }
    
    /**
     * Returns the current selection of the drop down
     * @return The current selection
     */
    public E getSelectedElement()
    {
        return elements.get(this.selectedKey);
    }
    
    public void setDefaultSelection(String selection)
    {
        selectedKey = selection; 
        this.fireAction(this, "selectionChanged");
    }
    
    public void addElement(HashMap.SimpleEntry<String,E> entry)
    {
        //add entry
        elements.put(entry.getKey(), entry.getValue());
        
        //build button
        Text t = new Text(entry.getKey());
        t.setScale(.6f);
        t.setColor(new Color(Color.black));
        
        final Button b = new Button(t,0,0);
        b.setWidth(this.width- 10);
        b.addActionListener(new ActionListener(){
          
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equals("clicked"))
                {
                    selectedKey = b.getText().toString();
                    closeDropDown();
                    fireAction(this,"selectionChanged");
                }
            }        
        
        });       
        b.setOwningScene(owningScene);
        
        this.buttons.add(b);
        
    }
    
    public void setSelectedElementValue(E value){
        this.elements.put(this.selectedKey, value);
    }
    
    public HashMap<String, E> getElements(){
        return new HashMap(this.elements);
    }
}
