package com.silvergobletgames.sylver.netcode;

import com.silvergobletgames.sylver.core.SceneObject.SceneObjectClassMask;
import java.io.Serializable;


public class SceneObjectSaveData extends SaveData implements Serializable 
{
    //defined serialVersionUID
    private static final long serialVersionUID = -7091769739353068405L;      
    
    private SceneObjectSaveData(){} 
    public SceneObjectSaveData(SceneObjectClassMask classMask, String ID)
    {
        dataMap.put("class", (Serializable)classMask);
        dataMap.put("id", ID);
    }
	
}
