package com.silvergobletgames.sylver.netcode;

import java.util.UUID;


public class ClientPacket extends Packet
{   
    //clients uuid
    protected UUID clientID;
    
    public ClientPacket()
    {
        super();
    }
    
    public void setClientID(UUID id)
    {
        this.clientID = id;
    }
    
    public UUID getClientID()
    {
        return clientID;
    }

}
