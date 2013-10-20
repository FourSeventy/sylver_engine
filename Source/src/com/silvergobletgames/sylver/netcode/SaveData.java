package com.silvergobletgames.sylver.netcode;

import java.io.Serializable;
import java.util.HashMap;

public class SaveData implements Serializable
{
    //defined serialVersionUID
    private static final long serialVersionUID = -6613182815517436645L;
    
    //map to hold our data
    public HashMap<String,Serializable> dataMap = new HashMap();    
}
