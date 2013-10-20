package com.silvergobletgames.sylver.netcode;

import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.core.SceneObject.CoreClasses;
import com.silvergobletgames.sylver.core.SceneObject.SceneObjectClassMask;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.TextureLoader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class SceneObjectDeserializer
{
    
    /**
     * Constructs a scene object from given renderData
     * @param renderData the RenderData to bulid the SceneObject from
     * @return returns a scene object, or a blank image if there was a problem.
     */
    public static SceneObject buildSceneObjectFromRenderData(SceneObjectRenderData renderData)
    {       
        try
        {
            //Get the class
            SceneObjectClassMask mask = renderData.getSceneObjectClass();
            Class cls = mask.getRepresentativeClass();
            Class[] paramTypes = new Class[1];

            //Obtain a handle to the Scene Object's build method
            paramTypes[0] = SceneObjectRenderData.class;
            Method method = cls.getMethod("buildFromRenderData", paramTypes);

            //Create the argument
            Object[] argList = new Object[1];
            argList[0] = renderData;

            //Invoke the method and create the object
            SceneObject sceneObject = (SceneObject)method.invoke(null, argList);
            
            //return the built object
            return sceneObject;
         
        }
        catch(NoSuchMethodException | IllegalAccessException |InvocationTargetException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(SceneObjectDeserializer.class.getName());
            logger.log(Level.SEVERE, "Build Scene Object From Render Data Fail: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
                
            return new Image("textureMissing.jpg");
        }
    }
    
    /**
     * Constructs a scene object from given saveData
     * @param saveData Save data to build the scene object from 
     * @return returns a scene object, or throws an exception if there was a problem with the data
     */
    public static SceneObject buildSceneObjectFromSaveData(SceneObjectSaveData saveData) throws Exception
    {
        try
        {
            //get the SceneobjectClassMask from save data
            SceneObjectClassMask mask = null;                                          
            mask = (SceneObjectClassMask)saveData.dataMap.get("class");
            
                       
            //get the Class
            Class cls = mask.getRepresentativeClass();
            Class[] paramTypes = new Class[1];

            //Obtain a handle to the Scene Object's build method
            paramTypes[0] = SceneObjectSaveData.class;
            Method method = cls.getMethod("buildFromFullData", paramTypes);

            //Create the argument
            Object[] argList = new Object[1];
            argList[0] = saveData;

            //Invoke the method and create the object
            SceneObject sceneObject = (SceneObject)method.invoke(null, argList);
            
            //return the built object
            return sceneObject;
         
        }
        catch(NoSuchMethodException | IllegalAccessException |InvocationTargetException e)
        {
            //log error to console
            Logger logger =Logger.getLogger(SceneObjectDeserializer.class.getName());
            logger.log(Level.SEVERE, "Build Scene Object From Save Data Fail: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
            
            throw new Exception("Scene Object Construction Failed");
        }
    }
      
    
}
