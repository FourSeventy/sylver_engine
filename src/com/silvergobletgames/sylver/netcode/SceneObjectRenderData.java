package com.silvergobletgames.sylver.netcode;

import com.silvergobletgames.sylver.core.SceneObject.SceneObjectClassMask;
import java.io.Serializable;


public class SceneObjectRenderData extends RenderData implements Serializable
{     
    //class the sceneObjectData represents
    private SceneObjectClassMask sceneObjectClass;
    
    //ID of the Object it represents
    private String ID;   

    private SceneObjectRenderData(){}
    
    public SceneObjectRenderData(SceneObjectClassMask klass,String ID)
    {
        this.sceneObjectClass = klass;
        this.ID = ID;
    }
    
    public SceneObjectClassMask getSceneObjectClass()
    {
        return sceneObjectClass;
    }
    
    public String getID()
    {
        return ID;
    }
         
}
