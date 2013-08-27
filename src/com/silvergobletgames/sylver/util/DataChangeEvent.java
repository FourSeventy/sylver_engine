
package com.silvergobletgames.sylver.util;

import java.util.EventObject;

public class DataChangeEvent extends EventObject {
    
   public static final int ITEM_ADDED = 0;
   public static final int ITEM_REMOVED = 1;
    
   private int type;
   private Object item;
   private Object source;
   
   public DataChangeEvent(Object source, Object item) 
   {
        super(source);
        this.item = item;
   }
   
   /**
     * Returns the event type. The possible values are:
     * <ul>
     * <li> {@link #ITEM_ADDED}
     * <li> {@link #ITEM_REMOVED}
     * </ul>
     *
     * @return an int representing the type value
     */
    public int getType() { return type; }
    
    public Object getItem()
    {
        return item;
    }
    
    public Object getSource()
    {
        return source;
    }
       
}
