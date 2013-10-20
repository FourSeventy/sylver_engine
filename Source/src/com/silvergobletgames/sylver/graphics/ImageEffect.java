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
public class ImageEffect extends Effect
{
    // The image that this effect will modify 
    protected Image owningImage;
    //the type of render effect that this is
    protected ImageEffectType renderEffectType;
    //start value
    private Object start;
    //end value
    private Object end;
    //should this renderEffect repeat
    protected boolean repeat;
    //start delay
    protected int delay;
    //interpolators
    protected HashMap<String,LinearInterpolator> interpolators = new HashMap<>(); 
    
    //render effect type enum
    public static enum ImageEffectType{
        BRIGHTNESS(Number.class),ALPHABRIGHTNESS(Number.class),COLOR(Color.class),DURATION(Number.class),ROTATION(Number.class),
        XTRANSLATE(Number.class),YTRANSLATE(Number.class),SCALE(Number.class),WIDTH(Number.class),
        HEIGHT(Number.class),XOVERLAYTRANSLATE(Number.class), YOVERLAYTRANSLATE(Number.class);
        
        public Class type;
        
        ImageEffectType(Class clas)
        {
            this.type = clas;
        }
        
    }
    
    
    //==============
    // Constructor
    //==============
    
    protected ImageEffect()
    {
        
    }
    
    public ImageEffect(ImageEffectType type, int duration, Object start, Object end)
    {
        //test to see if input is the correct type
        try
        {
            type.type.cast(start);
            type.type.cast(end);
        }
        catch(ClassCastException e){ throw new InvalidParameterException("Input array is of the wrong type");}
        
        this.renderEffectType = type;         
        this.duration = duration; 
        this.start = start;
        this.end = end;
        
        
        //set up the interpolators
        if(type != ImageEffectType.COLOR)
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
        if(renderEffectType == ImageEffectType.DURATION)
        {
            //if the image has an owning scene remove it
            if(this.owningImage.getOwningScene() != null)            
                this.owningImage.getOwningScene().remove(this.owningImage);
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

            //if our duration is up, set effect as expired
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
            this.adjustImage();
        }
                  
    }

    public ImageEffect copy()
    {
        ImageEffect returnCopy = new ImageEffect(this.renderEffectType,duration,this.start,this.end);
        returnCopy.repeat = this.repeat;
        returnCopy.delay = this.delay;
        
        return returnCopy;
    }
    
    private void adjustImage()
    {
        //switch through the different types of effects doing the correct adjustments     
        switch(renderEffectType)
        {
            case BRIGHTNESS: 
            {   
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setBrightness(adjustmentValue);
                break;
            }
            case ALPHABRIGHTNESS: 
            {   
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setAlphaBrightness(adjustmentValue);
                break;
            }
            case COLOR:
            {
                float r = (float)interpolators.get("r").interp(timeElapsed);
                float g = (float)interpolators.get("g").interp(timeElapsed);
                float b = (float)interpolators.get("b").interp(timeElapsed);
                float a = (float)interpolators.get("a").interp(timeElapsed);          
                owningImage.setColor(new Color(r,g,b,a));              
                break;
            } 
            case DURATION: 
            {
                break;
            }
            case ROTATION: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setAngle(adjustmentValue);
                break;
            }
            case XTRANSLATE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setPosition(adjustmentValue,owningImage.getPosition().y);
                 break;
            }
            case YTRANSLATE:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setPosition(owningImage.getPosition().x,adjustmentValue);
                break;
            } 
            case XOVERLAYTRANSLATE:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                if(!owningImage.imageOverlays.values().isEmpty())
                {
                    Overlay lay = (Overlay)owningImage.getOverlay("interact");
                    lay.setRelativePosition(adjustmentValue, lay.getRelativePosition().y); 
                }
                break;
            }
            case YOVERLAYTRANSLATE:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                if(!owningImage.imageOverlays.values().isEmpty())
                {
                    Overlay lay = (Overlay)owningImage.getOverlay("interact");
                    lay.setRelativePosition(lay.getRelativePosition().x, adjustmentValue); 
                }
                break;
            }
            case SCALE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setScale(adjustmentValue);
                break;
            } 
            case WIDTH:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setDimensions(adjustmentValue,owningImage.getDimensions().y);
                break;
            }
            case HEIGHT:
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(timeElapsed);
                owningImage.setDimensions(owningImage.getDimensions().x,adjustmentValue);
                break;
            }

        }
    }

    
    //=============
    // Accessors
    //=============
    
    public void setOwningImage(Image i) {
        this.owningImage = i;
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
        renderData.data.add(1,this.renderEffectType);
        renderData.data.add(2,this.duration);
        renderData.data.add(3,this.start);
        renderData.data.add(4,this.end);
        renderData.data.add(5,this.repeat);
        renderData.data.add(6,this.delay);

        
        return renderData;
     }
     
    public static ImageEffect buildFromRenderData(RenderData renderData)
    {
        ImageEffect effect = new ImageEffect((ImageEffectType)renderData.data.get(1),(int)renderData.data.get(2),renderData.data.get(3),renderData.data.get(4)); 
        effect.setRepeating((boolean)renderData.data.get(5));
        effect.setDelay((int)renderData.data.get(6));
        
        return effect;
    }
        


    

}
