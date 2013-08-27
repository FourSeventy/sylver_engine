package com.silvergobletgames.sylver.netcode;


public class PacketSequencer
{
    
    private long nextSequenceNumber;
    
    
    public PacketSequencer()
    {
        nextSequenceNumber = 0;
    }
    
    
    public long nextSequenceNumber()
    {
        long returnValue = nextSequenceNumber;
        
        nextSequenceNumber++;
        
        return returnValue;
    }
    
    public void resetSequence()
    {
        nextSequenceNumber = 0;
    }
    
}
