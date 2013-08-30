package com.silvergobletgames.sylver.graphics;

import com.jogamp.opengl.util.texture.Texture;
import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.graphics.ConcreteParticleEmitters.SparkEmitter;
import com.silvergobletgames.sylver.netcode.NetworkedSceneObject;
import com.silvergobletgames.sylver.netcode.SavableSceneObject;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import com.silvergobletgames.sylver.netcode.SceneObjectSaveData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO redo how this class works
public abstract class AbstractParticleEmitter extends NetworkedSceneObject implements SavableSceneObject
{
    //position of the emitter
    protected SylverVector2f position;
    //angle of the emitter in degrees
    protected float angle;  
    //total duration
    private int duration; //in frames 60frames/second
    //Current life left
    private long remainingDuration; //in frames 60frames/second
    //Particles per frame
    private float particlesPerFrame;
    private float accumulator;
    //Is the emitter done?
    private boolean finished = false;  
    //boolean saying if the emitter has stopped emitting new particles
    private boolean stoppedEmitting = false;
    //use relative paritcle positioning
    private boolean useRelativeParticles = false;
    
    //My list of particles
    private LinkedHashSet<Particle> particles = new LinkedHashSet();
    private static int INDIVIDUAL_MAX_PARTICLES = 1000;
   

    
    //================
    // Constructors
    //================
       
    public ParticleEmitter()
    {
        this.addToGroup(CoreGroups.EMITTER);
        
        //defaults
        this.position = new SylverVector2f(0, 0);
        this.duration = 60;
        this.remainingDuration = 60;
        this.particlesPerFrame = 1f;
        this.angle = 90;
        
    }
  
    
    //=====================
    // Scene Object Methods
    //=====================
        
    /**
     * Updates this emitter, releasing and disposing particles.
     */
    public void update()
    {
        //add new particles
        if (remainingDuration != 0  && !stoppedEmitting)
        {
            //Increment the accumulator and set up the loop cap
            accumulator += this.particlesPerFrame * Game.getInstance().getConfiguration().getEngineSettings().particleDensity.value;
            float numToAdd = accumulator;
            
            for (int i = 0; i < numToAdd; i++)
            {
                if ( particles.size() < INDIVIDUAL_MAX_PARTICLES)
                {
                    Particle newParticle = buildParticle();
                    newParticle.originalEmitterPosition = new SylverVector2f(this.getPosition());
                    particles.add(newParticle);
                    //Add to current particles, and decrement the accumulator
                    accumulator--;
                }
            }
            this.remainingDuration--;
        }
        else
        {
            if (particles.isEmpty())
            {
                finished = true;
                if (owningScene != null)
                    owningScene.remove(this);
            }
        }
        
        //update particles
        LinkedHashSet<Particle> updateList = new LinkedHashSet(particles);  
        for(Particle p: updateList)
        {
            if (p.TTL <= 0)
                particles.remove(p);         
            else
                p.update();
        }

    }
    
    /**
     * Draws each particle
     * @param gl2 
     */
    public void draw(GL2 gl2)
    {
        
        //=====================
        // Draw with a texture
        //=====================
        if(this.image != null)
        {
            //enable texturing and blending
            gl2.glEnable(GL2.GL_TEXTURE_2D);
            gl2.glEnable(GL2.GL_BLEND);
            gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);

            //clamp the texture
            Texture texture = image.getTexture();
            texture.bind(gl2);

            //render all the particles
            LinkedHashSet<Particle> updateList = new LinkedHashSet(particles);
            for(Particle particle: updateList)
            {                

                Color drawColor = particle.color;
                float drawWidth = image.getTexture().getWidth() * particle.scale;
                float drawHeight = image.getTexture().getHeight() * particle.scale;
                float x = particle.position.x;
                float y = particle.position.y;

                if (this.useRelativeParticles)
                {
                    x += this.position.x - particle.originalEmitterPosition.x;
                    y += this.position.y - particle.originalEmitterPosition.y;
                }
                
                //bind the draw color
                drawColor.bind(gl2);

                //draw the image
                gl2.glBegin(GL2.GL_QUADS);
                {
                    //bottom left
                    gl2.glTexCoord2d(0, 1);
                    gl2.glVertex2f(x - drawWidth/2, y - drawHeight/2); 

                    //bottom right
                    gl2.glTexCoord2d(1, 1);
                    gl2.glVertex2f(x + drawWidth/2, y - drawHeight/2);  

                    //top right
                    gl2.glTexCoord2d(1, 0);
                    gl2.glVertex2f(x + drawWidth/2, y + drawHeight/2);  

                    //top left    
                    gl2.glTexCoord2d(0, 0);
                    gl2.glVertex2f(x - drawWidth/2, y + drawHeight/2);          
                }
                gl2.glEnd();    
            }


            //reset bound color to white
            Color.white.bind(gl2);

            //disable texture and blend modes
            gl2.glDisable(GL2.GL_TEXTURE_2D);
            gl2.glDisable(GL2.GL_BLEND);    
        }
        
        //=====================
        // Draw with GLPoints
        //=====================
        else
        {
            //render all the particles
            gl2.glDisable(GL2.GL_TEXTURE_2D);
            LinkedHashSet<Particle> updateList = new LinkedHashSet(particles);
            for(Particle p: updateList)
            { 
                Color drawColor = p.color;
                float x = p.position.x;
                float y = p.position.y;
                gl2.glPointSize(4);
                
                //bind the draw color
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
            
    }
    
    /**
     * 
     * @return The position of the emitter
     */
    public SylverVector2f getPosition()
    {
        return position;
    }
    
    /**
     * Sets the position of the emitter
     * @param x x position
     * @param y y position
     */
    public void setPosition(float x , float y)
    {
        this.position.set(x, y);
    }
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    
    //=====================
    // Class Methods
    //=====================
         
   /**
    * This is the main abstract method that must be implemented as per the abstract factory pattern.
    * This method is called when the particle emitter is emitting a particle.
    * @return 
    */
    protected abstract Particle buildParticle();
    
    /**
     * 
     * @return How many particles are emiter per frame
     */
    public float getParticlesPerFrame()
    {
        return this.particlesPerFrame;
    }
    
    /**
     * Sets how many particles are emitted per frame
     * @param ppf how many particles per frame are emitted
     */
    public void setParticlesPerFrame(float ppf)
    {
        this.particlesPerFrame = ppf;
    }
    
    /**
     * 
     * @return The total duration of the emitter
     */
    public float getDuration()
    {
        return this.duration;
    }
    
    /**
     * Set the duration of the emitter
     * @param duration duration in frames for the emitter to live
     */
    public void setDuration(int duration)
    {
        this.duration = duration;  
        this.remainingDuration = duration;
    }
    
    /**
     * Gets the remaining duration of this particle emitter
     * @return Remaining duration
     */
    public float getRemainingDuration()
    {
        return this.remainingDuration;
    }
    
    /**
     * Get the angle of this emitter in degrees
     * @return 
     */
    public float getAngle()
    {
        return this.angle;
    }
    
    /**
     * Set the angle of this emitter in degrees
     * @param angle New angle in degrees
     */
    public void setAngle(float angle)
    {
        this.angle = angle;
    }
    
    /**
     * Get the image that this emitter uses for its particles
     * @return 
     */
    public Image getImage()
    {
        return this.image;
    }
    
    /**
     * Sets the image that this emitter uses for its particles
     * @param image 
     */
    public void setImage(Image image)
    {
        this.image = image;
    }
     
    /**
     * Returns if this emitter is finished or not
     * @return 
     */
    public boolean isFinished()
    {
        return finished;
    }
    
    /**
     * Flags the emitter to stop emitting particles then remove itself from its owning scene once it is done
     */
    public void stopEmittingThenRemove()
    {
        this.remainingDuration = 0;
        this.stoppedEmitting = true;
    }
    
    /**
     * A mode for particles that ensure that they will always keep the same position relative to the emitter, if the emitter
     * moves while particles are being emitted.
     * @param value 
     */
    public void useRelativePositioning(boolean value)
    {
        this.useRelativeParticles = value;
    }
    
    public ParticleEmitter copy()
    {
        //instantiate the factory
        ParticleEmitter returnEmitter = null;
        try
        {
            returnEmitter = this.getClass().newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(ParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "ParticleEmitter Instantiation Exception: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
        }
        
        returnEmitter.setAngle(this.angle);
        returnEmitter.setDuration(this.duration);
        returnEmitter.setImage(this.image.copy());
        returnEmitter.setParticlesPerFrame(this.particlesPerFrame);
        returnEmitter.stoppedEmitting = this.stoppedEmitting;
        
        return returnEmitter;
    }
    
    //====================
    // RenderData Methods
    //==================== 
    
    public SceneObjectRenderData dumpRenderData()
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.PARTICLEEMITTER,this.ID);

        renderData.data.add(0, this.getPosition().x);
        renderData.data.add(1, this.getPosition().y);
        renderData.data.add(2,stoppedEmitting);
        renderData.data.add(3,this.getClass());
        renderData.data.add(4,this.duration);
        renderData.data.add(5,this.particlesPerFrame);
        renderData.data.add(6,(this.image != null)?this.image.getTextureReference():false);
        renderData.data.add(7,this.angle);
        
        return renderData;
    }
    
    public static ParticleEmitter buildFromRenderData(SceneObjectRenderData renderData)
    {
         //instantiate the emitter
        ParticleEmitter emitter = null;
        try
        {   
            Class klass = (Class)renderData.data.get(3);
            emitter = (ParticleEmitter)klass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(ParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "ParticleEmitter Instantiation Exception: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
        }
       
        emitter.setPosition((float) renderData.data.get(0), (float)renderData.data.get(1));
        emitter.setDuration((int)renderData.data.get(4));
        emitter.setParticlesPerFrame((float)renderData.data.get(5));
        if(renderData.data.get(6) instanceof String)
        {
           emitter.setImage(new Image((String)renderData.data.get(6)));
        }
        emitter.setID(renderData.getID());
        emitter.setAngle((float)renderData.data.get(7));
        
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
        for(int i = 0; i <8; i ++)
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
        SceneObjectSaveData saved = new SceneObjectSaveData(CoreClasses.PARTICLEEMITTER,this.ID);
        
        saved.dataMap.put("emitterClass", this.getClass().getName());
        saved.dataMap.put("image", this.image.getTextureReference());
        saved.dataMap.put("x", this.getPosition().x);
        saved.dataMap.put("y", this.getPosition().y);
        saved.dataMap.put("duration", this.duration);
        saved.dataMap.put("particles", this.particlesPerFrame);
        saved.dataMap.put("angle",this.angle);
        
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
            Logger logger =Logger.getLogger(ParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "Emitter Class Not Found: " + (String)saved.dataMap.get("factoryClass"));
            logger.addHandler(new ConsoleHandler()); 
            
            emitterClass = SparkEmitter.class;
        }
        //get other saved data about the emitter
        String ref = (String)saved.dataMap.get("image");
        float x = (Float)saved.dataMap.get("x");
        float y = (Float)saved.dataMap.get("y");
        int ttl = (Integer)saved.dataMap.get("duration");
        float ppf = (Float)saved.dataMap.get("particles");
        
        float angle = 90;
        if(saved.dataMap.containsKey("angle")) //TODO get rid of
           angle= (float)saved.dataMap.get("angle");

        //instantiate the factory
        ParticleEmitter emitter = null;
        try
        {
            emitter = (ParticleEmitter)emitterClass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(ParticleEmitter.class.getName());
            logger.log(Level.SEVERE, "ParticleEmitter Instantiation Exception: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
        }

        emitter.setPosition(x, y);
        emitter.setDuration(ttl);
        emitter.setParticlesPerFrame(ppf);
        emitter.setImage(new Image(ref));
        emitter.setAngle(angle);
        
        return (SceneObject)emitter;
    }
    
    
    //========================
    // Private Particle Class
    //========================
    
    public static class Particle 
    {    
        //position and movement vectors
        protected SylverVector2f position;
        protected SylverVector2f velocity;
        protected SylverVector2f acceleration;

        //scale and growth
        protected float scale;
        protected float growth;
        
        //fade
        protected int fadeStart;
        protected float fade;

        //color
        protected Color color;   

        //time until dissipation
        protected int TTL;

        protected SylverVector2f originalEmitterPosition;

        /**
         * A particle emitted by the particle emitter
         * @param pos Initial position of the particle relative to the world
         * @param velocity Initial velocity of the particle
         * @param acceleration Initial acceleration of the particle
         * @param color color of the particle
         * @param scale scale of the particle
         * @param growth growth of the particle
         * @param ttl time until dissipation in game ticks
         */
        public Particle(SylverVector2f pos, SylverVector2f velocity, SylverVector2f acceleration,
                        Color color, float scale, float growth, int ttl)
        {

            this.position = pos;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.growth = growth;
            this.color = color;
            this.scale = scale;
            TTL = ttl;
            this.fadeStart = TTL/4;
            this.fade = color.a/((float)TTL/4);
            
        }

        /**
        * Updates the state of this particle.
        */
        public void update()
        {
            TTL--;
            velocity.add(acceleration);
            position.add(velocity);
            scale += growth;
            
            if(TTL < this.fadeStart)
               color.a = Math.max(color.a-fade, 0);

        }

    }
    
    
}


