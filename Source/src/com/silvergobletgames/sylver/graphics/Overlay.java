package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.netcode.RenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import java.util.ArrayList;
import java.util.Arrays;
import com.silvergobletgames.sylver.util.SylverVector2f;

public class Overlay 
{

    private Image image;
    private int duration;
    public int elapsedDuration;
    private SylverVector2f relativePosition;
    public boolean useRelativeSize = true;
    private float relativeSize;
    private boolean infiniteDuration = false;
    
    //==============
    // Constructors
    //==============
    
    public Overlay(Image image)
    {
        this(image,0,new SylverVector2f(0,0));
        this.infiniteDuration = true;
    }
    
    public Overlay(Image image, int duration, SylverVector2f relativePosition)
    {
        this.image = image;
        this.duration = duration;
        this.relativePosition = relativePosition;
        this.relativeSize = 1;
    }
    
    
    //===========
    // Accessors
    //===========
    
    public int getDuration()
    {
        return this.duration;
    }
    
    public SylverVector2f getRelativePosition()
    {
        return this.relativePosition;
    }
    
    public void setRelativePosition(float x, float y)
    {
        this.relativePosition.set(x, y);
    }
    
    public void setRelativeSize(float size)
    {
        this.relativeSize = size;
    }
    
    public void setUseRelativeSize(boolean value)
    {
        this.useRelativeSize = value;
    }
    
    public float getRelativeSize()
    {
        return this.relativeSize;
    }

    public boolean isInfiniteDuration()
    {
        return infiniteDuration;
    }
    
    public void setInfiniteDuration()
    {
        this.infiniteDuration = true;
    }
    
    public Image getImage()
    {
        return this.image;
    }
    
    public Overlay copy()
    {
        Overlay returnOverlay = new Overlay(this.image.copy());
        returnOverlay.duration = this.duration;
        returnOverlay.relativePosition = new SylverVector2f(this.relativePosition);
        returnOverlay.infiniteDuration = this.infiniteDuration;
        returnOverlay.relativeSize = this.relativeSize;
        
        return returnOverlay;
    }
    
    
    //====================
    // RenderData Methods
    //====================
    
     public RenderData dumpRenderData() 
     {
         RenderData renderData = new RenderData();
         
         renderData.data.add(0,this.image.dumpRenderData());
         renderData.data.add(1,this.duration);
         renderData.data.add(2,this.relativePosition.x);
         renderData.data.add(3,this.relativePosition.y);
         renderData.data.add(4,this.infiniteDuration);
         renderData.data.add(5,this.useRelativeSize);
         
         return renderData;
     }
     
     public static Overlay buildFromRenderData(RenderData renderData)
     {
         Image image = Image.buildFromRenderData((SceneObjectRenderData)renderData.data.get(0));
         int duration = (int)renderData.data.get(1);
         float x= (float)renderData.data.get(2);
         float y = (float)renderData.data.get(3);
         boolean infinite = (boolean)renderData.data.get(4);
         
         
         Overlay returnOverlay = new Overlay(image, duration, new SylverVector2f(x,y));
         returnOverlay.useRelativeSize = (boolean)renderData.data.get(5);
         
         returnOverlay.infiniteDuration = infinite;
         
         return returnOverlay;
     }
     
     public SceneObjectRenderDataChanges generateRenderDataChanges(RenderData oldData,RenderData newData)
     {
         SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
         int changeMap = 0;
         ArrayList changeList = new ArrayList();
         
         Image dummyImage = new Image("blank.png");
         SceneObjectRenderDataChanges imageChanges = dummyImage.generateRenderDataChanges((SceneObjectRenderData)oldData.data.get(0), (SceneObjectRenderData)newData.data.get(0));
         
         if(imageChanges != null)
         {
             changeList.add(imageChanges);
             changeMap += 1L;
         }
         
        //Non image fields
        for(int i = 1; i <=5; i++)
        {
                   
            if(!oldData.data.get(i).equals( newData.data.get(i)))
            {                 
                changeList.add(newData.data.get(i));
                changeMap += 1L << i;
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
         for(byte i = 0; i <=5; i ++)
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
         
         if(changeData.get(0) != null)
         {
             image.reconcileRenderDataChanges(lastTime, futureTime, (SceneObjectRenderDataChanges)changeData.get(0));
         }
         
         if(changeData.get(1) != null)
         {
             this.duration = (int)changeData.get(1);
         }
         
         float x = this.relativePosition.getX();
         float y = this.relativePosition.getY();
         
         if(changeData.get(2) != null)        
             x = (float)changeData.get(2);
                  
         if(changeData.get(3) != null)
             y = (float)changeData.get(3);
         
         if(changeData.get(2) != null || changeData.get(3) != null)
            this.relativePosition = new SylverVector2f(x,y);
         
         if(changeData.get(4) != null)
         {
             this.infiniteDuration = (boolean)changeData.get(4);
         }
         if(changeData.get(5) != null)
         {
             this.useRelativeSize = (boolean)changeData.get(5);
         }
         
         
        
 
     }
    
    
}
