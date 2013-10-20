package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import javax.swing.event.EventListenerList;
import com.silvergobletgames.sylver.util.SylverVector2f;

public abstract class WindowComponent extends SceneObject 
{

    //position and size of the component
    protected float xWindowRelative;
    protected float yWindowRelative;
    protected float width;
    protected float height;
    
    //event listener list
    protected EventListenerList listenerList = new EventListenerList();
    
    //hidden
    protected boolean hidden;
    
    //disabled
    protected boolean disabled;
    
    
    //==================
    // Constructors
    //==================
    
    public WindowComponent(float x, float y)
    {        
        this.xWindowRelative = x;
        this.yWindowRelative = y;
        super.setPosition(x, y);
    }
    
    
    //=====================
    // Scene Object Methods
    //=====================
       
    public abstract void draw(GL2 gl);
       
    public abstract void update();        
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    
   //=====================
   // Class Methods
   //=====================
       
    public float getWidth()
    {
        return width;
    }
    
    public void setWidth(float w)
    {
        this.width = w;
    }
    
    public float getHeight()
    {
        return height;
    }
    
    public void setHeight(float h)
    {
        this.height = h;
    }
    
    public SylverVector2f getWindowRelativePosition()
    {
        return new SylverVector2f(this.xWindowRelative,this.yWindowRelative);
    }
    
    public void setWindowRelativePosition(float x, float y)
    {
        this.xWindowRelative = x;
        this.yWindowRelative = y;
    }
    
    public boolean isHidden()
    {
        return this.hidden;
    }
    
    public void setHidden(boolean h)
    {
        this.hidden = h;
    }
    
    public boolean isDisabled()
    {
        return this.disabled;
    }
    
    public void setDisabled(boolean b)
    {
        this.disabled = b;
    }
    
    
    
    //=======================
    // Event Handling Methods
    //=======================
     
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
    
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }
    
    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }
    
    public void fireAction(Object source, String action)
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
    
}
