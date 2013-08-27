package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.core.SceneObjectManager;
import com.silvergobletgames.sylver.graphics.LightEffect.LightEffectType;
import com.silvergobletgames.sylver.netcode.*;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import com.silvergobletgames.sylver.util.SerializableEntry;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.Iterator;
import java.util.Map;

public class LightSource extends NetworkedSceneObject implements SavableSceneObject
{
    
    private SylverVector2f position;
    private float size;
    protected float conicalRadius; //stored as radians
    protected float direction; //stored as radians 
    private float intensity;
    private Color color;
    private boolean on = true;
    
    // The list of LightEffects
    private HashMap<String,LightEffect> lightEffects = new HashMap();

    
    //=====================
    // Constructors
    //=====================     
    
    /**
     * Constructs a new light source with its options set to default values;
     */
    public LightSource()
    {
        
        
        this.position = new SylverVector2f();   
        this.size = 100;
        this.conicalRadius = (float)(360 * Math.PI / 180);
        this.direction = 0;
        this.intensity = 1;
        this.color = new Color(1,1,1,1);
        
        this.addToGroup(CoreGroups.LIGHTSOURCE);
    }
    
    
    
    //=====================
    // Scene Object Methods
    //=====================
      
    public void update()
    {
        //update ImageEffects and remove them if expired   
        for (Iterator<Map.Entry<String,LightEffect>> it = this.lightEffects.entrySet().iterator(); it.hasNext();) 
        {
            //get image effect from iterator
            Map.Entry<String,LightEffect> entry = it.next();
            LightEffect effect = entry.getValue();
            
            //update the effect
            effect.update();  
            
            //if expired, remove it
            if(effect.isExpired())
            {   
                //remove effect
                it.remove();
                
                //fire effects on remove
                effect.onRemove(); 
                                
            }
        }
    }
    
    public void draw(GL2 gl)
    {
        
    }
    
    public SylverVector2f getPosition() 
    {
        return this.position;
    }
  
    public void setPosition(float x, float y)
    {
        this.position = new SylverVector2f(x,y);
    }
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    
    ///=====================
    // Class Methods
    //=====================
       
    /**
     * This will turn the light to face towards the given point.
     * @param point Point you want the light to face towards.
     */
    public void faceTowardsPoint(Point point)
    {  
        //make reference vector pointing to the right of the light
        SylverVector2f reference = new SylverVector2f(1, 0);
        
        //make the vector pointing to the point provided
        SylverVector2f pointVector = new SylverVector2f(point.x - position.x,point.y - position.y);
        
        reference.normalise();
        pointVector.normalise();

        
        this.direction = (float)(Math.acos(pointVector.dot(reference)));
      
        if(point.y < position.y)
            direction = (float)(Math.PI * 2) - direction;
        
    }
       
    public void toggle()
    {
        on = !on;
    }
    
    public void turnOn()
    {
        on = true;
    }
    
    public void turnOff()
    {
        on = false;
       
    }
    
    public boolean isOn()
    {
        return on;
    }
     
    /**
     * Adds a light effect to this light without a key. Its key will be set to the effects hashCode
     * @param effect The effect to add
     */
    public boolean addLightEffect(LightEffect effect)
    {
        return this.addLightEffect(Integer.toString(effect.hashCode()),effect);
    }
    
    /**
     * Adds a light effect to this light with an explicit key.
     * @param name The key that this effect can be referenced with
     * @param effect The effect to add
     */
    public boolean addLightEffect(String name, LightEffect effect)
    {
        //if we dont have a render efect of the same type already add it
        if(this.hasLightEffectType(effect.lightEffectType) == false)
        {
            //add the effect
            this.lightEffects.put(name,effect);

            //set the render effects owning image
            effect.setOwningLight(this);

            //tell the effect that it was applied
            effect.onApply();
            return true;
        }
        else 
            return false;
    }
    
    /**
     * Removes the given light effect from the light
     * @param effect The effect to remove
     */
    public void removeLightEffect(LightEffect effect)
    {
        //remove the effect
        for(String key:this.lightEffects.keySet())
        {
           LightEffect keyEffect = this.lightEffects.get(key);
           
           if(keyEffect == effect)
           {
               this.removeLightEffect(key);
               return;
           }
               
        }
    }
    
    /**
     * Removes the light effect associated to the given key from the light
     * @param key Key associated ot the render effect we want to remove
     */
    public void removeLightEffect(String key)
    {
        //remove the effect
        LightEffect effect = this.lightEffects.remove(key);
        
        //tell the effect that it was removed
        if(effect != null)
            effect.onRemove();   
      
    }
    
    /** 
     * Removes all light effects on this light. 
     */
    public void removeAllLightEffects() 
    {
        //call onRemove() for all the effects
        for (LightEffect effect: lightEffects.values())         
            effect.onRemove();
        
        //clear the list
        lightEffects.clear();
        
    }
    
    public boolean hasLightEffectType(LightEffectType type)
    {
        for(LightEffect effect: this.lightEffects.values())
        {
            switch(type)
            {
                case SIZE: if(effect.lightEffectType.equals(LightEffectType.SIZE)){return true;} break;
                case COLOR: if(effect.lightEffectType.equals(LightEffectType.COLOR)){return true;} break;
                case DIRECTION: if(effect.lightEffectType.equals(LightEffectType.DIRECTION)){return true;} break;
                case INTENSITY: if(effect.lightEffectType.equals(LightEffectType.INTENSITY)){return true;} break;
                case RADIUS: if(effect.lightEffectType.equals(LightEffectType.RADIUS)){return true;} break;
             }
        }
        return false;
    }
    
    
    
    //=============
    // Accessors
    //=============
    
    /**
     * Returns the size of the light
     * @return The size
     */
    public float getSize()
    {
        return this.size;
    }
    
     /**
     * Sets the size of the light
     * @param s new size for the light
     */
    public void setSize(float s)
    {
        this.size = s;
    }
    
    /**
     * Returns the conical radius of the light
     * @return The conical radius in degrees
     */
    public float getConicalRadius()
    {
        return (float)(this.conicalRadius * 180 / Math.PI);
    }
    
    /**
     * Sets the conical radius for the light
     * @param radius Radius in degrees
     */
    public void setConicalRadius(float radius)
    {
        this.conicalRadius = (float)(radius * Math.PI / 180);
    }
    
    /**
     * Returns the direction of the light
     * @return The direction in degrees
     */
    public float getDirection()
    {
        return (float)(this.direction * 180 /Math.PI);
    }
    
    /**
     * Direction for the light to face in degrees
     * @param direction 
     */
    public void setDirection(float direction)
    {
        this.direction = (float)(direction * Math.PI / 180);
    }
    
    /**
     * returns the intensity of the light
     * @return the intensity
     */
    public float getIntensity()
    {
        return this.intensity;
    }

    /**
     * Sets the intensity of the light
     * @param intensity 
     */
    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }
    
    /**
     * Returns the color of the light
     * @return 
     */
    public Color getColor()
    {
        return this.color;
    }
    
    /**
     * Set the color of the light
     * @param c 
     */
    public void setColor(Color c)
    {
        this.color = new Color(c);
    }
    
    
    //===================
    //Render Data Methods
    //===================
    
    public SceneObjectRenderData dumpRenderData() 
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.LIGHTSOURCE,this.ID);

        renderData.data.add(0,this.getPosition().x);
        renderData.data.add(1,this.getPosition().y);
        renderData.data.add(2, size);
        renderData.data.add(3, conicalRadius); //Convert for constructor
        renderData.data.add(4, direction);
        renderData.data.add(5, intensity);
        renderData.data.add(6, color.r);
        renderData.data.add(7, color.g);
        renderData.data.add(8, color.b);
        renderData.data.add(9,on);
        //render data for all the effects
         ArrayList<SerializableEntry> renderEffectData = new ArrayList();
         for(String key: this.lightEffects.keySet()){
             renderEffectData.add(new SerializableEntry(key,this.lightEffects.get(key).dumpRenderData()));
         }
         renderData.data.add(10,renderEffectData);
         
        return renderData;
    }
    
    public static LightSource buildFromRenderData(SceneObjectRenderData renderData)
    {
        SylverVector2f position = new SylverVector2f((float)renderData.data.get(0),(float)renderData.data.get(1));
        float size = (float)renderData.data.get(2);
        float conicalRadius = (float)renderData.data.get(3);
        float direction = (float)renderData.data.get(4);
        float intensity = (float)renderData.data.get(5);
        Color color = new Color((float)renderData.data.get(6),(float)renderData.data.get(7),(float)renderData.data.get(8));
        boolean on = (boolean)renderData.data.get(9);
        
        LightSource source = new LightSource();
        source.size = size;
        source.position = position;
        source.conicalRadius = conicalRadius;
        source.direction = direction;
        source.intensity = intensity;
        source.color = color;
        if(on)
            source.turnOn();
        else
            source.turnOff();
        
        //lightEffects
        ArrayList<SerializableEntry> lightEffectData = (ArrayList)renderData.data.get(10);
        for (SerializableEntry<String, RenderData> lightEffectEntry : lightEffectData)
        {
            //build either a lightEffect, or a MultiLightEffect
            LightEffect effect;
            if(lightEffectEntry.getValue().data.get(0).equals("1"))            
                effect = LightEffect.buildFromRenderData(lightEffectEntry.getValue());                     
            else
                effect = MultiLightEffect.buildFromRenderData(lightEffectEntry.getValue());
            
            source.addLightEffect(lightEffectEntry.getKey(), effect);
        }
        
        source.setID(renderData.getID());
        
        return source;
    }
    
    public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData)
    {
        SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
        int changeMap = 0;
        changes.ID = this.ID;
        ArrayList changeList = new ArrayList();
        
        //Non effect fields
        forloop: for(int i = 0; i < 10; i++)
        {
            //check for effects on fields and dont generate changes for those fields
            switch(i)
            {
                case 2: if(this.hasLightEffectType(LightEffectType.SIZE)){ continue forloop;} break; //size 
                case 3: if(this.hasLightEffectType(LightEffectType.RADIUS)){ continue forloop;} break; //radius
                case 4: if(this.hasLightEffectType(LightEffectType.DIRECTION)){ continue forloop;} break; //direction
                case 5: if(this.hasLightEffectType(LightEffectType.INTENSITY)){ continue forloop;} break; //intensity
                case 6: if(this.hasLightEffectType(LightEffectType.COLOR)){ continue forloop;} break; //color r
                case 7: if(this.hasLightEffectType(LightEffectType.COLOR)){ continue forloop;} break; //color b
                case 8: if(this.hasLightEffectType(LightEffectType.COLOR)){ continue forloop;} break; //color g
            }
            
            if(!oldData.data.get(i).equals( newData.data.get(i)))
            {
                changeList.add(newData.data.get(i));
                changeMap += 1 << i;
            }
        }
        
        //====================================
        //generate changes for Render Effects 
        //====================================
        
        ArrayList<SerializableEntry<String,SceneObjectRenderData>> effectAdds = new ArrayList();
        ArrayList<String> effectRemoves = new ArrayList();
        ArrayList<SerializableEntry> oldRenderData = (ArrayList)oldData.data.get(10);
        ArrayList<SerializableEntry> newRenderData = (ArrayList)newData.data.get(10);
        
        HashMap<String, SceneObjectRenderData> oldRenderMap = new HashMap();
        for (SerializableEntry<String, SceneObjectRenderData> entry : oldRenderData)
            oldRenderMap.put(entry.getKey(), entry.getValue());
        
        HashMap<String, SceneObjectRenderData> newRenderMap = new HashMap();
        for (SerializableEntry<String, SceneObjectRenderData> entry : newRenderData)
            newRenderMap.put(entry.getKey(), entry.getValue());
        
        
        //check for additions
        for (String newKey: newRenderMap.keySet())
        {
            if(!oldRenderMap.containsKey(newKey))
                effectAdds.add(new SerializableEntry(newKey,(RenderData)newRenderMap.get(newKey)));
        }
        
        //check for subtractions only if the duration is infinite
        for(String oldKey: oldRenderMap.keySet())
        {
            if(!newRenderMap.containsKey(oldKey))
                effectRemoves.add(oldKey);
        }
        
         if(!effectAdds.isEmpty())
        {
            changeList.add(effectAdds);
            changeMap += 1L << 10;
        }
        if(!effectRemoves.isEmpty())
        {
            changeList.add(effectRemoves);
            changeMap += 1L << 11;
        }
        
        changes.fields = changeMap;
        changes.data = changeList.toArray();
        
        if(changeList.size() > 0)
            return changes;
        else
            return null;
            
    }
    
    public void reconcileRenderDataChanges(long lastTime, long futureTime, SceneObjectRenderDataChanges renderDataChanges)
    {
        //construct an arraylist of data that we got, nulls will go where we didnt get any data
        int fieldMap = renderDataChanges.fields;
        ArrayList rawData = new ArrayList();
        rawData.addAll(Arrays.asList(renderDataChanges.data));        
        ArrayList changeData = new ArrayList();
        for(int i = 0; i <12; i ++)
        {
            // The bit was set
            if ((fieldMap & (1L << i)) != 0)
            {
                changeData.add(rawData.get(0));
                rawData.remove(0);
            }
            else
                changeData.add(null);          
        }


        //x position interpolator
        if(changeData.get(0) != null)
        {
            float startPosition=  interpolators.containsKey("xPosition")? (float)interpolators.get("xPosition").getB() : this.getPosition().x;          
            LinearInterpolator lerp1= new LinearInterpolator(startPosition,(float)changeData.get(0),lastTime,futureTime);
            this.interpolators.put("xPosition",lerp1);
        }
        else
            this.interpolators.remove("xPosition");

        //y position interpolator
        if(changeData.get(1) != null)
        {
            float startPosition=  interpolators.containsKey("yPosition")? (float)interpolators.get("yPosition").getB() : this.getPosition().y;
            LinearInterpolator lerp2= new LinearInterpolator(startPosition,(float)changeData.get(1),lastTime,futureTime);
            this.interpolators.put("yPosition",lerp2);
        }
        else
            this.interpolators.remove("yPosition");
        
        //direction interpolator
        if(changeData.get(4) != null)
        {
           float startPosition=  interpolators.containsKey("direction")? (float)interpolators.get("direction").getB() : this.getDirection();
            LinearInterpolator lerp3= new LinearInterpolator(startPosition,(float)changeData.get(4),lastTime,futureTime);
            this.interpolators.put("direction", lerp3);
        }
        else
            this.interpolators.remove("direction"); 

            
        //reconcile variables
        if(changeData.get(2) != null)
            this.size = (float)changeData.get(2);
        if(changeData.get(3) != null)
            this.conicalRadius = (float)changeData.get(3);      
        if(changeData.get(5) != null)
            this.intensity = (float)changeData.get(5);
        if(changeData.get(6) != null)
            this.color.r = (float)changeData.get(6);
        if(changeData.get(7) != null)
            this.color.g = (float)changeData.get(7);
        if(changeData.get(8) != null)
            this.color.b = (float)changeData.get(8);
        if(changeData.get(9) != null)
        {
             if((boolean)changeData.get(9))
                this.turnOn();
            else
                this.turnOff();
        }
        
        //added renderEffects
        if(changeData.get(10) != null)
        {
             ArrayList<SerializableEntry<String, RenderData>> effectAdds = (ArrayList)changeData.get(10);
            for (SerializableEntry<String, RenderData> entry : effectAdds)
            {
                LightEffect effect;
                String key = entry.getKey();
                
                if(entry.getValue().data.get(0).equals("1"))            
                    effect = LightEffect.buildFromRenderData(entry.getValue());                     
                else
                    effect = MultiLightEffect.buildFromRenderData(entry.getValue());
                
                this.addLightEffect(key, effect);
                              
            }           
        }
        
        //removed lightEffects
        if(changeData.get(11) != null)
        {
            ArrayList<String> removeList = (ArrayList<String>)changeData.get(11);
            
            for(String s: removeList)
            {
                //only remove effects that are set to repeat
                if(this.lightEffects.get(s) != null && this.lightEffects.get(s).repeat)
                    this.removeLightEffect(s);
            }
        }
    }
    
    public void interpolate(long currentTime)
    {
        if(!this.interpolators.isEmpty())
        {
            //interpolate x and y positions
            float x = this.getPosition().x;
            float y = this.getPosition().y;

            if(this.interpolators.containsKey("xPosition") && currentTime <= this.interpolators.get("xPosition").getb())
            x = (float)this.interpolators.get("xPosition").interp(currentTime);

            if(this.interpolators.containsKey("yPosition") && currentTime <= this.interpolators.get("yPosition").getb())
            y = (float)this.interpolators.get("yPosition").interp(currentTime);

            this.setPosition(x, y);
            
            if(this.interpolators.containsKey("direction") && currentTime <= this.interpolators.get("direction").getb() )
                this.direction = (float)this.interpolators.get("direction").interp(currentTime);
        }
    }
    
    
    
    
    //====================
    // Saving Methods
    //====================
        
    
    public SceneObjectSaveData dumpFullData() 
    {
        //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
        SceneObjectSaveData saved = new SceneObjectSaveData(CoreClasses.LIGHTSOURCE,this.ID);
        
        saved.dataMap.put("x", this.getPosition().x);
        saved.dataMap.put("y", this.getPosition().y);
        saved.dataMap.put("size", size);
        saved.dataMap.put("conical", (float)(conicalRadius * 180.0 / Math.PI)); //Convert for constructor
        saved.dataMap.put("direction", direction * 180/ (float)Math.PI);
        saved.dataMap.put("intensity", intensity);
        saved.dataMap.put("r", color.r);
        saved.dataMap.put("g", color.g);
        saved.dataMap.put("b", color.b);
        return saved;
    }
    

    public static SceneObject buildFromFullData(SceneObjectSaveData saved) 
    {
        //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
        float x = (Float)saved.dataMap.get("x");
        float y = (Float)saved.dataMap.get("y");
        float size = (Float)saved.dataMap.get("size");
        float radius = (Float)saved.dataMap.get("conical");
        float dir = (Float)saved.dataMap.get("direction");
        float intensity = (Float)saved.dataMap.get("intensity");
        float r = (Float)saved.dataMap.get("r");
        float g = (Float)saved.dataMap.get("g");
        float b = (Float)saved.dataMap.get("b");

        LightSource light = new LightSource();//new Vector2f(x,y), size, radius, dir, intensity, new Color(r, g, b));
        light.setPosition(x, y);
        light.size = size;
        light.setDirection(dir);
        light.setConicalRadius(radius);
        light.intensity = intensity;
        light.color = new Color(r,b,g,1);
        return (SceneObject)light;
    }
    
    

}
