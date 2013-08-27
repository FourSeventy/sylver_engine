package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.Effect;
import com.silvergobletgames.sylver.graphics.Anchorable.Anchor;
import com.silvergobletgames.sylver.netcode.RenderData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import java.security.InvalidParameterException;
import java.util.HashMap;
import com.silvergobletgames.sylver.util.SylverVector2f;

/**
 *
 * @author Justin Capalbo
 */
public class TextEffect extends Effect
{
    // The image that this effect will modify 
    protected Text owningText;
    //the type of render effect that this is
    protected TextEffect.TextEffectType textEffectType;
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
    public static enum TextEffectType{
        SCALE(Number.class),COLOR(Color.class),ANGLE(Number.class),XTRANSLATE(Number.class),YTRANSLATE(Number.class),DURATION(Number.class);
        
        public Class type;
        
        TextEffectType(Class clas)
        {
            this.type = clas;
        }
        
    }
    
    //==============
    // Constructor
    //==============
    
    protected TextEffect()
    {
        
    }
    
    public TextEffect(TextEffect.TextEffectType type, int duration, Object start, Object end)
    {
        //test to see if input is the correct type
        try
        {
            type.type.cast(start);
            type.type.cast(end);
        }
        catch(ClassCastException e){ throw new InvalidParameterException("Input is of the wrong type");}
        
        this.textEffectType = type;         
        this.duration = duration; 
        this.start = start;
        this.end = end;
        
        //set up the interpolators
        if(type != TextEffect.TextEffectType.COLOR)
        {
           LinearInterpolator interpolator = new LinearInterpolator(((Number)start).floatValue(), ((Number)end).floatValue(), 1, duration);
           interpolators.put("interpolator", interpolator);
        }
        else
        {
            LinearInterpolator r = new LinearInterpolator(((Color)start).r, ((Color)end).r, 1, duration);
            LinearInterpolator g = new LinearInterpolator(((Color)start).g, ((Color)end).g, 1, duration);
            LinearInterpolator b = new LinearInterpolator(((Color)start).b, ((Color)end).b, 1, duration);
            LinearInterpolator a = new LinearInterpolator(((Color)start).a, ((Color)end).a, 1, duration);
            interpolators.put("r", r);
            interpolators.put("g", g);
            interpolators.put("b", b);
            interpolators.put("a", a);
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
        if(textEffectType == TextEffect.TextEffectType.DURATION)
        {
            if(this.owningText.getOwningScene() != null)
               this.owningText.getOwningScene().remove(this.owningText);
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

            //if our duration is up, remove the effect
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
            this.adjustText();
        }
                  
    }

    public TextEffect copy()
    {
        return new TextEffect(this.textEffectType,duration,this.start,this.end);
    }
    
    private void adjustText()
    {
        //switch through the different types of effects doing the correct adjustments     
        switch(textEffectType)
        {
            case SCALE: 
            {   
                SylverVector2f beforeCenter = new SylverVector2f(owningText.getPosition().x + owningText.getWidth()/2,owningText.getPosition().y + owningText.getHeight()/2);
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningText.setScale(adjustmentValue);
                
                owningText.setPosition(beforeCenter.x - owningText.getWidth()/2, beforeCenter.y - owningText.getHeight()/2);
                break;
            }
            case COLOR:
            {
                float r = (float)interpolators.get("r").interp(timeElapsed);
                float g = (float)interpolators.get("g").interp(timeElapsed);
                float b = (float)interpolators.get("b").interp(timeElapsed); 
                float a = (float)interpolators.get("a").interp(timeElapsed); 
                owningText.setColor(new Color(r,g,b,a));              
                break;
            } 
            case DURATION: 
            {
                break;
            }
            case ANGLE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningText.setRotation(adjustmentValue);
                break;
            }
            case XTRANSLATE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningText.setPosition(adjustmentValue,owningText.getPosition().y);
                 break;
            }
            case YTRANSLATE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningText.setPosition(owningText.getPosition().x,adjustmentValue);
                break;
            }
        }
    }

    
    
    //=============
    // Accessors
    //=============
    
    public void setOwningText(Text l) {
        this.owningText = l;
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
        renderData.data.add(1,this.textEffectType);
        renderData.data.add(2,this.duration);
        renderData.data.add(3,this.start);
        renderData.data.add(4,this.end);
        renderData.data.add(5,this.repeat);
        renderData.data.add(6,this.delay);

        
        return renderData;
     }
     
    public static TextEffect buildFromRenderData(RenderData renderData)
    {
        TextEffect effect = new TextEffect((TextEffect.TextEffectType)renderData.data.get(1),(int)renderData.data.get(2),renderData.data.get(3),renderData.data.get(4)); 
        effect.setRepeating((boolean)renderData.data.get(5));
        effect.setDelay((int)renderData.data.get(6));
        
        return effect;
    }
        

}
