package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.core.Scene.Layer;
import com.silvergobletgames.sylver.util.HashBag;
import java.util.*;

/**
 * This class keeps mappings of all the scene objects in the current scene. It maps
 * scene objects according to their ID, layer and groups
 * @author Mike
 */
public class SceneObjectManager 
{
    
    //Contains all of the SceneObjects in a Scene, mapped by a String ID.
    private HashMap<String, SceneObject> sceneObjects = new HashMap();   
    
    //contains all of the layer mappings for the scene objects
    private TreeMap<Layer, ArrayList<SceneObject>> sceneObjectsLayerMap = new TreeMap();
    
    //Contains all of the group mappings for the scene objects
    private HashMap<Enum, LinkedHashSet<SceneObject>> sceneObjectGroupMap = new HashMap();
    

    //=====================
    // Constructors
    //===================== 
    
    public SceneObjectManager()
    {
        
        //init layers map
        this.sceneObjectsLayerMap.put(Layer.BACKGROUND, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.PARALLAX1, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.PARALLAX2, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.PARALLAX3, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.PARALLAX4, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.PARALLAX5, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.FOREGROUND1, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.FOREGROUND2, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.ATTACHED_BG, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.MAIN, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.ATTACHED_FG, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.WORLD_HUD, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.HUD, new ArrayList());
        this.sceneObjectsLayerMap.put(Layer.MENU, new ArrayList());
    }
    
    
    //=====================
    // Class Methods
    //===================== 

    /**
     * Adds a SceneObject to the manager
     * @param sceneObject SceneObject to map
     * @param layer Layer that this scene object is in
     */
    public void add(SceneObject sceneObject, Layer layer) 
    {
        //adds it to the master list
        sceneObjects.put(sceneObject.getID(), sceneObject);
               
        //add the scene object to its groups
        for(Enum group: sceneObject.getGroups()) 
        {
            //if we dont have an entry for this group yet, make one
            if(sceneObjectGroupMap.get(group) == null)
                sceneObjectGroupMap.put(group, new LinkedHashSet<SceneObject>());
                
            //add the sceneobject
            sceneObjectGroupMap.get(group).add(sceneObject);
        }
        
        //add to layer mapping
        this.sceneObjectsLayerMap.get(layer).add(sceneObject);
          
        
    }

    /**
     * Removes a SceneObject from the manager, and from any mappings that it was in
     * @param sceneObject SceneObject to remove
     */
    public void remove(SceneObject sceneObject)
    {
        //remove from the master list
        sceneObjects.remove(sceneObject.getID());

        //remove from its groupings
        LinkedHashSet<Enum> eGroups = sceneObject.getGroups();
        Iterator<Enum> iter = eGroups.iterator();
        while (iter.hasNext()) {
            sceneObjectGroupMap.get(iter.next()).remove(sceneObject);
        }
        
        //remove from layer mapping
        ArrayList<SceneObject> layerList;
        for (Layer layer: Layer.values())
        {
            layerList = sceneObjectsLayerMap.get(layer);
            if (layerList.remove(sceneObject)) {
                continue;
            }
        }
        
  
    }

    /**
     * Gets a SceneObject by its ID. Returns NULL if the ID does not exist.
     * @param ID ID of the scene object to retrieve
     * @return SceneObject, or NULL if the ID does not exist.
     */
    public SceneObject get(String ID) 
    {
        if (ID == null) 
            return null;
        
        return sceneObjects.get(ID);
    }
    
    /**
     * Returns an arrayList of all the SceneObjects in a group
     * @param grp Enum to get a list of scene objects from
     * @return Array of SceneObjects
     */
    public ArrayList<SceneObject> get(Enum grp) 
    {
        //if the group isnt empty
        if (!sceneObjectGroupMap.isEmpty()) 
        {
            //turn the group into a list
            LinkedHashSet<SceneObject> entitySet = sceneObjectGroupMap.get(grp);
            ArrayList<SceneObject> list = new ArrayList<>();
            
            
            //build list with group
            if(entitySet != null)
            {
                for(SceneObject object: entitySet)
                    list.add( object);
            }
            
            //Return the list
            return list;
        }
        //Else return an empty array
        return new ArrayList();
    }
    
    /**
     * Gets an arrayList of sceneObjects corresponding to a particular layer
     * @param layer to pick the scene objects from
     * @return ArrayList of scene objects
     */
    public ArrayList<SceneObject> get(Layer layer)
    {
        return this.sceneObjectsLayerMap.get(layer);
    }

    /**
     * Returns the layer of the given scene object.
     * @param object Scene object to determine the layer off
     * @return Layer of given scene object, or null if its not mapped with the manager.
     */
    public Layer getLayerOfSceneObject(SceneObject object)
    {
        for(Layer layer: Layer.values())
        {
            ArrayList<SceneObject> list =this.sceneObjectsLayerMap.get(layer);
            
            if(list.contains(object))
                return layer;
        }
        
        return null;
    }

    /**
     * Mapps a SceneObject to a group
     * @param grp
     * @param e 
     */
    public void mapToGroup(Enum grp, SceneObject e) 
    {

        //if the group list is null, make a new group for it
        if(sceneObjectGroupMap.get(grp) == null)
            sceneObjectGroupMap.put(grp, new LinkedHashSet<SceneObject>());
        
        sceneObjectGroupMap.get(grp).add(e);
    }

    /**
     * Removes a SceneObject from a particular group
     */
    public void unMapFromGroup(Enum grp, SceneObject e) 
    {
        LinkedHashSet s = sceneObjectGroupMap.get(grp);
        
        if(s != null)
          s.remove(e);
    }
    
    /**
     * Generates an id that is garunteed to have no duplicates in the scene.
     * @return 
     */
    public String generateUniqueID()
    {
        //Generates an id
        String ID;
        ID = "$" + UUID.randomUUID().toString().substring(0, 7);
        
        //checks to make sure its unique
        while(sceneObjects.containsKey(ID))
            ID = "$" + UUID.randomUUID().toString().substring(0, 7);
        
        return ID;
    }
    
   
}
