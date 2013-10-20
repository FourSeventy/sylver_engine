
package com.silvergobletgames.sylver.util;

import java.util.ArrayList;
import javax.swing.event.EventListenerList;


/**
 *
 * @author mike
 */
public class ChangeAlertList<E>  extends ArrayList<E>{

    protected EventListenerList listenerList = new EventListenerList();
    
    
    public ChangeAlertList()
    {
        
    }
    
    public ChangeAlertList(ChangeAlertList old)
    {
        super(old);
    }
    
    public void addDataChangeListener(DataChangeListener l) {
        listenerList.add(DataChangeListener.class, l);
    }
    
    public void removeDataChangeListener(DataChangeListener l) {
        listenerList.remove(DataChangeListener.class, l);
    }
    
    public DataChangeListener[] getListDataListeners() {
        return listenerList.getListeners(DataChangeListener.class);
    }
    
    protected void fireItemAdded(Object source, E item)
    {
        Object[] listeners = listenerList.getListenerList();
        DataChangeEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DataChangeListener.class) {
                if (e == null) {
                    e = new DataChangeEvent(source, item);
                }
                ((DataChangeListener)listeners[i+1]).itemAdded(e);
            }
        }
    }
    
     protected void fireItemRemoved(Object source, E item)
    {
        Object[] listeners = listenerList.getListenerList();
        DataChangeEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == DataChangeListener.class) {
                if (e == null) {
                    e = new DataChangeEvent(source, item);
                }
                ((DataChangeListener)listeners[i+1]).itemRemoved(e);
            }
        }
    }

   @Override
    public boolean add(E element){
       this.fireItemAdded(this, element);
        return super.add(element);
        
    }
    
    @Override
    public void add(int index, E element)
    {
        this.fireItemAdded(this, element);
        super.add(index,element);
    }
    
    @Override
    public E remove(int index)
    {
        E item = super.remove(index);
        this.fireItemRemoved(this, item);
        return item;
    }
       
    @Override
    public boolean remove(Object o){
        this.fireItemRemoved(this, (E)o);
        return super.remove((E)o);
      
    }      

    
}
