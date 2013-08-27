
package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.Scene;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 *
 * @author mike
 */
public class TabPane extends WindowComponent {

    //background
    private Image background;
    
    //edges
    private Image topEdge;
    private Image bottomEdge;
    private Image leftEdge;
    private Image rightEdge;
    
    //tabs
    private ArrayList<Button> tabs = new ArrayList<>();
    
    //selected tab
    private int selectedTab=0;
    
    //components it holds
    private ArrayList<ArrayList<WindowComponent>> componentList = new ArrayList<>();
    
    
    //===============
    // Constructors
    //===============
    
    public TabPane(float x, float y, float width, float  height)
    {
        super(x,y);
        this.width = width;
        this.height = height;
        
        //construct edges
        topEdge = new Image("topBar.png");
        topEdge.setPosition(x, y + height - 5);
        topEdge.setDimensions(width, 5);
        
        bottomEdge = new Image("topBar.png");
        bottomEdge.setPosition(x,y);
        bottomEdge.setDimensions(width,5);
        
        leftEdge = new Image("sideBar.png");
        leftEdge.setPosition(x, y);
        leftEdge.setDimensions(5, height);
        
        rightEdge = new Image("sideBar.png");
        rightEdge.setPosition(x + width - 5, y);
        rightEdge.setDimensions(5, height);
        
        //contruct background
        this.background = new Image("tabControlBackground.png");
        this.background.setPosition(x, y);
        this.background.setDimensions(width, height);
            
    }
    
    
    //===============
    // Class Methods
    //===============
    
    public void addTab(String s)
    {
        //set up tab
        Text t = new Text(s);
        t.setTextType(Text.CoreTextType.DEFAULT);
        t.setColor(new Color(Color.black));
        t.setScale(.65f);
        String image = "tabUnselected.png";
        if(tabs.isEmpty())
            image = "tabSelected.png";
            
        float accumWidth = 0;
        for(Button tab: tabs)
           accumWidth += tab.width;
        
        final Button tabButton = new Button(image,t,accumWidth + 5,this.yPosition + this.height - 5,t.getWidth() + 20,30);  
        tabButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                if(e.getActionCommand().equals("clicked"))
                {                 
                    //switch the components
                    switchToTab(tabs.indexOf(tabButton));
                    
                    fireAction(this,"tabChanged");
                }
            }
            
        });
        tabButton.setTextPadding(15, 10);
        tabButton.setOwningScene(owningScene);
        this.tabs.add(tabButton);
        
        //set up components
        componentList.add(new ArrayList<WindowComponent>());
    }
    
    /**
     * Removes the specified tab
     */
    public void removeTab(int index){
        tabs.remove(index);
        componentList.remove(index);
        this.selectedTab = Math.min(this.selectedTab, tabs.size()-1);
        switchToTab(selectedTab);
    }
    
    /**
     * Removes the end tab
     */
    public void removeLastTab(){
        removeTab(tabs.size() - 1);
    }
    
    @Override
    public void draw(GL2 gl) 
    {
        if(!hidden)
        {
            //draw background
            this.background.draw(gl); 

            //draw edges
            topEdge.draw(gl);
            bottomEdge.draw(gl);
            leftEdge.draw(gl);
            rightEdge.draw(gl); 

            //draw tabs
            for(Button t: tabs)
            {
                t.draw(gl);
            }

            //draw components
                for(WindowComponent wc: this.componentList.get(selectedTab))    
                    wc.draw(gl);    
        }
    }

    @Override
    public void update()
    {
            
        //update tabs
        for(Button t: tabs)
        {
            t.update();
        }
        
        //update all of the components of the selected tab
        ArrayList<WindowComponent> updateList = new ArrayList<>(this.componentList.get(selectedTab));
        for(int i = updateList.size() -1; i >= 0; i--)
        {
            updateList.get(i).update();      
        }      
       
    }
    
    public void setOwningScene(Scene scene)
    {
        this.owningScene = scene;
        
        for(Button button: this.tabs)
            button.setOwningScene(scene);
        
        for(ArrayList<WindowComponent> list: this.componentList)
        {
            for(WindowComponent wc: list)
                wc.setOwningScene(scene);
        }
    }
    
    
    @Override
    public void setPosition(float x, float y)
    {
        
        this.xPosition = x;
        this.yPosition =y;
        
         this.background.setPosition(x, y);
         topEdge.setPosition(x, y + height - 5);
         bottomEdge.setPosition(x,y);
         leftEdge.setPosition(x, y);
         rightEdge.setPosition(x + width - 5, y);
         
         //draw tabs
        for(int i = 0; i < tabs.size(); i++)
        {
            float accumWidth  = 0;
            for(int j = 0; j< i; j++)
                accumWidth += tabs.get(j).getWidth();
            
            tabs.get(i).setPosition(x  + 5 + accumWidth,y+ this.height -5);
        }
        
         for(WindowComponent wc: this.componentList.get(selectedTab))
             wc.setPosition( this.xPosition + wc.xWindowRelative, this.yPosition + wc.yWindowRelative);
        
    }
    
    public void addComponent(WindowComponent c,int tab)
    {
        this.componentList.get(tab).add(c);
        c.setOwningScene(owningScene);
    }
    
    public void removeComponent(WindowComponent c, int tab)
    {
        this.componentList.get(tab).remove(c);
    }
    
    public void switchToTab(int i)
    {                    
        //switch the images
        if(selectedTab <= tabs.size()-1)
           tabs.get(selectedTab).setImage(new Image("tabUnselected.png"));        
        tabs.get(i).setImage(new Image("tabSelected.png"));
        
        //set selected tab variable to new tab
        selectedTab = i;
        
        for(WindowComponent wc: this.componentList.get(selectedTab))
             wc.setPosition( this.xPosition + wc.xWindowRelative, this.yPosition + wc.yWindowRelative);
    }
    
    public int getSelectedTab()
    {
        return this.selectedTab;
    }
    
    /**
     * Return number of tabs
     * @return 
     */
    public int size(){
        return tabs.size();
    }
    
    /**
     * Clears the tabpane of its contents
     */
    public void clear(){
        componentList.clear();
        tabs.clear();
    }   
    
    public void refreshTabNames(String s){
        for (int i=0; i<tabs.size(); i++){
            tabs.get(i).setText(s + " " + i);
        }
    }
}
