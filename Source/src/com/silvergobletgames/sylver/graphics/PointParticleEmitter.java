package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.graphics.ConcreteParticleEmitters.SparkEmitter;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import com.silvergobletgames.sylver.netcode.SceneObjectSaveData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;

public abstract class PointParticleEmitter extends AbstractParticleEmitter 
{

    //particle color
    protected Color particleColor; 

    //particle size
    protected int particleSize;


    //================
    // Constructor
    //================

    /**
     * Constructor for a particle emitter that emmits GLPoints.
     * @param color Base color for points
     * @param size  Base Size for points
     */
    public PointParticleEmitter(Color color, int size)
    {
        //super constructor
    	super();
        
        //check for valid parameters
        if(color == null)        
            throw new InvalidParameterException("Color parameter can't be null");
        
        if(size < 1)
            throw new InvalidParameterException("Size parameter must be greater than 0");
        

        //assign member variables
    	this.particleColor = color;
    	this.particleSize = size;
    }




    //================
    // Methods
    //================

    /**
    * This is the main abstract method that must be implemented as per the abstract factory pattern.
    * This method is called when the particle emitter is emitting a particle.
    * @return 
    */
    protected abstract Particle buildParticle();
    
    /**
     * Draws each particle
     * @param gl2 
     */
    public void draw(GL2 gl2)
    {
    	//=====================
        // Draw with GLPoints
        //=====================
        
        //make sure texturing is disabled
        gl2.glDisable(GL2.GL_TEXTURE_2D);

        //render all the particles
        for(Particle p: particles)
        { 
            float x = p.position.x;
            float y = p.position.y;

            if (this.isRelativePositioning())
            {
                x += this.getPosition().x - p.originalEmitterPosition.x;
                y += this.getPosition().y - p.originalEmitterPosition.y;
            }
           
            gl2.glPointSize(this.particleSize);
            
            //bind the draw color
            Color drawColor = p.color;  
            drawColor.bind(gl2);
            
            gl2.glBegin(GL2.GL_POINTS);
            {
                gl2.glVertex2f(x,y);
            }
            gl2.glEnd();
            
            //reset bound color to white
            Color.white.bind(gl2);
        }
        

    }
    
    public AbstractParticleEmitter copyEmitter()
    {
        //instantiate the factory
        PointParticleEmitter returnEmitter = null;
        try
        {
            returnEmitter = this.getClass().newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(AbstractParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "ParticleEmitter Instantiation Exception: " + e.getMessage(),e);
          
        }
        
        returnEmitter.setAngle(this.getAngle());
        returnEmitter.setDuration(this.getDuration());
        returnEmitter.setColor(this.getColor());
        returnEmitter.setSize(this.getSize());
        returnEmitter.setParticlesPerFrame(this.getParticlesPerFrame());
        returnEmitter.setRelativePositioning(this.isRelativePositioning()); 
        if(this.isStopped())
            returnEmitter.stopEmittingThenRemove();
        
        return returnEmitter;
    }
    
    /**
     * Sets the base particle color
     * @param color 
     */
    public void setColor(Color color)
    {
        //check for valid parameters
        if(color == null)        
            throw new InvalidParameterException("Color parameter can't be null");
        
        this.particleColor = color;
    }
    
    /**
     * Gets the base particle color
     * @return 
     */
    public Color getColor()
    {
        return this.particleColor;
    }
    
    /**
     * Sets base particle size
     * @param size size to set
     */
    public void setSize(int size)
    {
        if(size < 1)
            throw new InvalidParameterException("Size parameter must be greater than 0");
        
        this.particleSize = size;
    }
    
    /**
     * Gets base particle size
     * @return base particle size
     */
    public int getSize()
    {
        return this.particleSize;
    }
    
    
    //====================
    // RenderData Methods
    //==================== 
    
    public SceneObjectRenderData dumpRenderData()
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.POINTPARTICLEEMITTER,this.ID);

        renderData.data.add(0, this.getPosition().x);
        renderData.data.add(1, this.getPosition().y);
        renderData.data.add(2,this.isStopped());
        renderData.data.add(3,this.getClass());
        renderData.data.add(4,this.getDuration());
        renderData.data.add(5,this.getParticlesPerFrame());
        renderData.data.add(6,this.particleColor);
        renderData.data.add(7,this.getAngle());
        renderData.data.add(8,this.particleSize);
        renderData.data.add(9,this.isRelativePositioning());
        
        return renderData;
    }
    
    public static PointParticleEmitter buildFromRenderData(SceneObjectRenderData renderData)
    {
         //instantiate the emitter
        PointParticleEmitter emitter = null;
        try
        {   
            Class klass = (Class)renderData.data.get(3);
            emitter = (PointParticleEmitter)klass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(PointParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "PointParticleEmitter Instantiation Exception: " + e.getMessage(),e);
         
        }
       
        emitter.setPosition((float) renderData.data.get(0), (float)renderData.data.get(1));
        emitter.setDuration((int)renderData.data.get(4));
        emitter.setParticlesPerFrame((float)renderData.data.get(5));
        emitter.setColor((Color)renderData.data.get(6));
        emitter.setID(renderData.getID());
        emitter.setAngle((float)renderData.data.get(7));
        emitter.setSize((int)renderData.data.get(8));
        emitter.setRelativePositioning((boolean)renderData.data.get(9));  
        
        return emitter;
    }
    
    public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData)
    {
        SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
        int changeMap = 0;
        changes.ID = this.ID;
        ArrayList changeList = new ArrayList();
        
        for(int i = 0; i < oldData.data.size(); i++)
        {
            if(!oldData.data.get(i).equals( newData.data.get(i)))
            {
                changeList.add(newData.data.get(i));
                changeMap += 1 << i;
            }
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
        for(int i = 0; i <10; i ++)
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
              
        //if its a fresh packet, build the interpolators for position
        //clear old interpolators
        this.interpolators.clear();

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
        
            
        if(changeData.get(2) != null && (Boolean)changeData.get(2))
            this.stopEmittingThenRemove();
        
        if(changeData.get(7) != null)
        {
            this.setAngle((float)changeData.get(7));
        }
        
        if(changeData.get(9) != null)
        {
            this.setRelativePositioning((boolean)changeData.get(9));
        }
    }
    
    public void interpolate(long currentTime)
    {
        if(!this.interpolators.isEmpty())
        {
            //interpolate x and y positions
            float x = this.getPosition().x;
            float y = this.getPosition().y;
            if(this.interpolators.containsKey("xPosition"))// && currentTime <= this.interpolators.get("xPosition").getb())
                x = (float)this.interpolators.get("xPosition").interp(currentTime);

            if(this.interpolators.containsKey("yPosition"))// && currentTime <= this.interpolators.get("yPosition").getb())
                y = (float)this.interpolators.get("yPosition").interp(currentTime);

            this.setPosition(x,y);
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
        SceneObjectSaveData saved = new SceneObjectSaveData(CoreClasses.IMAGEPARTICLEEMITTER,this.ID);
        
        saved.dataMap.put("emitterClass", this.getClass().getName());
        saved.dataMap.put("x", this.getPosition().x);
        saved.dataMap.put("y", this.getPosition().y);
        saved.dataMap.put("duration", this.getDuration());
        saved.dataMap.put("particles", this.getParticlesPerFrame());
        saved.dataMap.put("angle",this.getAngle());
        saved.dataMap.put("color",this.getColor());
        saved.dataMap.put("size",this.getSize());
        saved.dataMap.put("relative",this.isRelativePositioning());
        
        return saved;
    }
       
    public static SceneObject buildFromFullData(SceneObjectSaveData saved) 
    {
        //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
        
        //get the factory class
        Class emitterClass = null;
        try
        {
            emitterClass= Class.forName((String)saved.dataMap.get("emitterClass"));
        } 
        catch(ClassNotFoundException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(PointParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "Emitter Class Not Found: " + (String)saved.dataMap.get("factoryClass"));
         
            
            emitterClass = SparkEmitter.class;
        }
        //get other saved data about the emitter
        float x = (Float)saved.dataMap.get("x");
        float y = (Float)saved.dataMap.get("y");
        int ttl = (Integer)saved.dataMap.get("duration");
        float ppf = (Float)saved.dataMap.get("particles");
        Color color = (Color)saved.dataMap.get("color");
        int size = (int)saved.dataMap.get("size");
        
        float angle = 90;
        if(saved.dataMap.containsKey("angle")) //TODO get rid of
           angle= (float)saved.dataMap.get("angle");
        
        boolean relative = (boolean)saved.dataMap.get("relative"); 

        //instantiate the factory
        PointParticleEmitter emitter = null;
        try
        {
            emitter = (PointParticleEmitter)emitterClass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(PointParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "PointParticleEmitter Instantiation Exception: " + e.getMessage(),e);
        
        }

        emitter.setPosition(x, y);
        emitter.setDuration(ttl);
        emitter.setParticlesPerFrame(ppf);
        emitter.setAngle(angle);
        emitter.setColor(color);
        emitter.setSize(size);
        emitter.setRelativePositioning(relative);
        
        return (SceneObject)emitter;
    }





}