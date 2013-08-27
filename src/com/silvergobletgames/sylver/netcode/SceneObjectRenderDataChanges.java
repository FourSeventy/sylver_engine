package com.silvergobletgames.sylver.netcode;


public class SceneObjectRenderDataChanges 
{
    
    //ID of the Object it represents
    public String ID;
    
    //the fields that are in the changed data array
    public int fields; 
    
    //the actual changed data
    public Object[] data;
    
    //no args constructor for serialization
    public SceneObjectRenderDataChanges(){}
          
}
