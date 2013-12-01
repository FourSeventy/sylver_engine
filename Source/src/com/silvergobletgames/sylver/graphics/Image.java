package com.silvergobletgames.sylver.graphics;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.core.SceneObjectManager;
import com.silvergobletgames.sylver.graphics.AnimationPack.CoreAnimations;
import com.silvergobletgames.sylver.graphics.AnimationPack.DefaultAnimationPack;
import com.silvergobletgames.sylver.graphics.AnimationPack.ImageAnimation;
import com.silvergobletgames.sylver.graphics.ImageEffect.ImageEffectType;
import com.silvergobletgames.sylver.netcode.*;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import com.silvergobletgames.sylver.util.SerializableEntry;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import javax.swing.event.EventListenerList;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.Map.Entry;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Image extends NetworkedSceneObject implements SavableSceneObject, Anchorable
{
  
    //the OpenGL texture reference 
    private Texture texture;
    //name of the resource path to the current texture 
    private String textureRef = null;
    
    //the animation pack used for animation
    private AnimationPack animationPack;
    // The enum label given to the current animation 
    private ImageAnimation currentAnimation;
    // The number of updates that we have been showing the current texture. 
    private int frameCounter = 0;
    // The index of the current texture in the active animation
    private int index = 0;  
    
    //shader program
    private ShaderProgram shaderProgram;
    //vertex and frag string refs
    private String vertexRef = null;
    private String fragRef = null;
    //time
    private float time;      
    
 
    //width of the image 
    private float width;
    //height of the image 
    private float height;
    //anchor of the image
    private Anchorable.Anchor anchor = Anchor.BOTTOMLEFT;
    //rotation point
    private SylverVector2f rotationPoint = new SylverVector2f(.5f,.5f);   
    //angle to rotate the image
    private float angle = 0;
    //brightness of the image
    private float brightness = 1.0f;
    //alpha brightness
    private float alphaBrightness = 1.0f;
    //scale of the image
    private float scale = 1;
    //color for the image
    private Color color = new Color(Color.white);
    //true if the image should be flipped horizontally 
    private boolean flippedHorizontal = false;
    //true if the image should be flipped vertically 
    private boolean flippedVertical = false;
    //the map of ImageEffects
    private HashMap<String,ImageEffect> imageEffects = new HashMap<>();
    //the map of overlays
    protected HashMap<String, Overlay> imageOverlays = new HashMap<>();
    //the list of things that are listening to this Image 
    private EventListenerList listenerList = new EventListenerList();
    //will this image cull alpha
    private boolean alphaCulling = true;
    
    //position and coord buffers
    private static ByteBuffer texCoordBuffer = Buffers.newDirectByteBuffer(8); 
    private static FloatBuffer positionBuffer = Buffers.newDirectFloatBuffer(8);
         

    //===================
    // Constructors
    //===================

    /**
     * Creates a static image using the texture that can be referenced in the TextureLoader by ref
     * @param ref The reference string to the texture
     */
    public Image(String ref) 
    {
        this.currentAnimation = CoreAnimations.NONE;
        this.texture = Game.getInstance().getAssetManager().getTextureLoader().getTexture(ref);
        this.textureRef = ref;
       
        //sets width and height
        width = texture.getImageWidth();
        height = texture.getImageHeight();        
        
        
        this.addToGroup(CoreGroups.IMAGE);
    }
    

    /**
     * Creates an animated image using the given animation pack
     * @param animationPack Animation pack to create the animated image with
     */
    public Image(AnimationPack animationPack) 
    {
        //set up the texture map
        this.animationPack = animationPack;
        setAnimation(CoreAnimations.IDLE);
        
        //sets up the default texture
        this.texture = (Texture)this.animationPack.animationSet.get(this.currentAnimation).get(0);
        width = texture.getImageWidth();
        height = texture.getImageHeight();
        
        this.addToGroup(CoreGroups.IMAGE);
    }  
    
    /**
     * Creates an image that will draw the given shader. Will pass to the shader a "u_time" uniform
     * for shaders that want to change over time, as well as a "u_origin" uniform, and a "u_dimensions" uniform.
     * The given strings must match a shader program in the ShaderLoader.
     * @param vertexShader vertex shader name
     * @param fragShader frag shader name
     */
    public Image(String vertexShader, String fragShader)
    {
        //local variables
        this.vertexRef = vertexShader;
        this.fragRef = fragShader;
        this.shaderProgram = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram(vertexShader, fragShader);
        
        //sets some default size
        width =100;
        height =100;
        
        this.addToGroup(CoreGroups.IMAGE);
    }
    
  
    //=====================
    // SceneObject Methods
    //=====================

    /**
     * Updates the images render effects, overlays, and handles animation.
     */
    public void update()
    {                            
        //update ImageEffects and remove them if expired   
        for (Iterator<Entry<String,ImageEffect>> it = this.imageEffects.entrySet().iterator(); it.hasNext();) 
        {
            //get image effect from iterator
            Entry<String,ImageEffect> entry = it.next();
            ImageEffect effect = entry.getValue();
            
            //update the effect
            effect.update();  
            
            //if expired, remove it
            if(effect.isExpired())
            {   
                //remove
                it.remove();
                
                //fire effects on remove
                effect.onRemove(); 
                                
            }
        }
        
        //remove expired overlays       
        for(Iterator<Entry<String,Overlay>> it = this.imageOverlays.entrySet().iterator(); it.hasNext();)
        {
            Entry<String,Overlay> entry = it.next();
            Overlay overlay = entry.getValue();
            overlay.elapsedDuration++;
            
             if(overlay.elapsedDuration >= overlay.getDuration() && !overlay.isInfiniteDuration())
                it.remove();
        }

        //================================
        //set position and update overlays
        //================================
             
        for(Overlay overlay: this.imageOverlays.values())
            overlay.getImage().update();
        
        for(Overlay overlay: this.imageOverlays.values())  
             overlay.getImage().setPositionAnchored(this.position.x + overlay.getRelativePosition().x * this.width * this.scale, this.position.y + overlay.getRelativePosition().y * this.height * this.scale);
        
        
        //==================
        // process animation
        //==================
        if(this.isAnimated())
        {
            //if its time for a new texture
            if (frameCounter >= this.animationPack.getFPT(currentAnimation)) 
            {
                //reset frame counter
                frameCounter = 0;

                //increment index
                index++;
                index = index % animationPack.animationSet.get(this.currentAnimation).size();

                //Change textures
                this.texture = animationPack.animationSet.get(this.currentAnimation).get(index);
            } 
            else //Increment the frame counter.       
                frameCounter++;


            //when we finish animating an animation fire an event
            if (index == 0 && frameCounter == 0) 
                this.fireFinishedAnimatingEvent();
        }
        
    }
       
    /**
     * Draws the image to the given graphics context
     * @param gl2 Graphics context
     */
    public void draw(GL2 gl2)
    {       
      
        //set blending functions
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA); 
        gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        
        //set up filtering
//        this.texture.setTexParameteri(gl2, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);//GL2.GL_LINEAR_MIPMAP_LINEAR
//        this.texture.setTexParameteri(gl2, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        

        //=============================================================
        // Handles brightness, flip, scale, translation, and rotation
        //=============================================================

        //local vairables
        float drawWidth = this.width;
        float drawHeight = this.height;
        float x = this.position.x;
        float y = this.position.y;
        
        //draw color
        Color drawColor = new Color(color);
        
        //brightness
        drawColor.scale(brightness); 
        drawColor.a = drawColor.a * this.alphaBrightness;

        //scale
        drawWidth *= scale;
        drawHeight *= scale;

        //calculate rotation point
        float rotationPointX = x + this.rotationPoint.x * drawWidth;
        float rotationPointY = y + this.rotationPoint.y * drawHeight;

        //Rotate
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glPushMatrix();
        gl2.glTranslatef(rotationPointX, rotationPointY, 0.0f);
        gl2.glRotatef(angle, 0.0f, 0.0f, 1.0f);
        gl2.glTranslatef(-rotationPointX, -rotationPointY, 0.0f);
        
        
        //============================
        // Drawing for Textured Image
        //============================      
        if(this.shaderProgram == null)
        {
            //texture flip
            TextureCoords coords = texture.getImageTexCoords();
            float textureBottom = coords.bottom();
            float textureTop = coords.top();
            float textureLeft = coords.left();
            float textureRight = coords.right();

            if (flippedHorizontal) 
            {
                textureRight = coords.left();
                textureLeft = coords.right();
            }
            if (flippedVertical) 
            {
                textureTop = coords.bottom();
                textureBottom = coords.top();
            }
            //============================
            // Sends vertex data to shader
            //============================
            
            //creates vertex data array for position
            positionBuffer.clear();
            positionBuffer.put(new float[]{x,y}); //x, y,
            positionBuffer.put(new float[]{x + drawWidth,y});
            positionBuffer.put(new float[]{x +drawWidth,y+drawHeight});
            positionBuffer.put(new float[]{x,y + drawHeight});
            positionBuffer.rewind();

            //creates vertex data array for texCoords
            texCoordBuffer.clear();
            texCoordBuffer.put(new byte[]{(byte)textureLeft,(byte)textureBottom});
            texCoordBuffer.put(new byte[]{(byte)textureRight,(byte)textureBottom});
            texCoordBuffer.put(new byte[]{(byte)textureRight,(byte)textureTop});
            texCoordBuffer.put(new byte[]{(byte)textureLeft,(byte)textureTop});
            texCoordBuffer.rewind();

            //get the image shader
            ShaderProgram imageShader = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("imageVert.glsl", "imageFrag.glsl");

            //starts running the shader program
            gl2.glUseProgram(imageShader.program());     

            //enables the use of vertex array data
            gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);

            //gets the location of the position and texCoord attributes
            int texCoordAttributeLocation = gl2.glGetAttribLocation(imageShader.program(), "a_texCoord");
            int positionAttributeLocation = gl2.glGetAttribLocation(imageShader.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl2.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl2.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl2.glVertexAttribPointer(texCoordAttributeLocation, 2, GL2.GL_BYTE, false, 0, texCoordBuffer);
            gl2.glVertexAttribPointer(positionAttributeLocation, 2, GL2.GL_FLOAT, false, 0, positionBuffer);            

            //sets color uniform
            gl2.glUniform4f(gl2.glGetUniformLocation(imageShader.program(), "color"),drawColor.r,drawColor.g,drawColor.b,drawColor.a);

            //sets alpha culling uniform
            if(this.alphaCulling == false)
                gl2.glUniform1f(gl2.glGetUniformLocation(imageShader.program(), "u_cullAlpha"),0);

            //binds our texture to texture unit 0           
            gl2.glActiveTexture(GL2.GL_TEXTURE0);
            this.texture.bind(gl2);
            

            //sets the texture sampler
            gl2.glUniform1i(gl2.glGetUniformLocation(imageShader.program(), "s_texture"), 0);

            //draws the primitive
            gl2.glDrawArrays(GL2.GL_QUADS, 0, 4);

            //stops the shader program
            gl2.glUseProgram(0);
            gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);     

        }
        //============================
        // Drawing for Shader Image
        //============================ 
        else 
        {
            
            //starts running the shader program
            gl2.glUseProgram(this.shaderProgram.program());
            
            //sets time uniform
            gl2.glUniform1f(gl2.glGetUniformLocation(this.shaderProgram.program(), "u_time"),this.time/100);
            this.time++;
            
            //sets the origin uniform
            int origin = gl2.glGetUniformLocation(this.shaderProgram.program(), "u_origin"); //gets handle to glsl variable
            gl2.glUniform2f(origin,x, y);
            
            //sets the dimensions uniform
            int dimensions = gl2.glGetUniformLocation(this.shaderProgram.program(), "u_dimensions"); //gets handle to glsl variable
            gl2.glUniform2f(dimensions,drawWidth, drawHeight);


            //draw the primitive
            gl2.glBegin(GL2.GL_QUADS);
            {
                gl2.glVertex2f(x,y);
                gl2.glVertex2f(x + drawWidth,y);
                gl2.glVertex2f(x +drawWidth,y+drawHeight);
                gl2.glVertex2f(x,y + drawHeight);
            }
            gl2.glEnd();
 
            
            //stops the shader program
            gl2.glUseProgram(0);
            
            
        }
        //=========
        // Cleanup
        //=========
        
        //pop matrix from rotation transform
        gl2.glPopMatrix();

        //disable texture and blend modes
        gl2.glDisable(GL2.GL_BLEND);     
        
         //draw image overlays
        for (String overlayKey : this.imageOverlays.keySet()) 
            this.imageOverlays.get(overlayKey).getImage().draw(gl2);
        
    }
    
    /**
     * Sets the position
     * @param x x position
     * @param y y position
     */
    public void setPosition(float x, float y) 
    {
        this.position.set(x,y);
        
        //set overlay positions
        Set<String> overlayKeys = this.imageOverlays.keySet();
        Iterator<String> iter = overlayKeys.iterator();
        while (iter.hasNext())
        {
            String key = iter.next();
            Overlay overlay = this.imageOverlays.get(key);

            overlay.getImage().setPositionAnchored(this.position.x + overlay.getRelativePosition().x * this.width * this.scale, this.position.y + overlay.getRelativePosition().y * this.height * this.scale);         
        }
    }
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    
    
    //===================
    // Class Methods
    //==================
    
    public void setPositionAnchored(float x, float y)
    {        
        SylverVector2f pos = Anchorable.Anchor.getNewPosition(this, x, y);
        setPosition(pos.x, pos.y);
    }
    
    
    /**
     * Adds a render effect to this image without a key. Its key will be set to the effects hashCode
     * @param effect The effect to add
     */
    public boolean addImageEffect(ImageEffect effect)
    {
        return this.addImageEffect(Integer.toString(effect.hashCode()),effect);
    }
    
    /**
     * Adds a render effect to this image with an explicit key.
     * @param name The key that this effect can be referenced with
     * @param effect The effect to add
     */
    public boolean addImageEffect(String name, ImageEffect effect)
    {
        //if we dont have a render efect of the same type already add it
        if(this.hasImageEffectType(effect.renderEffectType) == false)
        {
            //add the effect
            this.imageEffects.put(name,effect);

            //set the render effects owning image
            effect.setOwningImage(this);

            //tell the effect that it was applied
            effect.onApply();
            return true;          
        }
        else 
            return false;
    }
    
    /**
     * Removes the given render effect from the image
     * @param effect The effect to remove
     */
    public void removeImageEffect(ImageEffect effect)
    {
        //remove the effect
        for(String key:this.imageEffects.keySet())
        {
           ImageEffect keyEffect = this.imageEffects.get(key);
           
           if(keyEffect == effect)
           {
               this.removeImageEffect(key);
               return;
           }
               
        }
    }
    
    /**
     * Removes the render effect associated to the given key from the image
     * @param key Key associated ot the render effect we want to remove
     */
    public void removeImageEffect(String key)
    {
        //remove the effect
        ImageEffect effect = this.imageEffects.remove(key);
        
        //tell the effect that it was removed
        if(effect != null)
            effect.onRemove();   
      
    }
    
    /** 
     * Removes all render effects on this image. 
     */
    public void removeAllImageEffects() 
    {
        //call onRemove() for all the effects
        for (ImageEffect effect: imageEffects.values())         
            effect.onRemove();
        
        //clear the list
        imageEffects.clear();
        
    }
    
    public boolean hasImageEffect(String name)
    {
        return this.imageEffects.containsKey(name);
    }
    
    public boolean hasImageEffectType(ImageEffectType type)
    {
        for(ImageEffect effect: this.imageEffects.values())
        {
            switch(type)
            {
                case WIDTH: if(effect.renderEffectType.equals(ImageEffectType.WIDTH)){return true;} break;
                case HEIGHT: if(effect.renderEffectType.equals(ImageEffectType.HEIGHT)){return true;} break;
                case XTRANSLATE: if(effect.renderEffectType.equals(ImageEffectType.XTRANSLATE)){return true;} break;
                case YTRANSLATE: if(effect.renderEffectType.equals(ImageEffectType.YTRANSLATE)){return true;} break;
                case ROTATION: if(effect.renderEffectType.equals(ImageEffectType.ROTATION)){return true;} break;
                case SCALE: if(effect.renderEffectType.equals(ImageEffectType.SCALE)){return true;} break;
                case COLOR: if(effect.renderEffectType.equals(ImageEffectType.COLOR)){return true;} break;
                case BRIGHTNESS: if(effect.renderEffectType.equals(ImageEffectType.BRIGHTNESS)){return true;} break;
            }
        }
        return false;
    }
    
    
    /**
     * Adds an image overlay to this image with an explicit key.
     * @param name The key that this overlay can be referenced with
     * @param overlay the overlay to add
     */
    public void addOverlay(String name, Overlay overlay)
    {
        //give the overlay the appropriate relative size
        if(overlay.useRelativeSize)
        {
            float overlayScale = (float)Math.sqrt((overlay.getRelativeSize() * Math.pow(this.scale, 2) *this.getWidth() * this.getHeight())/(overlay.getImage().getWidth() * overlay.getImage().getHeight())); 
            overlay.getImage().setScale(overlayScale);
        }
        
        //position overlay
        overlay.getImage().setPositionAnchored(this.position.x + overlay.getRelativePosition().x * this.width, this.position.y + overlay.getRelativePosition().y * this.height);
         
        this.imageOverlays.put(name, overlay);
    }
    
    /**
     * Adds an overlay to this image without a key. Its key will be set to the overlays hashCode
     * @param overlay  the overlay to add
     */
    public void addOverlay(Overlay overlay)
    {
        addOverlay(Integer.toString(overlay.hashCode()), overlay); 
    }
    
    /**
     * Removes the overlay with the given key from the image
     * @param name 
     */
    public void removeOverlay(String name)
    {
        this.imageOverlays.remove(name);
    }
    
    /**
     * Removes the given overlay from the image
     * @param overlay 
     */
    public void removeOverlay(Overlay overlay)
    {
         //remove the effect
        for(String key:this.imageOverlays.keySet())
        {
           Overlay keyOverlay = this.imageOverlays.get(key);
           
           if(keyOverlay == overlay)
           {
               this.removeOverlay(key);
               return;
           }
               
        }
    }
    
    /**
     * Removes all overlays from the image
     */
    public void removeAllOverlays()
    {
        this.imageOverlays.clear();
    }
    
    public boolean hasOverlay(String name)
    {
        return this.imageOverlays.containsKey(name);
    }
    
    public Overlay getOverlay(String name)
    {
        return this.imageOverlays.get(name);
    }
    
    /**
     * Fires finished animating events to any listeners
     */
    private void fireFinishedAnimatingEvent() 
    {
        
        Object[] listeners = listenerList.getListenerList();
        ImageAnimation e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) 
        {
            if (listeners[i] == AnimationListener.class) 
            {
                if (e == null) 
                {
                    e = currentAnimation;
                }
                ((AnimationListener) listeners[i + 1]).finishedAnimating(e);
            }
        }
    }

    

    //=================
    // Accessor Methods
    //=================
  
    public int getAnimationIndex()
    {
        return this.index;
    }
    
    public void setAnimationIndex(int index)
    {
        this.index = index;
    }
    
    /**
     * Returns the texture reference of this image, or null if it doesnt have one
     * @return 
     */
    public String getTextureReference()
    {
        return this.textureRef;
    }
    
    /**
     * Sets the texture reference of this image
     * @param ref 
     */
    public void setTextureReference(String ref)
    {
        this.currentAnimation = CoreAnimations.NONE;
        this.texture = Game.getInstance().getAssetManager().getTextureLoader().getTexture(ref);
        this.textureRef = ref;
       
        //sets width and height
        width = texture.getImageWidth();
        height = texture.getImageHeight();
    }
    
     /**
     * Get the OpenGL texture holding this image
     * 
     * @return The OpenGL texture holding this image
     */
    public Texture getTexture() 
    {
        return texture;
    }
    
    /**
     * If this image is a shader image, returns the vertex ref, else it returns null
     * @return 
     */
    public String getVertexReference()
    {
        return this.vertexRef;
    }
    
    /**
     * If this is a shader image, returns the frag ref, else it returns null
     * @return 
     */
    public String getFragReference()
    {
        return this.fragRef;
    }
    
    public AnimationPack getAnimationPack()
    {
        return this.animationPack;
    }

    /**
     * Retrieves the current animation of this image
     * @return 
     */
    public ImageAnimation getAnimation()
    {
        return currentAnimation;
    }
    
    /**
     * Sets the current animation. If the new animation doesnt exist in this images AnimationPack,
     * the animation will default to IDLE. If the new animation is the same as the old animation, it will not reset
     * current animation index.
     * @param newAnimation The new animation to set
     */
    public void setAnimation(ImageAnimation newAnimation)
    {
        if (this.isAnimated())
        {
            if (currentAnimation != newAnimation && this.animationPack.animationSet.containsKey(newAnimation))
            {
                currentAnimation = newAnimation;
                this.texture =this.animationPack.animationSet.get(this.currentAnimation).get(0);
                this.index = 0;
                this.frameCounter = 0;
            }
            else if( !this.animationPack.animationSet.containsKey(newAnimation))
            {
                this.setAnimation(CoreAnimations.IDLE);
            }
        }
    }

    /**
     * Returns true if this image is using an animation pack rather than a static texture
     * @return 
     */
    public boolean isAnimated() 
    {
        if (this.animationPack == null) 
            return false;
        else
            return true;
    }
  
    /**
     * Returns the brightness of the image. The color of the image is multiplied by the brightness
     * @return Brightness of the image
     */
    public float getBrightness()
    {
        return this.brightness;
    }
    
    /**
     * Sets the brightness of the image. The color of the image is multiplied by the brightness
     * @param b brightness of the image
     */
    public void setBrightness(float b)
    {
        this.brightness = b;
    }
    
    /**
     * Brightness of the image that affects the alpha of the color
     * @return 
     */
    public float getAlphaBrightness()
    {
        return this.alphaBrightness;
    }
    
    /**
     * Brightness value that affects the alpha value of the color
     * @param b 
     */
    public void setAlphaBrightness(float b)
    {
        this.alphaBrightness = b;
    }
    
    /**
     * Returns the scale of this image
     * @return 
     */
    public float getScale() 
    {
        return this.scale;
    }
    
    /**
     * Sets the scale
     * @param s 
     */
    public void setScale(float s)
    {
        this.scale = s;
    }

    /**
     * Gets horizontal flip
     */
    public boolean isFlippedHorizontal() 
    {
        return flippedHorizontal;
    }
    
    /**
     * Sets horizontal flip
     * @param f 
     */
    public void setHorizontalFlip(boolean f) 
    {
        this.flippedHorizontal = f;
    }

    /**
     * Gets vertical flip
     */
    public boolean isFlippedVertical() 
    {
        return flippedVertical;
    }
    
    /**
     * Sets vertical flip
     * @param f 
     */
    public void setVerticalFlip(boolean f) 
    {
        this.flippedVertical = f;
    }

    /**
     * Returns current color of the image. 
     * @return 
     */
    public Color getColor() 
    {
          return color;
    }

    /**
     * Sets the color 
     * @param c 
     */
    public void setColor(Color c) 
    {
        if (c != null) 
            color = c;      
        else 
            color = new Color(Color.white);    
    } 

    /**
     * Set the Image's anchor point.
     */    
    @Override
    public void setAnchor(Anchorable.Anchor type)
    {
        if (type != null)
        {
            this.anchor = type;
        }    
    }
    
    /** 
     * Get the anchor.
     */
    @Override
    public Anchor getAnchor()
    {
        return anchor;
    }
     
    /**
     * Sets the position
     */
    public void setDimensions(float x, float y) 
    {
        width = x;
        height = y;
    }
    
    public SylverVector2f getDimensions()
    {
        return new SylverVector2f(this.width , this.height );
    }

    /**
     * Get the width of this image
     * 
     * @return The width of this image
     */
    public float getWidth()
    {
        return width;
    }

    /**
     * Get the height of this image
     * 
     * @return The height of this image
     */
    public float getHeight() 
    {
        return height;
    }

    /**
     * Get a deep copy of this image.
     * 
     * @return The copy of this image
     */
    public Image copy()
    { 
        //construct the copy
        Image imageCopy;       
        if(textureRef != null)       
            imageCopy = new Image(this.textureRef);   
        else if(this.animationPack != null)
            imageCopy = new Image(this.animationPack);
        else
            imageCopy = new Image(this.vertexRef,this.fragRef);

        
        //copy fields
        imageCopy.setPosition(this.position.x, this.position.y);
        imageCopy.width = width;
        imageCopy.height = height;
        imageCopy.textureRef = textureRef;
        imageCopy.brightness = brightness;
        imageCopy.scale = scale;
        imageCopy.angle = angle;       
        imageCopy.currentAnimation = this.currentAnimation;
        imageCopy.flippedHorizontal = this.flippedHorizontal;
        imageCopy.flippedVertical = this.flippedVertical;
        
        //adds a copy of the renderEffects to the new image
        for (String key: this.imageEffects.keySet())       
            imageCopy.addImageEffect(key, this.imageEffects.get(key).copy());
        
        //adds a copy of the overlays to the new image
        for(String overlayKey: this.imageOverlays.keySet())
            imageCopy.addOverlay(overlayKey, this.imageOverlays.get(overlayKey).copy());
        
        //set the anchor of the new copy
        imageCopy.anchor = this.anchor;
        
        //return the copied image
        return imageCopy;
    }
  
    /**
     * Returns the angle of theimage in degrees.
     * @return Angle in degrees
     */
    public float getAngle() 
    {
        return this.angle;
    }
    
    /**
     * Sets the angle in degrees
     * @param a Angle in degrees
     */
    public void setAngle(float a) 
    {
        this.angle = a;
    }
    
    public SylverVector2f getRotationPoint()
    {
        return this.rotationPoint;
    }
    
    public void setRotationPoint(float x, float y)
    {
        this.rotationPoint.set(x, y);
    }
    
    public void setAlphaCulling(boolean value)
    {
        this.alphaCulling = value;
    }
    
    public boolean isAlphaCulling()
    {
        return this.alphaCulling;
    }

    public void addAnimationListener(AnimationListener l) 
    {
        listenerList.add(AnimationListener.class, l);
    }

    public void removeAnimationListener(AnimationListener l) 
    {
        listenerList.remove(AnimationListener.class, l);
    }

    public AnimationListener[] getAnimationListeners() 
    {
        return listenerList.getListeners(AnimationListener.class);
    }
     
    
    //===================
    //Render Data Methods
    //===================
    
     public SceneObjectRenderData dumpRenderData() 
     {
         SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.IMAGE,this.ID);
    
         renderData.data.add(0,textureRef);
         renderData.data.add(1,width);
         renderData.data.add(2,height);
         renderData.data.add(3,this.getPosition().x);
         renderData.data.add(4,this.getPosition().y);
         renderData.data.add(5,angle);
         renderData.data.add(6,scale);
         renderData.data.add(7,this.animationPack!= null?this.animationPack.getClass():null);
         renderData.data.add(8,color);
         renderData.data.add(9,this.isFlippedHorizontal());
         renderData.data.add(10,this.isFlippedVertical());
         renderData.data.add(11,this.vertexRef);
         renderData.data.add(12,this.fragRef);
         renderData.data.add(13,this.alphaBrightness);
         renderData.data.add(14,this.currentAnimation);
         renderData.data.add(15,this.brightness);
         renderData.data.add(16,this.anchor);
         renderData.data.add(17,this.rotationPoint.x);
         renderData.data.add(18,this.rotationPoint.y);
         
         //render data for all the overlays
         ArrayList<SerializableEntry> overlayData = new ArrayList();
         for (String key: this.imageOverlays.keySet()){
             overlayData.add(new SerializableEntry(key, imageOverlays.get(key).dumpRenderData()));
         }
         renderData.data.add(19,overlayData);
         
         //render data for all the effects
         ArrayList<SerializableEntry> renderEffectData = new ArrayList();
         for(String key: this.imageEffects.keySet()){
             renderEffectData.add(new SerializableEntry(key,this.imageEffects.get(key).dumpRenderData()));
         }
         renderData.data.add(20,renderEffectData);
                
         return renderData;
        
     }
     
     public static Image buildFromRenderData(SceneObjectRenderData renderData)
     {
               

        String ref = (String)renderData.data.get(0);
        Class klass = (Class)renderData.data.get(7);
        String vertexRef = (String)renderData.data.get(11);
        String fragRef = (String)renderData.data.get(12);
        Image image;
        
        //creates the image as either animated or static
        if(ref != null)
            image = new Image(ref);
        else if(klass != null)
        {
            AnimationPack animationPack = null;
            try
            {   
                animationPack = (AnimationPack)klass.newInstance();
                
            } 
            catch(InstantiationException| IllegalAccessException e)
            {
                //log error to console
                Logger logger =Logger.getLogger(Image.class.getName());
                logger.log(Level.SEVERE, "AnimationPack Instantiation Exception: " + e.getMessage(),e);
    
                
                //use default animation pack
                animationPack = new DefaultAnimationPack();
            }
 
            image = new Image(animationPack);          
        }
        else //we have a shader image
        {
            image = new Image(vertexRef,fragRef);
        }
        
        image.setID(renderData.getID());        
        image.setDimensions((float)renderData.data.get(1), (float)renderData.data.get(2));  
        image.setPosition((float)renderData.data.get(3), (float)renderData.data.get(4));
        image.setAngle((float)renderData.data.get(5));
        image.setScale((float)renderData.data.get(6));   
        image.setColor((Color)renderData.data.get(8));
        image.setHorizontalFlip((boolean)renderData.data.get(9));
        image.setVerticalFlip((boolean)renderData.data.get(10));
        image.setAlphaBrightness((Float)renderData.data.get(13)); 
        image.setAnimation((ImageAnimation)renderData.data.get(14));
        image.setBrightness((float)renderData.data.get((15)));
        image.setAnchor((Anchor)renderData.data.get(16));
        image.setRotationPoint((float)renderData.data.get(17), (float)renderData.data.get(18));
        
        //Overlays
        ArrayList<SerializableEntry> overlayData = (ArrayList)renderData.data.get(19);
        for (SerializableEntry<String, RenderData> overlayEntry : overlayData)
        {
            if (overlayEntry.getValue() != null)           
                image.addOverlay(overlayEntry.getKey(), Overlay.buildFromRenderData(overlayEntry.getValue()));        
        }
        
        //renderEffects
        ArrayList<SerializableEntry> renderEffectData = (ArrayList)renderData.data.get(20);
        for (SerializableEntry<String, RenderData> renderEffectEntry : renderEffectData)
        {
            //build either a RenderEffect, or a MultiRenderEffect
            ImageEffect effect;
            if(renderEffectEntry.getValue().data.get(0).equals("1"))            
                effect = ImageEffect.buildFromRenderData(renderEffectEntry.getValue());                     
            else
                effect = MultiImageEffect.buildFromRenderData(renderEffectEntry.getValue());
            
            image.addImageEffect(renderEffectEntry.getKey(), effect);
        }
            

         
        return image;    
    }
        
     public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData)
     {
        SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
        int changeMap = 0;
        changes.ID = this.ID;
        ArrayList changeList = new ArrayList();
        
        //Non overlay fields
        forloop: for(int i = 0; i <=18; i++)
        {
            //check for effects on fields and dont generate changes for those fields
            switch(i)
            {
                case 1: if(this.hasImageEffectType(ImageEffectType.WIDTH)){ continue forloop;} break; //width 
                case 2: if(this.hasImageEffectType(ImageEffectType.HEIGHT)){ continue forloop;} break; //height
                case 3: if(this.hasImageEffectType(ImageEffectType.XTRANSLATE)){ continue forloop;} break; //x
                case 4: if(this.hasImageEffectType(ImageEffectType.YTRANSLATE)){ continue forloop;} break; //y
                case 5: if(this.hasImageEffectType(ImageEffectType.ROTATION)){ continue forloop;} break; //angle
                case 6: if(this.hasImageEffectType(ImageEffectType.SCALE)){ continue forloop;} break; //scale
                case 8: if(this.hasImageEffectType(ImageEffectType.COLOR)){ continue forloop;} break; //color
                case 15: if(this.hasImageEffectType(ImageEffectType.BRIGHTNESS)){ continue forloop;} break; //brightness
            }
                   
            if((oldData.data.get(i) != null && newData.data.get(i) != null) && (!oldData.data.get(i).equals( newData.data.get(i))))
            {                 
                changeList.add(newData.data.get(i));
                changeMap += 1L << i;
            }
        }
        
        //====================================
        //generate changes for image overlays 
        //====================================
        ArrayList<SerializableEntry> imageChanges = new ArrayList();
        ArrayList<SerializableEntry> imageAdds = new ArrayList();
        ArrayList<String> imageRemoves = new ArrayList();
        
        ArrayList<SerializableEntry> oldOverlayData = (ArrayList)oldData.data.get(19);
        ArrayList<SerializableEntry> newOverlayData = (ArrayList)newData.data.get(19);
        
        HashMap<String, SceneObjectRenderData> oldMap = new HashMap();
        for (SerializableEntry<String, SceneObjectRenderData> entry : oldOverlayData)
            oldMap.put(entry.getKey(), entry.getValue());
        
        HashMap<String, SceneObjectRenderData> newMap = new HashMap();
        for (SerializableEntry<String, SceneObjectRenderData> entry : newOverlayData)
            newMap.put(entry.getKey(), entry.getValue());

        //check for additions and changes
        for (String newKey: newMap.keySet())
        {
            
            //If old map contains the new key, we must check for a change in data, and send it if there is a change
            if (oldMap.containsKey(newKey))
            {
                Overlay dummyOverlay = this.imageOverlays.get(newKey);
                SceneObjectRenderDataChanges overlayChange = dummyOverlay.generateRenderDataChanges(oldMap.get(newKey), newMap.get(newKey));
                if (overlayChange != null)
                    imageChanges.add(new SerializableEntry(newKey, overlayChange));
            }
            //if it doesnt, then we have an addition
            else
            {
                imageAdds.add(new SerializableEntry(newKey, newMap.get(newKey)));
            }
        } 
        
        //check for subtractions
        for(String oldKey: oldMap.keySet())
        {
            if(!newMap.containsKey(oldKey))
                imageRemoves.add(oldKey);
        }


        if(!imageChanges.isEmpty() && false)
        {
            changeList.add(imageChanges);
            changeMap += 1L << 19;
        }
        if(!imageAdds.isEmpty())
        {
            changeList.add(imageAdds);
            changeMap += 1L << 20;
        }
        if(!imageRemoves.isEmpty())
        {
            changeList.add(imageRemoves);
            changeMap += 1L << 21;
        }
        
        
        //====================================
        //generate changes for Render Effects 
        //====================================
        
        ArrayList<SerializableEntry<String,SceneObjectRenderData>> renderAdds = new ArrayList();
        ArrayList<String> renderRemoves = new ArrayList();
        ArrayList<SerializableEntry> oldRenderData = (ArrayList)oldData.data.get(20);
        ArrayList<SerializableEntry> newRenderData = (ArrayList)newData.data.get(20);
        
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
                renderAdds.add(new SerializableEntry(newKey,(RenderData)newRenderMap.get(newKey)));
        }
        
        //check for subtractions 
        for(String oldKey: oldRenderMap.keySet())
        {
            if(!newRenderMap.containsKey(oldKey))
                renderRemoves.add(oldKey);
        }
        
        if(!renderAdds.isEmpty())
        {
            changeList.add(renderAdds);
            changeMap += 1L << 22;
        }
        if(!renderRemoves.isEmpty())
        {
            changeList.add(renderRemoves);
            changeMap += 1L << 23;
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
        for(byte i = 0; i <=23; i ++)
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
        if(changeData.get(3) != null)
        {
            float startPosition=  interpolators.containsKey("xPosition")? (float)interpolators.get("xPosition").getB() : this.getPosition().x;          
            LinearInterpolator lerp1= new LinearInterpolator(startPosition,(float)changeData.get(3),lastTime,futureTime);
            this.interpolators.put("xPosition",lerp1);
        }
        else
            this.interpolators.remove("xPosition");

        //y position interpolator
        if(changeData.get(4) != null)
        {
            float startPosition=  interpolators.containsKey("yPosition")? (float)interpolators.get("yPosition").getB() : this.getPosition().y;
            LinearInterpolator lerp2= new LinearInterpolator(startPosition,(float)changeData.get(4),lastTime,futureTime);
            this.interpolators.put("yPosition",lerp2);
        }
        else
            this.interpolators.remove("yPosition");

        //angle interpolator
        if(changeData.get(5) != null)
        {
            float startPosition=  interpolators.containsKey("angle")? (float)interpolators.get("angle").getB() : this.getAngle();
            LinearInterpolator lerp3= new LinearInterpolator(startPosition,(float)changeData.get(5),lastTime,futureTime);
            this.interpolators.put("angle", lerp3);
        }
        else
            this.interpolators.remove("angle");
         

        //reconcile data
        this.setDimensions(changeData.get(1) != null?(float)changeData.get(1):this.width, changeData.get(2) != null?(float)changeData.get(2):this.height);
        if(changeData.get(6) != null)
            this.setScale((float)changeData.get(6));  
        if(changeData.get(8) != null)
            this.setColor((Color)changeData.get(8));
        if(changeData.get(9) != null)          
            this.setHorizontalFlip((boolean)changeData.get(9));          
        if(changeData.get(10) != null)
            this.setVerticalFlip((boolean)changeData.get(10));
        if(changeData.get(13) != null)
            this.setAlphaBrightness((float)changeData.get(13));
        if(changeData.get(14) != null)
            this.setAnimation((ImageAnimation)changeData.get(14));
        if(changeData.get(15) != null)
            this.setBrightness((float)changeData.get(15));
        if(changeData.get(16) != null)
            this.setAnchor((Anchor)changeData.get(16));
        
        float rotationPointX = this.rotationPoint.x;
        float rotationPointY = this.rotationPoint.y;
        
        if(changeData.get(17) != null)
            rotationPointX = (float)changeData.get(17);
        if(changeData.get(18) != null)
            rotationPointY = (float)changeData.get(18);
        
        this.setRotationPoint(rotationPointX, rotationPointY);
            

        //Overlay Changes
        if (changeData.get(19) != null)
        {
            ArrayList<SerializableEntry<String, SceneObjectRenderDataChanges>> overlayChanges = (ArrayList)changeData.get(19);
            for (SerializableEntry<String, SceneObjectRenderDataChanges> entry : overlayChanges)
            {
                if (this.imageOverlays.containsKey(entry.getKey()))
                    this.imageOverlays.get(entry.getKey()).reconcileRenderDataChanges(0, 1,entry.getValue());
            }
        }
        //New overlays
        if (changeData.get(20) != null)
        {
            ArrayList<SerializableEntry<String, RenderData>> overlayAdds = (ArrayList)changeData.get(20);
            for (SerializableEntry<String, RenderData> entry : overlayAdds)
            {
                this.imageOverlays.put(entry.getKey(), Overlay.buildFromRenderData(entry.getValue()));
            }
        }
        //Removed overlays
        if (changeData.get(21) != null)
        {
            ArrayList<String> overlayRemoves = (ArrayList)changeData.get(21);
            for (String entry : overlayRemoves)
            {
                this.imageOverlays.remove(entry);
            }
        }
        
        //added renderEffects
        if(changeData.get(22) != null)
        {
            ArrayList<SerializableEntry<String, RenderData>> renderAdds = (ArrayList)changeData.get(22);
            for (SerializableEntry<String, RenderData> entry : renderAdds)
            {
                ImageEffect effect;
                String key = entry.getKey();
                
                if(entry.getValue().data.get(0).equals("1"))            
                    effect = ImageEffect.buildFromRenderData(entry.getValue());                     
                else
                    effect = MultiImageEffect.buildFromRenderData(entry.getValue());
                
                this.addImageEffect(key, effect);
                              
            }           
        }
        
        //removed renderEffects
        if(changeData.get(23) != null)
        {
            ArrayList<String> removeList = (ArrayList<String>)changeData.get(23);
            
            //remove only if the effect is repeating
            for(String s: removeList)
            {
                if(this.imageEffects.get(s) != null &&this.imageEffects.get(s).repeat)
                    this.removeImageEffect(s);
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

            if(this.interpolators.containsKey("xPosition"))             
                x = (float)this.interpolators.get("xPosition").interp(currentTime);
            
            if(this.interpolators.containsKey("yPosition"))
               y = (float)this.interpolators.get("yPosition").interp(currentTime);   

            this.setPosition(x, y);

            //interpolate angle
            if(this.interpolators.containsKey("angle"))
            {
                float newangle = (float)this.interpolators.get("angle").interp(currentTime);  
                this.setAngle(newangle);
            }
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
        
        SceneObjectSaveData saved = new SceneObjectSaveData(CoreClasses.IMAGE,this.ID);
        
        saved.dataMap.put("ref",textureRef);
        saved.dataMap.put("pack",this.animationPack != null?this.animationPack.getClass().getName():null);
        saved.dataMap.put("width",width);
        saved.dataMap.put("height",height);
        saved.dataMap.put("x",this.getPosition().x);
        saved.dataMap.put("y",this.getPosition().y);
        saved.dataMap.put("angle",angle);
        saved.dataMap.put("scaleX",scale);
        saved.dataMap.put("horizonal",this.isFlippedHorizontal());
        saved.dataMap.put("vertical",this.isFlippedVertical());
        saved.dataMap.put("r",color.r);
        saved.dataMap.put("g",color.g);
        saved.dataMap.put("b",color.b);
        saved.dataMap.put("a",color.a);
        saved.dataMap.put("rotX",this.rotationPoint.x);
        saved.dataMap.put("rotY",this.rotationPoint.y);
        saved.dataMap.put("vertexRef", this.vertexRef);
        saved.dataMap.put("fragRef",this.fragRef);
        return saved;
    }
      
    public static Image buildFromFullData(SceneObjectSaveData saveData) 
    {
        //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
        
        String refStr = (String) saveData.dataMap.get("ref");
        String animationPackClassName = (String)saveData.dataMap.get("pack");
        String vertexRef = (String)saveData.dataMap.get("vertexRef");
        String fragRef = (String)saveData.dataMap.get("fragRef");
        float w = (float) saveData.dataMap.get("width");
        float h = (float) saveData.dataMap.get("height");
        float x = (float) saveData.dataMap.get("x");
        float y = (float) saveData.dataMap.get("y");
        float theta = (float) saveData.dataMap.get("angle");
        float scale = (Float) saveData.dataMap.get("scaleX");
        float r = (float)saveData.dataMap.get("r");
        float g = (float)saveData.dataMap.get("g");
        float b = (float)saveData.dataMap.get("b");
        float a = (float)saveData.dataMap.get("a");
        Color col = new Color(r,g,b,a);

        boolean hFlip = (boolean) saveData.dataMap.get("horizonal");
        boolean vFlip = (boolean) saveData.dataMap.get("vertical");
        
        Float rotationX = (Float)saveData.dataMap.get("rotX");
        Float rotationY = (Float)saveData.dataMap.get("rotY");

        Image image = null;
        if(refStr != null)
             image = new Image(refStr);
        else if(animationPackClassName != null)
        {
            AnimationPack animationPack = null;
            try
            {   
                Class klass = Class.forName(animationPackClassName);
                animationPack = (AnimationPack)klass.newInstance();
                
            } 
            catch(InstantiationException| ClassNotFoundException | IllegalAccessException e)
            {
                //log error to console
                Logger logger =Logger.getLogger(Image.class.getName());
                logger.log(Level.SEVERE, "AnimationPack Instantiation Exception: " + e.getMessage(),e);
        
                
                animationPack = new DefaultAnimationPack();
            }
 
            image = new Image(animationPack); 
        }
        else //shader image
        {
            image = new Image(vertexRef,fragRef);
        }
        
        image.setPosition(x, y);
        image.setDimensions(w, h);
        image.setAngle(theta);
        image.setScale(scale);
        image.setColor(col);
        image.setHorizontalFlip(hFlip);
        image.setVerticalFlip(vFlip);
        
        if(rotationX != null && rotationY != null)
            image.setRotationPoint(rotationX, rotationY);
        
        image.setID((String)saveData.dataMap.get("id"));


        return image;
    }

    
}
