package com.silvergobletgames.sylver.core;

/**
 * Abstract class that is the base for all the effects in the engine.
 * Defines the abstract onApply(), onRemove(), and update(), functions,
 * as well as some base functionality
 * @author Mike
 */
public abstract class Effect
{  
    //name of the effect
    protected String name; 
    //total duration of the effect
    protected int duration;   
    //time elapsed
    protected int timeElapsed = 0; 
    //is expired
    protected boolean expired = false;
    
  
    
    //==================
    // Class Methods
    //==================
     
    public abstract void onApply();
    public abstract void onRemove();
    public abstract void update();
    
    /**
     * Returns if this effect is expired or not
     * @return true if the effect is expired
     */
    public final boolean isExpired()
    {
        return this.expired;
    }
    
    /**
     * Get a copy of this effect.  Defined differently for each effect.  If copy
     * isn't defined all the way down, may make things funky.
     * @return 
     */
    public abstract Effect copy();
    
    
    //======================
    // Accessor Methods
    //======================
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public int getDuration()
    {
        return this.duration;
    }
    
    
    
    
    
    
}
