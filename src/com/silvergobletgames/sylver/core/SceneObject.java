package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.graphics.*;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.LinkedHashSet;
import javax.media.opengl.GL2;

/**
 * SceneObjects are the main building blocks of the game. This abstract SceneObject class represents the core functionality
 * required of anything that wants to be added to a scene.
 * @author mike
 */
public abstract class SceneObject 
{   
    //the Scene Object's identification string
    protected String ID;
    //the Scene that this object belongs too
    protected Scene owningScene;
    //the groups that the SceneObject belongs to
    private LinkedHashSet<Enum> myGroups = new LinkedHashSet();   
    
    
    //an open ended label interface for the extensible enum pattern
    public static interface SceneObjectClassMask
    {
        public Class getRepresentativeClass();
        
        public String name();
     
        
    }
    
    //masks for the core SceneObjects located in the sylver engine
    public static enum CoreClasses implements SceneObjectClassMask
    {
        IMAGE(Image.class),LIGHTSOURCE(LightSource.class),DARKSOURCE(DarkSource.class),IMAGEPARTICLEEMITTER(ImageParticleEmitter.class),POINTPARTICLEEMITTER(PointParticleEmitter.class),TEXT(Text.class);
        
        private Class representativeClass;
        
        CoreClasses(Class c)
        {
            representativeClass = c;
        }    
        
        public Class getRepresentativeClass()
        {
            return this.representativeClass;
        }
        
    }
    
    public static enum CoreGroups
    {
        SHADOWCASTER,LIGHTSOURCE,DARKSOURCE,EMITTER,IMAGE,TEXT;       
    }

    
       
    //=======================
    // SceneObject Methods
    //=======================
    
    public abstract void update();
    
    public abstract void draw(GL2 gl);
    
    public abstract SylverVector2f getPosition();
    
    public abstract void setPosition(float x, float y);
    
    public abstract void addedToScene();
    
    public abstract void removedFromScene();
    
    
    
      
    //======================
    // Final Accessors
    //======================
    
    /**
     * Returns the ID of this SceneObject
     * @return ID of the SceneObject
     */
    public final String getID() 
    {
        return ID;
    }

    /**
     * Sets the ID of the SceneObject
     * @param id the new ID for the SceneObject
     */
    public final void setID(String id) 
    {     
         ID = id;
    }
    
    /**
     * Gets the Scene that this SceneObject belongs to
     * @return the Scene that this SceneObject belongs to
     */
    public final Scene getOwningScene()
    {
        return this.owningScene;
    }
    
    /**
     * Sets the owning scene of this SceneObject. This method should exclusively be used
     * in the add(SceneObject) method of a Scene to avoid timing and ownership problems.
     * @param scene Scene to which this SceneObject will belong
     */
    public void setOwningScene(Scene scene)  
    {
        this.owningScene = scene;
    }
    
   

    //=======================
    // Grouping Functionality
    //=======================
    
    /**
     * Maps this SceneObject to a new group. These groups are used to organize SceneObjects.
     * @param grp group for this SceneObject to be a part of
     */
    public final void addToGroup(Enum grp) 
    {
        myGroups.add(grp);
        if (owningScene != null) 
            owningScene.getSceneObjectManager().mapToGroup(grp, this);
        
    }

    /**
     * Removes this SceneObject from association with given group
     * @param grp group to remove from the SceneObject
     */
    public final void removeFromGroup(Enum grp) 
    {
        myGroups.remove(grp);
        
        if (owningScene != null) 
            (owningScene).getSceneObjectManager().unMapFromGroup(grp, this);
        
    }

    /**
     * Returns the set of groups that this SceneObject belongs to.
     * @return the set of groups that this SceneObject belongs to
     */
    public final LinkedHashSet<Enum> getGroups() 
    {
        return myGroups;
    }

    /**
     * Checks to see if this SceneObject is part of a given group
     * @param group group to see if the SceneObject belongs to 
     * @return true if the SceneObject belongs to this group, false otherwise
     */
    public final boolean isInGroup(Enum group) 
    {
        return myGroups.contains(group);
    }
    

}
