package com.silvergobletgames.sylver.graphics;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.graphics.ConcreteParticleEmitters.SparkEmitter;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import com.silvergobletgames.sylver.netcode.SceneObjectSaveData;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import com.silvergobletgames.sylver.util.Log;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2;

public abstract class ImageParticleEmitter extends AbstractParticleEmitter 
{

    //Texture to draw particles with
    protected Image image; 
    protected float particleRotation;


    //========================
    // Constructors
    //========================

    /**
     * Constructor for a particle emitter that emits Images
     * @param image Image to emmit
     */
    public ImageParticleEmitter(Image image)
    {
        //super constructor
    	super();
        
        //check for valid parameter
        if(image == null)        
            throw new InvalidParameterException("Image cant be null");
        

        //assign member varialbes
    	this.image = image;
    }




    //===================
    // Methods
    //===================

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

                if (this.isRelativePositioning())
                {
                    x += this.getPosition().x - particle.originalEmitterPosition.x;
                    y += this.getPosition().y - particle.originalEmitterPosition.y;
                }
                
                //Rotate
                gl2.glMatrixMode(GL2.GL_MODELVIEW);
                gl2.glPushMatrix();
                gl2.glTranslatef( x + drawWidth/2, y+drawWidth/2, 0.0f);
                gl2.glRotatef(this.particleRotation, 0.0f, 0.0f, 1.0f);
                gl2.glTranslatef(-(x + drawWidth/2), -(y+drawWidth/2), 0.0f);
                
                //bind the draw color
                drawColor.bind(gl2);
                
                TextureCoords coords = texture.getImageTexCoords();
                float textureBottom = coords.bottom();
                float textureTop = coords.top();
                float textureLeft = coords.left();
                float textureRight = coords.right();               

                //draw the image
                gl2.glBegin(GL2.GL_QUADS);
                {
                    //bottom left
                    gl2.glTexCoord2d(textureLeft, textureBottom);
                    gl2.glVertex2f(x - drawWidth/2, y - drawHeight/2); 

                    //bottom right
                    gl2.glTexCoord2d(textureRight, textureBottom);
                    gl2.glVertex2f(x + drawWidth/2, y - drawHeight/2);  

                    //top right
                    gl2.glTexCoord2d(textureRight, textureTop);
                    gl2.glVertex2f(x + drawWidth/2, y + drawHeight/2);  

                    //top left    
                    gl2.glTexCoord2d(textureLeft, textureTop);
                    gl2.glVertex2f(x - drawWidth/2, y + drawHeight/2);          
                }
                gl2.glEnd();    
                
                //pop matrix from rotation transform
                gl2.glPopMatrix();
            }


            
            
            
            //reset bound color to white
            Color.white.bind(gl2);

            //disable texture and blend modes
            gl2.glDisable(GL2.GL_TEXTURE_2D);
            gl2.glDisable(GL2.GL_BLEND);    
        }
    }
    
    public AbstractParticleEmitter copyEmitter()
    {
        //instantiate the factory
        ImageParticleEmitter returnEmitter = null;
        try
        {
            returnEmitter = this.getClass().newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Log.error( "ParticleEmitter Instantiation Exception: " + e.getMessage(),e);
     
        }
        
        returnEmitter.setAngle(this.getAngle());
        returnEmitter.setDuration(this.getDuration());
        returnEmitter.setImage(this.image.copy());
        returnEmitter.setRelativePositioning(this.isRelativePositioning()); 
        returnEmitter.setParticlesPerFrame(this.getParticlesPerFrame());
        returnEmitter.setParticleRotation(this.getParticleRotation());
        if(this.isStopped())
        {
            returnEmitter.stopEmittingThenRemove();
        }
        
        return returnEmitter;
    }
    
    /**
     * gets the angle that particles will be rotated by.
     * @return 
     */
    public float getParticleRotation()
    {
        return this.particleRotation;
    }
    
    /**
     * sets the angle that particles will be rotated by.
     * @param angle 
     */
    public void setParticleRotation(float angle)
    {
        this.particleRotation = angle;
    }
    
    
    
     //====================
    // RenderData Methods
    //==================== 
    
    public SceneObjectRenderData dumpRenderData()
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.IMAGEPARTICLEEMITTER,this.ID);

        renderData.data.add(0, this.getPosition().x);
        renderData.data.add(1, this.getPosition().y);
        renderData.data.add(2,this.isStopped());
        renderData.data.add(3,this.getClass());
        renderData.data.add(4,this.getDuration());
        renderData.data.add(5,this.getParticlesPerFrame());
        renderData.data.add(6,this.image.getTextureReference());
        renderData.data.add(7,this.getAngle());
        renderData.data.add(8,this.isRelativePositioning());
        renderData.data.add(9,this.getParticleRotation());
        
        return renderData;
    }
    
    public static ImageParticleEmitter buildFromRenderData(SceneObjectRenderData renderData)
    {
         //instantiate the emitter
        ImageParticleEmitter emitter = null;
        try
        {   
            Class klass = (Class)renderData.data.get(3);
            emitter = (ImageParticleEmitter)klass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Log.error( "ParticleEmitter Instantiation Exception: " + e.getMessage(),e);
         
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
        emitter.setRelativePositioning((boolean)renderData.data.get(8)); 
        emitter.setParticleRotation((float)renderData.data.get(9)); 
        
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
        
        if(changeData.get(8) != null)
        {
            this.setRelativePositioning((boolean)changeData.get(8));
        }
        
        if(changeData.get(9) != null)
        {
            this.setParticleRotation((float)changeData.get(9));
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
        saved.dataMap.put("image", this.image.getTextureReference());
        saved.dataMap.put("x", this.getPosition().x);
        saved.dataMap.put("y", this.getPosition().y);
        saved.dataMap.put("duration", this.getDuration());
        saved.dataMap.put("particles", this.getParticlesPerFrame());
        saved.dataMap.put("angle",this.getAngle());
        saved.dataMap.put("rotation",this.getParticleRotation());
        
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
            Log.error( "Emitter Class Not Found: " + (String)saved.dataMap.get("factoryClass"),e);
          
            
            emitterClass = SparkEmitter.class;
        }
        //get other saved data about the emitter
        String ref = (String)saved.dataMap.get("image");
        float x = (Float)saved.dataMap.get("x");
        float y = (Float)saved.dataMap.get("y");
        int ttl = (Integer)saved.dataMap.get("duration");
        float ppf = (Float)saved.dataMap.get("particles");
        float rotation = (Float)saved.dataMap.get("rotation");
        
        float angle = 90;
        if(saved.dataMap.containsKey("angle")) //TODO get rid of
           angle= (float)saved.dataMap.get("angle");

        //instantiate the factory
        ImageParticleEmitter emitter = null;
        try
        {
            emitter = (ImageParticleEmitter)emitterClass.newInstance();
        } 
        catch(InstantiationException| IllegalAccessException e)
        {
            //log error to console
            Log.error( "ParticleEmitter Instantiation Exception: " + e.getMessage(),e);
        
        }

        emitter.setPosition(x, y);
        emitter.setDuration(ttl);
        emitter.setParticlesPerFrame(ppf);
        emitter.setImage(new Image(ref));
        emitter.setAngle(angle);
        emitter.setParticleRotation(rotation);
        
        return (SceneObject)emitter;
    }






    //====================
    // Accessor Methods
    //====================

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









}