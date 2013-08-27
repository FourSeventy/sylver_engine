package com.silvergobletgames.sylver.netcode;


public abstract class Packet
{
    //sequence number
    protected long sequenceNumber;
    
    //ack number
    protected long ackNumber;
    //ack bitfield
    protected byte ackBits;
    


    public Packet()
    {
        
    }   
    
    public void setSequenceNumber(long number)
    {
        this.sequenceNumber = number;
    }
    
    public long getSequenceNumber()
    {
        return this.sequenceNumber;
        
    }
    
    public long getAck()
    {
        return this.ackNumber;
    }
    
    public byte getAckBitfield()
    {
        return this.ackBits;
    }
    
    public void setAck(long ack)
    {
        this.ackNumber = ack;
    }
    
    public void setAckBitfield(byte bit)
    {
        this.ackBits = bit;
    }
    
}
