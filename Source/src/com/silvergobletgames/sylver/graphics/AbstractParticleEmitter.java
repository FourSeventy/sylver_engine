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
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO redo how this class works
public abstract class AbstractParticleEmitter extends NetworkedSceneObject implements SavableSceneObject
{
    //angle of the emitter in degrees
    private float angle;  
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
    protected LinkedHashSet<Particle> particles = new LinkedHashSet();
    private static int INDIVIDUAL_MAX_PARTICLES = 1000; //todo make configurable
   

    
    //================
    // Constructors
    //================
       
    public AbstractParticleEmitter()
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
     * Updates this emitter, calling the #buildParticle() method and handling updates of each particle
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
        Iterator it = particles.iterator();
        for(Particle p; it.hasNext(); )
        {
            p = (Particle)it.next();
            
            if (p.TTL <= 0)
                it.remove();         
            else
                p.update();
        }

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
    public int getDuration()
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
     * Returns if this emitter is finished or not
     * @return 
     */
    public boolean isFinished()
    {
        return finished;
    }
    
    public boolean isStopped()
    {
        return this.stoppedEmitting;
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

   /**
    *
    *@return if this emitter uses relative positioning or not.
    */
    public boolean isRelative()
    {
    	return this.useRelativeParticles;
    }
    
    public abstract AbstractParticleEmitter copyEmitter();
  
    
    //====================
    // RenderData Methods
    //==================== 
    
    public abstract SceneObjectRenderData dumpRenderData();
    
    //public abstract static ParticleEmitter buildFromRenderData(SceneObjectRenderData renderData);
       
    public abstract SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData);
         
    public abstract void reconcileRenderDataChanges(long lastTime, long futureTime, SceneObjectRenderDataChanges renderDataChanges);
      
    public abstract void interpolate(long currentTime);
   
    
    //====================
    // Saving Methods
    //====================    
    
    public abstract SceneObjectSaveData dumpFullData();
       
    //public abstract static SceneObject buildFromFullData(SceneObjectSaveData saved);
   
    
    
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


