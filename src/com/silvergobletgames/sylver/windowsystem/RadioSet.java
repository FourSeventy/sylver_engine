package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.Scene;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;


/**
 *
 * @author mike
 */
public class RadioSet<E> extends WindowComponent {
    
    
    //the hashmap holding all of the elements
    private HashMap<String,E> elements = new HashMap<>();
    
    //the currently selected key
    private Text currentSelection;
    
    //list of the circle buttons
    private ArrayList<Button> circles = new ArrayList<>();
    //list of text corresponding to buttons
    private ArrayList<Text> labels = new ArrayList<>();
    
    
    //================
    // Constructors
    //================
    
    public RadioSet(float x, float y)
    {
        super(x,y);

    }

    
    
   //=====================
   // Scene Object Methods
   //=====================
      
    public void update()
    {
        for(Button c: circles)
        {
            c.update();
        }
        
    }
    
    public void draw(GL2 gl)
    {
        if(!hidden)
        {
            //draw all the things!
            for(Button circle:circles)
            {
                circle.draw(gl);
            }

            for(Text t: labels)
            {           
                 t.draw(gl);           
            }
        }
    }
      
    public void setPosition(float x, float y)
    {
        this.xPosition = x;
        this.yPosition = y;
        
        for(int i = 0; i< circles.size(); i++)
        {
            circles.get(i).setPosition(this.xPosition,this.yPosition + 25 * i);      
        }
        
        for(int i = 0; i< labels.size(); i++)
        {
            labels.get(i).getPosition().x = this.xPosition +30;
            labels.get(i).getPosition().y = this.yPosition + 3 + 25* i; 
        }
    }
    
    public void setOwningScene(Scene owningScene)
    {
        this.owningScene = owningScene;
        
        for(Button buttons: this.circles)
            buttons.setOwningScene(owningScene);
    }
    
    
    //=====================
    // Class Methods
    //=====================
    
    /**
     * Add an element to the radio set
     * @param entry 
     */
    public void addElement(HashMap.SimpleEntry<String,E> entry)
    {
        //put the element into the hashmap
        elements.put(entry.getKey(), entry.getValue());
        
        //add the label
        Text t = new Text(entry.getKey());
        t.getPosition().x = this.xPosition +50;
        t.getPosition().y = this.yPosition + 20* elements.size();   
        t.setScale(.7f);
        labels.add(t);
        
        //add the circle
        final Button m = new Button("circle.png",this.xPosition,this.yPosition + 20 * elements.size(),20,20);
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                if(e.getActionCommand().equals( "clicked"))
                   circle_clicked(labels.get(circles.indexOf(m)));
               
            }
            });
        m.setOwningScene(owningScene);
        circles.add(m);
    }
    
    /**
     * Get the element that is currently selected
     * @return 
     */
    public E getSelectedValue()
    {
        return elements.get(currentSelection.toString());
    }
    
    public void selectElement(String str){
        for (Text t : labels)
        {
            if (t.toString().equals(str)){
                this.circle_clicked(t);
                break;
            }
        }
    }
    
    private void circle_clicked(Text text)
    {      
        //reset the old selections image
        if(currentSelection != null)
            circles.get(labels.indexOf(currentSelection)).setImage(new Image("circle.png"))  ; 
        
        //set the current selections image
        circles.get(labels.indexOf(text)).setImage( new Image("circleSelected.png"));
        
        //set the current selection key
        this.currentSelection =text;
        
        //fire event alerting that the selection has been changed
        this.fireAction(this, "selectionChanged");
    }
    
    public void setDefaultSelection(String selection)
    {
        for(Text t :this.labels)
        {
            if(selection.equals(t.toString()))
            {
                this.currentSelection = t;
                //set the current selections image
                 circles.get(labels.indexOf(t)).setImage( new Image("circleSelected.png"));
                 this.fireAction(this, "selectionChanged");
            }
        }
    }
}
