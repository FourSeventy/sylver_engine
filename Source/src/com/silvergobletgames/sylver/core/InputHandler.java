package com.silvergobletgames.sylver.core;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Cursor;
import com.silvergobletgames.sylver.graphics.Image;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * singleton class that handles input
 * @author mike
 */
public class InputHandler implements MouseListener,KeyListener 
{

    //input snapshot
    private InputSnapshot inputSnapshot;
    
    //typed key list
    private CopyOnWriteArrayList<Character> typedKeys = new CopyOnWriteArrayList<>();   
    //pressed map
    private ConcurrentHashMap<Short, Byte> pressedMap = new ConcurrentHashMap<>();
    //released map
    private ConcurrentHashMap<Short, Byte> releasedMap = new ConcurrentHashMap<>();
    //screen mouse position
    private Point screenMouseLocation = new Point(0,0); //TODO - protect this with a semaphore
    
    //mouse variables
    private boolean mouseDown = false; 
    private boolean mouseClicked = false;
    private boolean mouseMoved= false;   
    private int mouseWheelRotation = 0;
    private int mouseButtonClicked;
    private boolean mouseKilled;
  
    //================
    // Constructor
    //================
    protected InputHandler()
    {                 
        //set a default inputSnapshot so we always have valid input
        this.takeInputSnapshot();
   
    }
    
    
    //===========
    // Methods
    //===========
    
    /**
     * Takes an input snapshot, and clears all buffered input
     */
    protected void takeInputSnapshot()
    {
        //take input snapshot
        this.inputSnapshot = new InputSnapshot(new ArrayList(this.typedKeys),new HashMap(this.releasedMap), new HashMap(this.pressedMap), mouseDown, mouseClicked, mouseMoved, mouseWheelRotation, mouseButtonClicked,this.mouseKilled, screenMouseLocation);
       
        //============
        //clear input
        //============
        
        //clear typed keys
        this.typedKeys.clear();
        
        //clear released keys
        releasedMap.clear();
               
       //clear mouse bools
        mouseMoved= false;
        mouseClicked = false;
        mouseKilled = false;  
        
   
    }
    
    /**
     * Returns the latest input snapshot.
     * @return Latest InputSnapshot
     */
    public InputSnapshot getInputSnapshot()
    {
        return this.inputSnapshot;
    }
    

    //===================
    //Key Event Handlers
    //===================
 
    public void keyPressed(KeyEvent e)
    {
        //first press
        if(!e.isAutoRepeat()) 
        {
            //build modifier bitmask
            byte modifierMask = 0b0000_0000;           
            if(e.isShiftDown())
            {
               modifierMask += 0b000_0001; 
            }
            if(e.isControlDown())
            {
                modifierMask += 0b000_0010;
            }
            if(e.isAltDown())
            {
                modifierMask += 0b000_0100;
            }
            
            //store key press
            pressedMap.put(e.getKeySymbol(),modifierMask);	
        }
        
        //key typed
        if( e.isPrintableKey() )  
        {           
            typedKeys.add(e.getKeyChar());         
        }
    }

  
    
    public void keyReleased(KeyEvent e)
    {
       
        //first release
        if(!e.isAutoRepeat())
        {
            //remove from pressed map
            pressedMap.remove(e.getKeySymbol());
           
             //build modifier bitmask
            byte modifierMask = 0b0000_0000;           
            if(e.isShiftDown())
            {
               modifierMask += 0b000_0001; 
            }
            if(e.isControlDown())
            {
                modifierMask += 0b000_0010;
            }
            if(e.isAltDown())
            {
                modifierMask += 0b000_0100;
            }
            
            //store key release
            releasedMap.put(e.getKeySymbol(),modifierMask);
        }    
        
    }


    
    public void mouseClicked(MouseEvent e)
    {
        this.mouseClicked = true;
        this.mouseButtonClicked = e.getButton();  
    }

   
    public void mouseEntered(MouseEvent e)
    {

        Cursor cursor = Game.getInstance().getGraphicsWindow().getCursor();
        if (cursor != null)
        {
            Game.getInstance().getGraphicsWindow().setPointerVisible(false);  
            cursor.getImage().setColor(new Color(cursor.getImage().getColor().r, cursor.getImage().getColor().g, cursor.getImage().getColor().b, 1)); 
        }
        
    }

   
    public void mouseExited(MouseEvent e)
    {

        Game.getInstance().getGraphicsWindow().setPointerVisible(true);
        Cursor cursor = Game.getInstance().getGraphicsWindow().getCursor();
        if (cursor != null )
            cursor.getImage().setColor(new Color(cursor.getImage().getColor().r, cursor.getImage().getColor().g, cursor.getImage().getColor().b, 0));
        
    }

    
    public void mousePressed(MouseEvent e)
    {
        this.mouseDown = true;
        this.mouseButtonClicked = (int)e.getButton();
    }

    
    public void mouseReleased(MouseEvent e)
    {
        this.mouseClicked = true;
        this.mouseDown = false;
    }

    
    public void mouseMoved(MouseEvent e)
    {
        this.mouseMoved = true; 
        
        //set the screenMouseLocation
        this.setScreenMouseLocation( new Point(e.getX(),e.getY()));
    }

   
    public void mouseDragged(MouseEvent e)
    {
         //set the screenMouseLocation
          this.setScreenMouseLocation( new Point(e.getX(),e.getY()));
    }

    
    public void mouseWheelMoved(MouseEvent e)
    {
        this.mouseWheelRotation = (int)e.getRotation()[1];
    }
    
    //private helper method
    private void setScreenMouseLocation(Point location)
    {

        float x = location.x;       
        float y = Game.getInstance().getGraphicsWindow().getHeight() - location.y;

        //normalize to the aspect ratio       
        float xfinal = x/ Game.getInstance().getGraphicsWindow().getWidth() * Game.getInstance().getGraphicsWindow().getCurrentAspectRatio().x;
        float yfinal = y/ Game.getInstance().getGraphicsWindow().getHeight() * Game.getInstance().getGraphicsWindow().getCurrentAspectRatio().y;

        this.screenMouseLocation = new Point((int)xfinal,(int)yfinal);
        
    }
}
