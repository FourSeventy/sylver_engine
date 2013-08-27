package com.silvergobletgames.sylver.netcode;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author mike
 */
public class RenderData implements Serializable
{    
    //data representing things about the object that are important for rendering
    public ArrayList<Object> data = new ArrayList<>();
    
    public RenderData(){}
}
