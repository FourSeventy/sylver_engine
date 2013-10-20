package com.silvergobletgames.sylver.core;

import com.jogamp.newt.event.KeyEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Mike
 */
public class InputSnapshot {
    
    //typed key list
    private ArrayList<Character> typedKeys = new ArrayList<>();
    //released set
    private HashMap<Short,Byte> releasedMap = new HashMap<>();
    //pressed set
    private HashMap<Short,Byte> pressedMap = new HashMap<>();
    //screen mouse position
    private Point screenMouseLocation = new Point(0,0); 
    
    //mouse variables
    private boolean mouseDown = false; 
    private boolean mouseClicked = false;
    private boolean mouseMoved= false;   
    private boolean mouseWheelMoved = false;
    private int mouseWheelRotation = 0;
    private int mouseButtonClicked;
    private boolean mouseKilled;
    
    
    //==============
    // Constructor
    //==============
    
    public InputSnapshot(ArrayList<Character> typedKeys,
                         HashMap<Short,Byte> releasedMap,
                         HashMap<Short,Byte> pressedMap,
                         boolean mouseDown,
                         boolean mouseClicked,
                         boolean mouseMoved,
                         int mouseWheelRotation,
                         int mouseButtonClicked,
                         boolean mouseKilled,
                         Point screenMouseLocation)
    {
        this.typedKeys = new ArrayList(typedKeys);
        this.releasedMap = releasedMap;
        this.pressedMap = pressedMap;
        this.mouseDown = mouseDown;
        this.mouseClicked = mouseClicked;
        this.mouseMoved = mouseMoved;
        
        if(mouseWheelRotation > 0)
          this.mouseWheelMoved = true;
        
        this.mouseWheelRotation = mouseWheelRotation;
        this.mouseButtonClicked = mouseButtonClicked;
        this.screenMouseLocation = new Point(screenMouseLocation);
        
    }
    
    
    //============================
    // Accessor and Query Methods
    //============================
     
    /**
     * Returns a list of all visible characters that were typed in the last snapshot. This
     * list is in the order that they were typed.
     * @return 
     */
    public ArrayList<Character> getTypedCharacters()
    {
        ArrayList<Character> typedCharacters = new ArrayList(this.typedKeys);
        
        return typedCharacters;
    }
    
    /**
     * Queries input snapshot to see if given key is currently pressed down.
     * A key is pressed down from the snapshot that it is moved into its down position until
     * the snapshot that it is released
     * @param key KeyEvent int constant representing the key
     * @return True if key is pressed down, false otherwise.
     */
    public boolean isKeyPressed(short key)
    {
       return pressedMap.containsKey(key);
    }
    
    /**
     * Queries input snapshot to see if given key is currently pressed down with the Shift modifier
     * @param key
     * @return True if the key is pressed down with the shift modifier
     */
    public boolean isKeyPressedShiftModifier(short key)
    {
        if(pressedMap.containsKey(key))
        {
           if((pressedMap.get(key) & 0b0000_0001) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    /**
     * Queries input snapshot to see if given key is currently pressed down with the ctrl modifier
     * @param key
     * @return True if the key is pressed down with the ctrl modifier
     */
    public boolean isKeyPressedCtrlModifier(short key)
    {
        if(pressedMap.containsKey(key))
        {
           if((pressedMap.get(key) & 0b0000_0010) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    /**
     * Queries input snapshot to see if given key is currently pressed down with the alt modifier
     * @param key
     * @return True if the key is pressed down with the alt modifier
     */
    public boolean isKeyPressedAltModifier(short key)
    {
        if(pressedMap.containsKey(key))
        {
           if((pressedMap.get(key) & 0b0000_0100) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    
    /**
     * Queries input snapshot to see if given key was released. 
     * A release happens if the key was returned to its UP position during this snapshot
     * @param key KeyEvent int constant representing the key
     * @return True if key was released, false otherwise
     */
    public boolean isKeyReleased(short key)
    {
       return releasedMap.containsKey(key);
    }
    
    /**
     * Queries input snapshot to see if given key is currently released with the Shift modifier
     * @param key
     * @return True if the key is released with the shift modifier
     */
    public boolean isKeyReleasedShiftModifier(short key)
    {
        if(releasedMap.containsKey(key))
        {
           if((releasedMap.get(key) & 0b0000_0001) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    /**
     * Queries input snapshot to see if given key is currently released with the ctrl modifier
     * @param key
     * @return True if the key is released with the ctrl modifier
     */
    public boolean isKeyReleasedCtrlModifier(short key)
    {
        if(releasedMap.containsKey(key))
        {
           if((releasedMap.get(key) & 0b0000_0010) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    /**
     * Queries input snapshot to see if given key is currently released with the alt modifier
     * @param key
     * @return True if the key is released with the alt modifier
     */
    public boolean isKeyReleasedAltModifier(short key)
    {
        if(releasedMap.containsKey(key))
        {
           if((releasedMap.get(key) & 0b0000_0100) != 0) //if the bitmask says we pressed shift
               return true;
        }    
        
        //else return false
        return false;
    }
    
    
    
    
    
    
      
    
    
    /**
     * Queries the input snapshot to see if the mouse is currently down.
     * @return True if the mouse is currently pressed down, otherwise false
     */
    public boolean isMouseDown()
    {
        return this.mouseDown;
    }
    
    /**
     * Queries the input snapshot to see if the mouse was clicked.
     * @return True if the mouse was clicked, otherwise false
     */
    public boolean isMouseClicked()
    {
        return this.mouseClicked;
    }
    
    /**
     * Queries the input snapshot to see if the mouse was moved
     * @return True if the mouse moved, false otherwise
     */
    public boolean isMouseMoved()
    {
        return this.mouseMoved;
    }
    
    /**
     * Queries the input snapshot to see if the mouse wheel was moved
     * @return returns True if the mouse wheel was moved, false otherwise
     */
    public boolean isMouseWheelMoved()
    {
        return this.mouseWheelMoved;
    }
    
    /**
     * 
     * @return The mouse screen location
     */
    public Point getScreenMouseLocation()
    {
        return this.screenMouseLocation;
    }     
    
    /**
     * 
     * @return Returns which mouse button was clicked
     */
    public int buttonClicked()
    {
        return this.mouseButtonClicked;
    }
    
    /**
     * 
     * @return Returns how much the mouse wheel was rotated
     */
    public int getWheelRotation()
    {
        return this.mouseWheelRotation;
    }
    
    public void killMouseClick()
    {
        mouseDown = false;
        mouseClicked = false;
        mouseKilled = true;
    }
    
    public boolean isMouseKilledLastTick()
    {
        return mouseKilled;
    }
    
    /**
     * 
     * @return The entire key released map
     */
    public HashMap<Short, Byte> getReleasedMap()
    {
        return this.releasedMap;
    }
    
    /**
     * 
     * @return The entire key pressed map
     */
    public HashMap<Short, Byte> getPressedMap()
    {
        return this.pressedMap;
    }

}
