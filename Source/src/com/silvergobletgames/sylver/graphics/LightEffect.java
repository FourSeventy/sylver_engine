package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.Effect;
import com.silvergobletgames.sylver.netcode.RenderData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import java.security.InvalidParameterException;
import java.util.HashMap;
import com.silvergobletgames.sylver.util.SylverVector2f;

/**
 *
 * @author Justin Capalbo
 */
public class LightEffect extends Effect
{
    // The image that this effect will modify 
    protected LightSource owningLight;
    //the type of render effect that this is
    protected LightEffect.LightEffectType lightEffectType;
    //start value
    private Object start;
    //end value
    private Object end;
    //should this renderEffect repeat
    protected boolean repeat;
    //delay
    protected int delay;
    //interpolators
    protected HashMap<String,LinearInterpolator> interpolators = new HashMap<>(); 
    
    //render effect type enum
    public static enum LightEffectType{
        SIZE(Number.class),COLOR(Color.class),DIRECTION(Number.class),INTENSITY(Number.class),RADIUS(Number.class),DURATION(Number.class);
        
        public Class type;
        
        LightEffectType(Class clas)
        {
            this.type = clas;
        }
        
    }
    
    //==============
    // Constructor
    //==============
    
    protected LightEffect()
    {
        
    }
    
    public LightEffect(LightEffect.LightEffectType type, int duration, Object start, Object end)
    {
        //test to see if input is the correct type
        try
        {
            type.type.cast(start);
            type.type.cast(end);
        }
        catch(ClassCastException e){ throw new InvalidParameterException("Input array is of the wrong type");}
        
        this.lightEffectType = type;         
        this.duration = duration; 
        this.start = start;
        this.end = end;
        
        //set up the interpolators
        if(type != LightEffect.LightEffectType.COLOR)
        {
           LinearInterpolator interpolator = new LinearInterpolator(((Number)start).floatValue(), ((Number)end).floatValue(), 1, duration);
           interpolators.put("interpolator", interpolator);
        }
        else
        {
            LinearInterpolator r = new LinearInterpolator(((Color)start).r, ((Color)end).r, 1, duration);
            LinearInterpolator g = new LinearInterpolator(((Color)start).g, ((Color)end).g, 1, duration);
            LinearInterpolator b = new LinearInterpolator(((Color)start).b, ((Color)end).b, 1, duration);
            interpolators.put("r", r);
            interpolators.put("g", g);
            interpolators.put("b", b);
        }
    }

    
    //===============
    // Effect Methods
    //===============
    
    public void onApply() 
    {
    }
    
    public void onRemove() 
    {
        if(lightEffectType == LightEffect.LightEffectType.DURATION)
        {
            this.owningLight.turnOff();
        }
        
    }
    
    public void update()
    {
        if(delay >= 0)
            delay--;
        else
        {
            //increment time elapsed
            timeElapsed++;

            //if our duration is up, set as expired
            if (timeElapsed > duration && !repeat) 
            {
                this.expired = true; 
                return;
            }
            else if(timeElapsed > duration && repeat)
            {
                timeElapsed = 1;
            }

            //adjust the correct amount of the images value
            this.adjustLight();
        }
                  
    }

    public LightEffect copy()
    {
        return new LightEffect(this.lightEffectType,duration,this.start,this.end);
    }
    
    private void adjustLight()
    {
        //switch through the different types of effects doing the correct adjustments     
        switch(lightEffectType)
        {
            case SIZE: 
            {   
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningLight.setSize(adjustmentValue);
                break;
            }
            case COLOR:
            {
                float r = (float)interpolators.get("r").interp(timeElapsed);
                float g = (float)interpolators.get("g").interp(timeElapsed);
                float b = (float)interpolators.get("b").interp(timeElapsed);      
                owningLight.setColor(new Color(r,g,b));              
                break;
            } 
            case DURATION: 
            {
                break;
            }
            case INTENSITY: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningLight.setIntensity(adjustmentValue);
                break;
            }
            case RADIUS: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningLight.setConicalRadius(adjustmentValue);
                break;
            }
            case DIRECTION:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningLight.setDirection(adjustmentValue);
                break;
            }
        }
    }

    
    
    //=============
    // Accessors
    //=============
    
    public void setOwningLight(LightSource l) {
        this.owningLight = l;
    }
    
    public void setRepeating(boolean value)
    {
        this.repeat = value;
    }
    
    public void setDelay(int delay)
    {
        this.delay = delay;
    }
    
    //==================
    // Saving Methods
    //==================
    
    public RenderData dumpRenderData() 
    {
         
        //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
                         
        RenderData renderData = new RenderData();
        
        renderData.data.add(0,"1");
        renderData.data.add(1,this.lightEffectType);
        renderData.data.add(2,this.duration);
        renderData.data.add(3,this.start);
        renderData.data.add(4,this.end);
        renderData.data.add(5,this.repeat);
        renderData.data.add(6,this.delay);

        
        return renderData;
     }
     
    public static LightEffect buildFromRenderData(RenderData renderData)
    {
        LightEffect effect = new LightEffect((LightEffect.LightEffectType)renderData.data.get(1),(int)renderData.data.get(2),renderData.data.get(3),renderData.data.get(4)); 
        effect.setRepeating((boolean)renderData.data.get(5));
        effect.setDelay((int)renderData.data.get(6));
        
        return effect;
    }
        

}
