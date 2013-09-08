package com.silvergobletgames.sylver.graphics;

import com.jogamp.graph.font.FontFactory;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.core.SceneObjectManager;
import com.silvergobletgames.sylver.graphics.TextEffect.TextEffectType;
import com.silvergobletgames.sylver.netcode.NetworkedSceneObject;
import com.silvergobletgames.sylver.netcode.RenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderData;
import com.silvergobletgames.sylver.netcode.SceneObjectRenderDataChanges;
import com.silvergobletgames.sylver.util.LinearInterpolator;
import com.silvergobletgames.sylver.util.SerializableEntry;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class stores data about a text string that we want to render to the screen.
 * @author Justin Capalbo
 */
public class Text extends NetworkedSceneObject implements Anchorable
{   
    
    // The anchor 
    private Anchorable.Anchor anchor = Anchor.BOTTOMLEFT;
    // The string of this Text 
    protected String text;
    // the scale of the text 
    protected float scale = 1;
    // The angle to rotate this text by 
    protected float angle = 0;
    // The color we want this text drawn at 
    public Color color;
    // The text renderer to draw this text with 
    protected TextType textType;
    //map of text effects
    private HashMap<String,TextEffect> textEffects = new HashMap();
    
    //an open ended label interface for the extensible enum pattern
    public static interface TextType{
        
    }
    
    //the core text types 
    public static enum CoreTextType implements TextType{      
        DEFAULT,MENU,CODE;
    }

    
    //================
    // Constructors
    //================    
    
    /**
     * Default constructor
     */
    public Text()
    {     
        this("");
    }
    
    /**
     * Takes just text.
     * @param txt 
     */
    public Text(String txt)
    {
        this(txt,CoreTextType.DEFAULT);
    }

    /**
     * Text and type
     */
    public Text(String txt, TextType textType)
    {
        this.text = txt;
        this.textType = textType;
        this.color = new Color(Color.white);
        this.position = new SylverVector2f(0,0);
        
        this.addToGroup(CoreGroups.TEXT);
    }
    
    public Text(Text old)
    {
        super();
        
        this.text = old.text;
        this.position = new SylverVector2f(old.getPosition().x,old.getPosition().y);
        this.scale = old.scale;
        this.angle = old.angle;
        this.color = new Color(old.color);
        this.textType = old.textType;
        this.owningScene = old.owningScene;
        
        this.addToGroup(CoreGroups.TEXT);
    }
  
    
    ///=====================
    // Scene Object Methods
    //=====================
     
    public void draw(GL2 gl2)
    {
        //get the text renderer
        TextRenderer tr = OpenGLGameWindow.textRenderers.get(textType);
        
        //if the text renderer is null print an error and use the default
        if(tr == null)
        {
             //log error to console
            Logger logger =Logger.getLogger(Text.class.getName());
            logger.log(Level.SEVERE, "Text Renderer " + textType.toString() + ", is not registered");
            logger.addHandler(new ConsoleHandler()); 
        
            tr = OpenGLGameWindow.textRenderers.get(CoreTextType.DEFAULT);
        }

        //model view transforms
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glPushMatrix();
        gl2.glTranslatef(position.x, position.y, 0);
        gl2.glRotatef(angle, 0, 0, 1);
        
        //start the rendering
        tr.setColor(color.getawt());
        tr.begin3DRendering();  

        //draw the text
        if(text != null)          
            tr.draw3D(text, 0, 0,0,scale);       
        tr.flush();
       
        //some hacks because end3D rendering is buggy
        gl2.glPopClientAttrib();
        gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);     
        gl2.glPopAttrib();
        
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glPopMatrix();
        
        //reset bound color to white
        Color.white.bind(gl2);
                
    }
    
    public void update()
    {
         //update TextEffects and remove them if expired   
        for (Iterator<Map.Entry<String,TextEffect>> it = this.textEffects.entrySet().iterator(); it.hasNext();) 
        {
            //get image effect from iterator
            Map.Entry<String,TextEffect> entry = it.next();
            TextEffect effect = entry.getValue();
            
            //update the effect
            effect.update();  
            
            //if expired, remove it
            if(effect.isExpired())
            {   
                //remove effect
                it.remove();
                
                //fire effects on remove
                effect.onRemove(); 
                              
            }
        }
        
    }
    
    public void addedToScene()
    {
        
    }
    
    public void removedFromScene()
    {
        
    }
    
    
    //====================
    //Anchorable Methods
    //====================
    
    @Override
    public void setAnchor(Anchorable.Anchor type){
        if (type != null)
        {
            this.anchor = type;
        }    
    }

    @Override
    public Anchor getAnchor(){
        return anchor;
    }

    @Override
    public SylverVector2f getDimensions() {
        return new SylverVector2f(this.getWidth(), this.getHeight());
    }

    @Override
    public float getScale() {
        return this.scale;
    }

    @Override
    public void setPositionAnchored(float x, float y) {
        SylverVector2f pos = Anchorable.Anchor.getNewPosition(this, x, y);
        setPosition(pos.x, pos.y);
    }
    
    
    //================
    // Class Methods
    //================
   
    /**
     * Sets the color
     */
    public void setColor(Color c){
        this.color = c;
    }
    
    /**
     * Sets the scale
     */
    public void setScale(float s){
        this.scale = s;
    }
    
    /**
     * Sets the angle
     */
    public void setRotation(float a){
        this.angle = a;
    }
    
    /**
     * Sets a new text
     */
    public void setText(String s){
        this.text = s;
    }
    
    public String toString()
    {
        return this.text;
    }
    
    /**
     * sets the render type
     */
    public void setTextType(TextType t){
        this.textType = t;
    }
    
    /**
     * Returns the width of this Text
     * WHY IS THIS SO SAD
     * @return The width of the text, (may be able to account for scale)
     */
    public float getWidth()
    {
        FontMetrics fm = OpenGLGameWindow.fontMetricsMap.get(this.textType);     
        float width = fm.stringWidth(text); 
        width *= scale;
        return (float)width;
    }
    
    public float getHeight()
    {
        FontMetrics fm = OpenGLGameWindow.fontMetricsMap.get(this.textType);     
        float height = fm.getHeight(); 
        height *= scale;
        return (float)height;
    }

    public int length()
    {
        return text.length();
    }
    
    //======================
    // Text Effect Methods
    //======================
    
    /**
     * Adds a render effect to this image without a key. Its key will be set to the effects hashCode
     * @param effect The effect to add
     */
    public boolean addTextEffect(TextEffect effect)
    {
        return this.addTextEffect(Integer.toString(effect.hashCode()),effect);
    }
    
    /**
     * Adds a render effect to this image with an explicit key.
     * @param name The key that this effect can be referenced with
     * @param effect The effect to add
     */
    public boolean addTextEffect(String name, TextEffect effect)
    {
        //if we dont have a render efect of the same type already add it
        if(this.hasTextEffectType(effect.textEffectType) == false)
        {
            //add the effect
            this.textEffects.put(name,effect);

            //set the render effects owning image
            effect.setOwningText(this);

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
    public void removeTextEffect(TextEffect effect)
    {
        //remove the effect
        for(String key:this.textEffects.keySet())
        {
           TextEffect keyEffect = this.textEffects.get(key);
           
           if(keyEffect == effect)
           {
               this.removeTextEffect(key);
               return;
           }
               
        }
    }
    
    /**
     * Removes the render effect associated to the given key from the image
     * @param key Key associated ot the render effect we want to remove
     */
    public void removeTextEffect(String key)
    {
        //remove the effect
        TextEffect effect = this.textEffects.remove(key);
        
        //tell the effect that it was removed
        if(effect != null)
            effect.onRemove();   
      
    }
    
    /** 
     * Removes all render effects on this image. 
     */
    public void removeAllTextEffects() 
    {
        //call onRemove() for all the effects
        for (TextEffect effect: textEffects.values())         
            effect.onRemove();
        
        //clear the list
        textEffects.clear();
        
    }
    
    public boolean hasTextEffect(String name)
    {
        return this.textEffects.containsKey(name);
    }
    
    public boolean hasTextEffectType(TextEffectType type)
    {
        for(TextEffect effect: this.textEffects.values())
        {
            switch(type)
            {             
                case XTRANSLATE: if(effect.textEffectType.equals(TextEffectType.XTRANSLATE)){return true;} break;
                case YTRANSLATE: if(effect.textEffectType.equals(TextEffectType.YTRANSLATE)){return true;} break;
                case ANGLE: if(effect.textEffectType.equals(TextEffectType.ANGLE)){return true;} break;
                case SCALE: if(effect.textEffectType.equals(TextEffectType.SCALE)){return true;} break;
                case COLOR: if(effect.textEffectType.equals(TextEffectType.COLOR)){return true;} break;
            }
        }
        return false;
    }
    
    
    
    //====================
    // RenderData Methods
    //==================== 
    
    public SceneObjectRenderData dumpRenderData() 
    {
        SceneObjectRenderData renderData = new SceneObjectRenderData(CoreClasses.TEXT,this.ID);

        renderData.data.add(0,text);
        renderData.data.add(1,this.getPosition().x);
        renderData.data.add(2,this.getPosition().y);
        renderData.data.add(3,this.scale);
        renderData.data.add(4,this.angle);
        renderData.data.add(5,this.color);
        renderData.data.add(6,this.textType);
        //render data for all the effects
         ArrayList<SerializableEntry> renderEffectData = new ArrayList();
         for(String key: this.textEffects.keySet()){
             renderEffectData.add(new SerializableEntry(key,this.textEffects.get(key).dumpRenderData()));
         }
         renderData.data.add(7,renderEffectData);
        
        return renderData;
    }
       
    public static Text buildFromRenderData(SceneObjectRenderData renderData)
    {
        String text = (String)renderData.data.get(0);
        Text txt = new Text(text);
        txt.setID(renderData.getID());
        txt.setPosition((float)renderData.data.get(1), (float)renderData.data.get(2));
        txt.setScale((float)renderData.data.get(3));
        txt.setRotation((float)renderData.data.get(4));
        txt.setColor((Color)renderData.data.get(5));
        txt.setTextType((TextType)renderData.data.get(6));
        
        //lightEffects
        ArrayList<SerializableEntry> textEffectData = (ArrayList)renderData.data.get(7);
        for (SerializableEntry<String, RenderData> textEffectEntry : textEffectData)
        {
            //build either a lightEffect, or a MultiLightEffect
            TextEffect effect = null;
            if(textEffectEntry.getValue().data.get(0).equals("1"))            
                effect = TextEffect.buildFromRenderData(textEffectEntry.getValue());                     
            else
                effect = MultiTextEffect.buildFromRenderData(textEffectEntry.getValue());
            
            txt.addTextEffect(textEffectEntry.getKey(), effect);
        }
        
        return txt;
    }
    
    public SceneObjectRenderDataChanges generateRenderDataChanges(SceneObjectRenderData oldData,SceneObjectRenderData newData)
    {
        SceneObjectRenderDataChanges changes = new SceneObjectRenderDataChanges();
        
        int changeMap = 0;
        changes.ID = this.ID;
        ArrayList changeList = new ArrayList();
        
        forloop: for(int i = 0; i < 7; i++)
        {
            
            //check for effects on fields and dont generate changes for those fields
            switch(i)
            {
                case 1: if(this.hasTextEffectType(TextEffectType.XTRANSLATE)){ continue forloop;} break; 
                case 2: if(this.hasTextEffectType(TextEffectType.YTRANSLATE)){ continue forloop;} break; 
                case 3: if(this.hasTextEffectType(TextEffectType.SCALE)){ continue forloop;} break; 
                case 4: if(this.hasTextEffectType(TextEffectType.ANGLE)){ continue forloop;} break; 
                case 5: if(this.hasTextEffectType(TextEffectType.COLOR)){ continue forloop;} break; 
            }
            
            if(!oldData.data.get(i).equals( newData.data.get(i)))
            {
                changeList.add(newData.data.get(i));
                changeMap += 1 << i;
            }
        }
        
        //====================================
        //generate changes for Render Effects 
        //====================================
        
        ArrayList<SerializableEntry<String,SceneObjectRenderData>> effectAdds = new ArrayList();
        ArrayList<String> effectRemoves = new ArrayList();
        ArrayList<SerializableEntry> oldRenderData = (ArrayList)oldData.data.get(7);
        ArrayList<SerializableEntry> newRenderData = (ArrayList)newData.data.get(7);
        
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
                effectAdds.add(new SerializableEntry(newKey,(RenderData)newRenderMap.get(newKey)));
        }
        
        //check for subtractions 
        for(String oldKey: oldRenderMap.keySet())
        {
            if(!newRenderMap.containsKey(oldKey))
                effectRemoves.add(oldKey);
        }
        
         if(!effectAdds.isEmpty())
        {
            changeList.add(effectAdds);
            changeMap += 1L << 7;
        }
        if(!effectRemoves.isEmpty())
        {
            changeList.add(effectRemoves);
            changeMap += 1L << 8;
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
        
        if(changeData.get(0)!= null)
           this.text = (String)changeData.get(0);
        
         //if its a fresh packet, build the interpolators for position      
        //clear old interpolators
        this.interpolators.clear();

        //x position interpolator
        if(changeData.get(1) != null)
        {
            float startPosition=  interpolators.containsKey("xPosition")? (float)interpolators.get("xPosition").getB() : this.getPosition().x;          
            LinearInterpolator lerp1= new LinearInterpolator(startPosition,(float)changeData.get(1),lastTime,futureTime);
            this.interpolators.put("xPosition",lerp1);
        }
        else
            this.interpolators.remove("xPosition");

        //y position interpolator
        if(changeData.get(2) != null)
        {
            float startPosition=  interpolators.containsKey("yPosition")? (float)interpolators.get("yPosition").getB() : this.getPosition().y;
            LinearInterpolator lerp2= new LinearInterpolator(startPosition,(float)changeData.get(2),lastTime,futureTime);
            this.interpolators.put("yPosition",lerp2);
        }
        else
            this.interpolators.remove("yPosition");
        
        //scale interpolator     
        if(changeData.get(3) != null)
        {   
            float startPosition=  interpolators.containsKey("scale")? (float)interpolators.get("scale").getB() : scale;
            LinearInterpolator lerp3= new LinearInterpolator(startPosition,(float)changeData.get(3),lastTime,futureTime);
            this.interpolators.put("scale",lerp3);
        }
        else
            this.interpolators.remove("scale");
        
        //angle interpolator       
        if(changeData.get(4) != null)
        {
            float startPosition=  interpolators.containsKey("angle")? (float)interpolators.get("angle").getB() : angle;
            LinearInterpolator lerp4= new LinearInterpolator(startPosition,(float)changeData.get(4),lastTime,futureTime);
            this.interpolators.put("angle",lerp4);
        }
        else
            this.interpolators.remove("angle");
        
        if(changeData.get(5) != null)
            this.color = (Color)changeData.get(5);
        if(changeData.get(6) != null)
            this.textType = (TextType)changeData.get(6);
        
         //added renderEffects
        if(changeData.get(7) != null)
        {
             ArrayList<SerializableEntry<String, RenderData>> effectAdds = (ArrayList)changeData.get(7);
            for (SerializableEntry<String, RenderData> entry : effectAdds)
            {
                TextEffect effect = null;
                String key = entry.getKey();
                
                if(entry.getValue().data.get(0).equals("1"))            
                    effect = TextEffect.buildFromRenderData(entry.getValue());                     
                else
                    effect = MultiTextEffect.buildFromRenderData(entry.getValue());
                
                this.addTextEffect(key, effect);
                              
            }           
        }
        
        //removed renderEffects
        if(changeData.get(8) != null)
        {
            ArrayList<String> removeList = (ArrayList<String>)changeData.get(8);
            
            //remove the effects from the remove list only if they are repeating
            for(String s: removeList)
            {
                if(this.textEffects.get(s) != null && this.textEffects.get(s).repeat)
                   this.removeTextEffect(s);
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
            if(this.interpolators.containsKey("xPosition") && currentTime <= this.interpolators.get("xPosition").getb())             
               x = (float)this.interpolators.get("xPosition").interp(currentTime);
            
            if(this.interpolators.containsKey("yPosition")&& currentTime <= this.interpolators.get("yPosition").getb())
               y = (float)this.interpolators.get("yPosition").interp(currentTime);

            this.setPosition(x, y);
            
            //interpolate scale
            if(this.interpolators.containsKey("scale") && currentTime <= this.interpolators.get("scale").getb())
                this.scale = (float)this.interpolators.get("scale").interp(currentTime);
            
            //interpolate angle
            if(this.interpolators.containsKey("angle") && currentTime <= this.interpolators.get("angle").getb())
                this.angle = (float)this.interpolators.get("angle").interp(currentTime);
        }
    }
    
    
    
}
