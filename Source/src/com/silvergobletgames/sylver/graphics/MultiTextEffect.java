
package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.netcode.RenderData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Mike
 */
public class MultiTextEffect extends TextEffect {
    
    //the points we will move between   
    private ArrayList points;
    //the durations
    private ArrayList<Integer> durations = new ArrayList();
    
    //current values
    private int currentIndex = 0 ;
    private int currentTimeElapsed = 0;
    
    
    //==============
    // Constructor
    //==============
    
    public MultiTextEffect(TextEffectType type, Object[] points, int[] durations)
    {
        //checking for parameter errors
        if(points.length != durations.length +1)
            throw new InvalidParameterException("One of the input arrays is an incorrect length.");
        
        //checking for parameter errors
        try
        {
            for(int i = 0; i < points.length; i++)
            type.type.cast(points[i]);
        }
        catch(ClassCastException e){ throw new InvalidParameterException("Input array is of the wrong type");}
        
        //set local variables
        this.textEffectType = type;
        this.points =  new ArrayList(Arrays.asList(points));
        
        //set the duration list
        for(int d: durations)
            this.durations.add(d);
        
        
        //calculate total dration
        for(Integer number: durations)
            this.duration += number;
        
        //build interpolators
        this.buildInterpolators(0);
        
    }
    
    //===============
    // Effect Methods
    //===============
    
    public void onApply() 
    {
    }
    
    public void onRemove() 
    {
        
    }
    
    public void update()
    {

      if(delay > 0)
            delay--;
        else
        {
            //increment time elapsed
            timeElapsed++;
            currentTimeElapsed++;

            //if we are on our final index, and its duration is up, remove the effect
            if ( currentIndex == durations.size() -1 && currentTimeElapsed > this.durations.get(currentIndex) && !repeat) 
            {
                if (owningText != null)            
                    this.expired = true; 

                return;
            }
            else if(currentIndex == durations.size() -1 && currentTimeElapsed > this.durations.get(currentIndex) && repeat)
            {
                currentIndex = 0;
                currentTimeElapsed = 1;
                timeElapsed = 1;
                this.buildInterpolators(0);
            }



            //if the current index is going to change, rebuild the interpolators
            if(currentTimeElapsed > this.durations.get(currentIndex))
            {
                currentIndex++;
                currentTimeElapsed = 1;
                buildInterpolators(currentIndex);

            }

            //adjust the correct amount of the images value
            this.adjustText();
            
        }

    }
    
    public MultiTextEffect copy()
    { 
        int[] array = new int[this.durations.size()];
        for(int i = 0;i < durations.size() ; i++)
            array[i] = durations.get(i);
        
        return new MultiTextEffect(this.textEffectType,points.toArray(),array); 
    }
    
    private void buildInterpolators(int index)
    {
        //set up the interpolators
        if(this.textEffectType != TextEffectType.COLOR)
        {
           LinearInterpolator interpolator = new LinearInterpolator(((Number)this.points.get(index)).floatValue(), ((Number)this.points.get(index + 1)).floatValue(), 1, this.durations.get(index));
           interpolators.put("interpolator", interpolator);
        }
        else
        {
            LinearInterpolator r = new LinearInterpolator(((Color)this.points.get(index)).r, ((Color)this.points.get(index+1)).r, 1, this.durations.get(index));
            LinearInterpolator g = new LinearInterpolator(((Color)this.points.get(index)).g, ((Color)this.points.get(index+1)).g, 1, this.durations.get(index));
            LinearInterpolator b = new LinearInterpolator(((Color)this.points.get(index)).b, ((Color)this.points.get(index+1)).b, 1, this.durations.get(index));
            LinearInterpolator a = new LinearInterpolator(((Color)this.points.get(index)).a, ((Color)this.points.get(index+1)).a, 1, this.durations.get(index));
            interpolators.put("r", r);
            interpolators.put("g", g);
            interpolators.put("b", b);
            interpolators.put("a", a);
        }
    }
    
    private void adjustText()
    {
        //switch through the different types of effects doing the correct adjustments     
        switch(textEffectType)
        {
            case SCALE: 
            {   
                SylverVector2f beforeCenter = new SylverVector2f(owningText.getPosition().x + owningText.getWidth()/2,owningText.getPosition().y + owningText.getHeight()/2);
                float adjustmentValue = (float)interpolators.get("interpolator").interp(currentTimeElapsed);
                owningText.setScale(adjustmentValue);
         
                
                owningText.setPosition(beforeCenter.x - owningText.getWidth()/2, beforeCenter.y - owningText.getHeight()/2);
                break;
            }
            case COLOR:
            {
                float r = (float)interpolators.get("r").interp(currentTimeElapsed);
                float g = (float)interpolators.get("g").interp(currentTimeElapsed);
                float b = (float)interpolators.get("b").interp(currentTimeElapsed);      
                float a = (float)interpolators.get("a").interp(currentTimeElapsed); 
                owningText.setColor(new Color(r,g,b,a));                  
                break;
            } 
            case DURATION: 
            {
                break;
            }
            case ANGLE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(currentTimeElapsed);
                owningText.setRotation(adjustmentValue);
                break;
            }
            case XTRANSLATE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(currentTimeElapsed);
                owningText.setPosition(adjustmentValue,owningText.getPosition().y);
                 break;
            }
            case YTRANSLATE: 
            {
                float adjustmentValue = (float)interpolators.get("interpolator").interp(currentTimeElapsed);
                owningText.setPosition(owningText.getPosition().x,adjustmentValue);
                break;
            }
        }
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
        
        renderData.data.add(0,"2");
        renderData.data.add(1,this.textEffectType);
        renderData.data.add(2,this.durations);
        renderData.data.add(3,this.points);
        renderData.data.add(4,this.repeat);
        renderData.data.add(5,this.delay);
        
        return renderData;
     }
     
    public static TextEffect buildFromRenderData(RenderData renderData)
    {
         int[] array = new int[((ArrayList<Integer>)renderData.data.get(2)).size()];
        for(int i = 0;i < ((ArrayList<Integer>)renderData.data.get(2)).size() ; i++)
            array[i] = ((ArrayList<Integer>)renderData.data.get(2)).get(i);
        
        MultiTextEffect effect = new MultiTextEffect((TextEffectType)renderData.data.get(1),((ArrayList)renderData.data.get(3)).toArray(),array); 
        effect.setRepeating((boolean)renderData.data.get(4));   
        effect.setDelay((int)renderData.data.get(5));
        return effect;
    }
    
}
