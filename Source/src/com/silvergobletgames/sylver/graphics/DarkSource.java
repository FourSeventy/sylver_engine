
package com.silvergobletgames.sylver.graphics;

import com.jogamp.opengl.util.texture.Texture;
import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.netcode.NetworkedSceneObject;
import com.silvergobletgames.sylver.netcode.SavableSceneObject;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import com.silvergobletgames.sylver.netcode.SceneObjectSaveData;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.ArrayList;
import java.util.Arrays;
import javax.media.opengl.GL2;

/**
 *
 * @author Mike
 */
public class DarkSource extends NetworkedSceneObject implements SavableSceneObject
{
    
    private float width;
    private float height;
    private float intensity = 1;
    private Texture texture;
    private String ref;
    
    //=====================
    // Constructors
    //=====================     
    
    /**
     * Constructs a new DarkSource with its options set to default values;
     */
    public DarkSource(String ref)
    {
              
        this.position = new SylverVector2f();   
        this.ref = ref;
        this.texture =Game.getInstance().getAssetManager().getTextureLoader().getTexture(ref);
        this.width = texture.getWidth();
        this.height =texture.getHeight();
        
        this.addToGroup(CoreGroups.DARKSOURCE);
    }

    //=====================
    // Scene Object Methods
    //=====================
    
    public void update()
    {
    }

    public void draw(GL2 gl)
    {
    }  

    public void addedToScene()
    {
      
    }

    public void removedFromScene()
    {
    
    }
    
    //==============
    // Accessors
    //==============
    
    public float getWidth()
    {
        return this.width;
    }
    
    public float getHeight()
    {
        return this.height;
    }
    
    public void setDimentions(float x, float y)
    {
        this.width = x;
        this.height = y;
    }
    
    public Texture getTexture()
    {
        return this.texture;
    }
    
    public float getIntensity()
    {
        return this.intensity;
    }
    
    public void setIntensity(float intensity)
    {
        this.intensity = intensity;
    }

    
    //====================
    //Render Data Methods
    //====================
    
    @Override
    public SceneObjectRenderData dumpRenderData()
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.DARKSOURCE,this.ID);

        renderData.data.add(0,this.getPosition().x);
        renderData.data.add(1,this.getPosition().y);
        renderData.data.add(2, ref);
        renderData.data.add(3, width);
        renderData.data.add(4, height);
        renderData.data.add(5, intensity);
        
        return renderData;
    }
    
    public static DarkSource buildFromRenderData(SceneObjectRenderData renderData)
    {
        
        SylverVector2f position = new SylverVector2f((float)renderData.data.get(0),(float)renderData.data.get(1));
        String ref = (String)renderData.data.get(2);
        float width = (float)renderData.data.get(3);
        float height = (float)renderData.data.get(4);
        float intensity = (float)renderData.data.get(5);
        
        DarkSource d = new DarkSource(ref);
        d.setID(renderData.getID());
        d.setDimentions(width, height);
        d.setIntensity(intensity);
        d.setPosition(position.x, position.y);
        
        return d;
    }

    @Override
    public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData, SceneObjectRenderData newData)
    {
        SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
        int changeMap = 0;
        changes.ID = this.ID;
        ArrayList changeList = new ArrayList();
        
        //Non effect fields
        for(int i = 0; i < 6; i++)
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

    @Override
    public void reconcileRenderDataChanges(long lastTime, long futureTime, SceneObjectRenderDataChanges renderDataChanges)
    {
         //construct an arraylist of data that we got, nulls will go where we didnt get any data
        int fieldMap = renderDataChanges.fields;
        ArrayList rawData = new ArrayList();
        rawData.addAll(Arrays.asList(renderDataChanges.data));        
        ArrayList changeData = new ArrayList();
        for(int i = 0; i <6; i ++)
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
    }

    @Override
    public void interpolate(long currenttime)
    {
        
    }

    
    //=================
    //Savable Methods
    //=================
    @Override
    public SceneObjectSaveData dumpFullData()
    {
         //=====================================
        // WARNING
        //-Making changes to this method could
        //break saved data
        //======================================
        SceneObjectSaveData saved = new SceneObjectSaveData(CoreClasses.DARKSOURCE,this.ID);
        
        saved.dataMap.put("x", this.getPosition().x);
        saved.dataMap.put("y", this.getPosition().y);
        saved.dataMap.put("width", this.width);
        saved.dataMap.put("height", this.height);
        saved.dataMap.put("intensity", this.intensity);
        saved.dataMap.put("texture",this.ref);
        
        return saved;
    }
    
    public static DarkSource buildFromFullData(SceneObjectSaveData saved) 
    {
        float x = (float)saved.dataMap.get("x");
        float y = (float)saved.dataMap.get("y");
        float width = (float)saved.dataMap.get("width");
        float height = (float)saved.dataMap.get("height");
        float intensity = (float)saved.dataMap.get("intensity");
        String ref = (String)saved.dataMap.get("texture");
        
        DarkSource d = new DarkSource(ref);
        d.setPosition(x, y);
        d.intensity = intensity;
        d.setDimentions(width, height);
        d.ID = (String)saved.dataMap.get("id");
        return d; 
    }
    
}
