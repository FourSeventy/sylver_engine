package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.Scene;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import java.awt.Point;
import java.util.ArrayList;
import javax.media.opengl.GL3bc;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.media.opengl.GL2;
import javax.swing.event.EventListenerList;
import com.silvergobletgames.sylver.util.SylverVector2f;


public class Window extends SceneObject
{

    //the image that represents the background of the window
    protected Image background;
    
    //rendering position and dimentions
    protected float xPosition;
    protected float yPosition;    
    protected float width;
    protected float height;

    //bool that represents if the window is open
    protected boolean isOpen = true;
    //mouse is hovering boolean
    private boolean mouseHovering = false;
    
    //the components in the window
    protected ArrayList<WindowComponent> windowComponents = new ArrayList<>();  
    
    //event handling stuff
    protected EventListenerList listenerList = new EventListenerList();
    
    
    //================
    // Constructors
    //================
    /**
     * Constructs a Window
     * @param ref Reference to the location of the background image
     * @param xPosition X position of the menu
     * @param yPosition Y position of the menu
     */
    public Window(Image image, float xPosition, float yPosition, float width, float height)
    {
        super();
        
        //builds background image     
        background = image;
        background.setPosition(xPosition, yPosition); 
        background.setDimensions(width, height); 
        
        //sets x and y position of the window
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.width = width;
        this.height = height;       
    }
    
    
    //=====================
    // Scene Object Methods
    //=====================
     
    public void update()
    {
       if(this.isOpen())
       {
           //adjust the backgrounds position
           this.background.setPosition(this.xPosition, this.yPosition); 
           

           //update all of the components
           ArrayList<WindowComponent> updateList = new ArrayList<>(windowComponents);
           for(int i = updateList.size() -1; i >= 0; i--)
           {              
               updateList.get(i).setPosition( this.xPosition + updateList.get(i).xWindowRelative, this.yPosition + updateList.get(i).yWindowRelative);
               updateList.get(i).update();
           }
           
           //set some position variables we will check for firing events 
          
           
               Point mouseLocation = Game.getInstance().getInputHandler().getInputSnapshot().getScreenMouseLocation(); 

                //determine if we need to fire a mouseEntered event
                if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height && mouseHovering == false)
                {
                    this.fireAction(this, "mouseEntered");
                }


                //determine if we need to fire a mouseExited event
                if(!(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height) && mouseHovering == true)
                {

                    this.fireAction(this,"mouseExited");              
                }

                //update mouseHovered boolean
                if(mouseLocation.x >= this.xPosition && mouseLocation.x <= this.xPosition + this.width && mouseLocation.y >= this.yPosition && mouseLocation.y <= this.yPosition + this.height)
                    this.mouseHovering = true;
                else
                    this.mouseHovering = false;
           
       }
       
    }
    
    /**
     * Displays the changes made in update to the screen
     * @param g Graphics environment
     */
    public void draw(GL2 gl)
    {
        if(this.isOpen() )
        {           
            //draw background          
            background.draw(gl);          

            //draw components
            for(WindowComponent wc: windowComponents)       
                wc.draw(gl);       
        }
    }
    
    public SylverVector2f getPosition()
    {
        return new SylverVector2f((int)xPosition,(int)yPosition);
    }
    
    public void setPosition(float x, float y) 
    {
        this.xPosition = x;
        this.yPosition = y;
        
        //set position for all components
        ArrayList<WindowComponent> updateList = new ArrayList<>(windowComponents);
        for(int i = updateList.size() -1; i >= 0; i--)
        {
            updateList.get(i).setPosition( this.xPosition + updateList.get(i).xWindowRelative, this.yPosition + updateList.get(i).yWindowRelative);
        }
    }
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    @Override
    public void setOwningScene(Scene scene)
    {
        this.owningScene = scene;
        
        //set all components owningScenes
        for(WindowComponent component: this.windowComponents)
            component.setOwningScene(scene);
    }
    
    
    //=====================
    // Class Methods
    //=====================    

    public boolean isOpen()
    {
        return isOpen;
    }

    public void open()
    {
        this.isOpen = true;
    }
    
    public void close()
    {
        //make sure a final mouseExited gets thrown
        if(this.mouseHovering)
            this.fireAction(this,"mouseExited"); 
        
        this.isOpen = false;
        this.mouseHovering = false;
    }
    
    public void toggle()
    {
        if(isOpen)
            this.close();
        else
            this.open();
    }
  
    /**
     * Add a window component to the window
     * @param wc window component to be added
     */
    public void addComponent(WindowComponent wc)
    {
        wc.setPosition( this.xPosition + wc.xWindowRelative, this.yPosition + wc.yWindowRelative);
        windowComponents.add(wc);
        
        //sets components owning scene
        wc.setOwningScene(this.owningScene);
    }
    
    /**
     * Add a window component to the window in a certain position. This matters for rendering
     * @param position Position in the list that it will be added
     * @param wc Window component to add
     */
    public void addComponent(int position, WindowComponent wc)
    {
        wc.setPosition( this.xPosition + wc.xWindowRelative, this.yPosition + wc.yWindowRelative);
        windowComponents.add(position,wc);
        
        //sets components owning scene
        wc.setOwningScene(this.owningScene);
    }
    
    public ArrayList<WindowComponent> getComponentList()
    {
        return this.windowComponents;
    }
    
    public void removeComponent(WindowComponent c)
    {
        this.windowComponents.remove(c);
    }
    
    public void removeAllComponents()
    {
        this.windowComponents.clear();
    }
    
    public float getWidth()
    {
        return this.width;
    }
    
    public float getHeight()
    {
        return this.height;
    }
    
    public void setHeight(float height)
    {
        this.height = height;
        this.background.setDimensions(this.background.getWidth(), height);
    }
    
    public void setWidth(float width)
    {
        this.width = width;
        this.background.setDimensions(width,this.background.getHeight());
    }
    
    /*=======================
     * Event Handling Methods
     *=======================
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }
    
    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }
    
    protected void fireAction(Object source, String action)
    {
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                if (e == null) {
                    e = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, action);
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }
        }
    }
    
    
    //=========================
    // Render Data Functions
    //=========================
    
    public SceneObjectRenderData dumpRenderData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void reconcileRenderDataChanges(long lastTime, long futureTime, SceneObjectRenderDataChanges renderData)
    {   
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void interpolate(long currentTime)
    {
        
    }
  

 
}
