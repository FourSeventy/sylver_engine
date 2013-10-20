package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.audio.AudioRenderer;
import com.silvergobletgames.sylver.audio.Sound;
import com.silvergobletgames.sylver.graphics.RenderingPipelineGL2;
import com.silvergobletgames.sylver.graphics.RenderingPipelineGL3;
import com.silvergobletgames.sylver.graphics.Viewport;
import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 * This class is the basic building block of a game. All your Images, Sounds, Lights, and Effects
 * live in a Scene. A Scene has three main methods: update(), render() and handleInput(). The Game
 * will call all of these methods for you once the scene is added to the Game class.
 * @author Mike
 */
public abstract class Scene 
{
    //scene object manager
    private SceneObjectManager sceneObjectManager = new SceneObjectManager();
    
    //effects manager
    private SceneEffectsManager sceneEffectsManager = new SceneEffectsManager();
    
    //scene viewport
    private Viewport viewport = new Viewport();   
    
    //parallax layerse enum
    public static enum Layer
    {
        BACKGROUND(0,1,0),
        PARALLAX5(.501f,5,5), //.501 to fix rounding bug
        PARALLAX4(.6f,4,4),
        PARALLAX3(.7f,3,3),
        PARALLAX2(.8f,2,2),
        PARALLAX1(.9f,1,1),
        ATTACHED_BG(1,0,6),
        MAIN(1,0,7),
        ATTACHED_FG(1,0,8),
        FOREGROUND1(1.1f,0,12),
        FOREGROUND2(1.2f,0,13),
        WORLD_HUD(1,0,9),
        HUD(0,0,10),
        MENU(0,0,11);
        
        
        public int saveInt;
        public float coordinateScalingFactor;
        public float blurFactor;
        
        Layer(float scalingFactor, float blur, int save)
        {
            coordinateScalingFactor = scalingFactor;
            blurFactor = blur;
            saveInt = save;
        }
        
        public static Layer fromIntToLayer(int number)
        {
            switch(number)
            {
                case 0: return BACKGROUND;
                case 1: return PARALLAX1;
                case 2: return PARALLAX2;
                case 3: return PARALLAX3;
                case 4: return PARALLAX4;
                case 5: return PARALLAX5;
                case 6: return ATTACHED_BG;
                case 7: return MAIN;
                case 8: return ATTACHED_FG;
                case 9: return WORLD_HUD;
                case 10: return HUD;
                case 11: return MENU;
                case 12: return FOREGROUND1;
                case 13: return FOREGROUND2;
                default: return null;
                    
            }
        }
        
        public static float getLayerConversionFactor(Layer fromLayer, Layer toLayer)
        {
            return toLayer.coordinateScalingFactor / fromLayer.coordinateScalingFactor;
        }
        
        
        
        
    }
    
    
    //====================
    // Scene Core Methods
    //====================
    
    /**
     * A barebones scene update method that updates everything in the scene.
     */
    public void update()
    {
        //update scene effects manager
        this.sceneEffectsManager.update();
        
        //update everything in the scene     
        ArrayList<SceneObject> parallaxLayerObjects;
        for (Layer layer: Layer.values())
        {
            parallaxLayerObjects = new ArrayList(this.getSceneObjectManager().get(layer));
            for (SceneObject sceneObject:  parallaxLayerObjects)
            {
                //update the SceneObject
                sceneObject.update();
            }
        }
    }

    /**
     * Renders everything in the scene using either the GL2 or GL3 renderer, based on the GlCapabilities
     * @param gl 
     */
    public void render(GL2 gl)
    {
        //set viewport size
        getViewport().setDimensions(Game.getInstance().getGraphicsWindow().getCurrentAspectRatio().x, Game.getInstance().getGraphicsWindow().getCurrentAspectRatio().y);
                  
        if(gl.isGL3bc())
        {
            //=================
            // GL3bc rendering
            //=================         
            RenderingPipelineGL3.render((GL3bc)gl, getViewport(), getSceneObjectManager(), getSceneEffectsManager());
        }      
        else
        {
            //===============
            // GL2 rendering
            //===============
            RenderingPipelineGL2.render(gl, getViewport(), getSceneObjectManager(), getSceneEffectsManager()); 
        }
          
    }
      

    /**
     * Handles the appropriate input for the scene. 
     */
    public abstract void handleInput();
  
    
    //===========================
    // Scene Basic Functionality
    //===========================
    
    /**
     * Returns the scene object manager for the scene
     * @return The scene object manager
     */
    public final SceneObjectManager getSceneObjectManager()
    {
        return this.sceneObjectManager;
    }
    
    /**
     * Returns the viewport of the scene
     * @return The viewport
     */
    public final Viewport getViewport()
    {
        return this.viewport;
    }
    
    /**
     * Allows you to specify your own viewport for the scene.
     * @param viewport Viewport for this scene
     */
    public final void setViewport(Viewport viewport)
    {
        this.viewport = viewport;
    }
    
    /**
     * Returns the scene effects manager of the scene
     * @return the SceneEffectsManager
     */
    public final SceneEffectsManager getSceneEffectsManager()
    {
        return this.sceneEffectsManager;     
    }
       
    /**
     * Adds a scene object to the scene. When a scene object gets added to the scene
     * it is assigned an ID if it didnt have one, and is registered with the SceneObjectManager
     * @param item SceneObject to add
     * @param layer Layer to add it to
     */
    public void add(SceneObject item, Layer layer)
    {
        //set the scene objects owning scene
        item.setOwningScene(this);
        
        //give the sceneObject an ID if it doesnt already have one
        if(item.getID() == null || item.getID().equals(""))
           item.setID(this.sceneObjectManager.generateUniqueID()); 

        //add the item to the scene object manager
        this.sceneObjectManager.add(item,layer);
        
        //notify the SceneObject that it was added
        item.addedToScene(); 
    }   
    
    /**
     * Add a sound to the scene.
     * @param sound Sound to add
     */
    public void add(Sound sound)
    {
        AudioRenderer.playSound(sound);
    }
      
    /**
     * Removes a scene obejct from the scene
     * @param item SceneObject to remove
     */
    public void remove(SceneObject item)
    {
        //remove the item from the scene object manager
        this.sceneObjectManager.remove(item);
        
        //notify the SceneObject that it was removed
        item.removedFromScene();
        
        //set objects owning scene to null
        item.setOwningScene(null);
    }
             
    /**
     * This method gets called when a scene is switched into. Can be used for initialization
     * @param args 
     */
    public void sceneEntered(ArrayList args)
    {
        
    }
    
    /**
     * This method gets called when a scene is switched out of. Can be used for cleanup
     */
    public void sceneExited()
    {
        
    }
    
}
