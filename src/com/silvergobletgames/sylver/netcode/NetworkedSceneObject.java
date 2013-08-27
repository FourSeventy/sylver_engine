
package com.silvergobletgames.sylver.netcode;

import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import java.util.HashMap;


public abstract class NetworkedSceneObject extends SceneObject
{
    //interpolators that all scene objects will use for rendering
    protected HashMap<String,LinearInterpolator> interpolators = new HashMap<>(); //TODO: move into networked scene object
    
    
    //=============================
    // Networking Specific Methods
    //=============================
    public abstract SceneObjectRenderData dumpRenderData();
    
    //public static SceneObject buildFromRenderData(SceneObjectRenderData data);
    
    public abstract SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData);
    
    public abstract void reconcileRenderDataChanges(long lastTime, long futureTime, SceneObjectRenderDataChanges renderDataChanges);
    
    public abstract void interpolate(long currenttime);
    
}
