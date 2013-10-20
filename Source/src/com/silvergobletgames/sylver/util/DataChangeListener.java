
package com.silvergobletgames.sylver.util;

import java.util.EventListener;

public interface DataChangeListener extends EventListener {
    
   
    public void itemAdded(DataChangeEvent e );
    public void itemRemoved(DataChangeEvent e);
   

}
