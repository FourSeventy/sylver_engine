package com.silvergobletgames.sylver.windowsystem;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.core.InputHandler;
import com.silvergobletgames.sylver.core.InputSnapshot;
import com.silvergobletgames.sylver.graphics.Color;
import com.silvergobletgames.sylver.graphics.Image;
import com.silvergobletgames.sylver.graphics.Text;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author mike
 */
public class Menu extends Window {
    
    //title bar
    private Button titleBar;  
    //close button
    private Button closeButton;   
    //title
    private String title;
    private boolean dragging = false;
    
    
    private float mouseDragXAdjust;
    private float mouseDragYAdjust;
    
    private Button bottomEdge ;
    private Button leftEdge ;
    private Button rightEdge;

    
    //===============
    // Constructors
    //===============

    public Menu(Image image, float xPosition, float yPosition, float width, float height)
    {
        super(image, xPosition, yPosition, width, height);
    }
    
    public Menu(String title,float xPosition, float yPosition, float width, float height)
    {
        super(new Image("menuBlank.png"),xPosition,yPosition,width,height);
        
        //set title
        this.title = title;
        
        //build title bar
        Text t = new Text(title);
        t.setScale(0.6f);
        t.color = new Color(Color.black);       
        titleBar = new Button("topBar.png",t,0 , height - 20 , width - 20, 20);
        titleBar.setTextPadding(5, 5);
        titleBar.dontKillClick = true;
        titleBar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                if(e.getActionCommand().equals("mouseDown"))
                {
                    dragging = true;
                    updateMouseDragAdjustment();
                }
                
                if(e.getActionCommand().equals("mouseUp"))
                {
                    dragging = false;
                } 
            }
        });
        this.addComponent(titleBar);
     
        //build close button
        closeButton = new Button("close.png", width - 20,height - 20 ,20,20);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                if(e.getActionCommand().equals("clicked"))
                {
                    closeButton_clicked();
                }
            }           
        });     
        this.addComponent(closeButton);
        
        //add edges
        bottomEdge = new Button("topBar.png",0,0,width,5);
        this.addComponent(bottomEdge);
        leftEdge = new Button("sideBar.png",0,0,5,height-20);
        this.addComponent(leftEdge);
        rightEdge = new Button("sideBar.png", width - 5,0,5,height-20);
        this.addComponent(rightEdge);
        
    }
    
    //===============
    // Class Methods
    //=============== 
    
    public void updateMouseDragAdjustment()
    {
        
            InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();
            this.mouseDragXAdjust = input.getScreenMouseLocation().x - getPosition().x;
            this.mouseDragYAdjust = input.getScreenMouseLocation().y - getPosition().y;
        
    }
    
    public void update()
    {
        if(this.dragging)
        {
            
                InputSnapshot input = Game.getInstance().getInputHandler().getInputSnapshot();
                float xPosition = input.getScreenMouseLocation().x - this.mouseDragXAdjust;
                float yPosition = input.getScreenMouseLocation().y - this.mouseDragYAdjust;
                
                this.setPosition((int)xPosition,(int)yPosition);
            
        }
        super.update();        
    }
    
    public void closeButton_clicked()
    {
        this.close();
    }
    
}
