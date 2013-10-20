
package com.silvergobletgames.sylver.graphics;


public class Cursor
{
    // cursor image
    private Image cursorImage;
    
    //cursor anchor
    private Anchorable.Anchor cursorAnchor;
    
    
    //=============
    // Constructor
    //=============
    
    public Cursor(Image image, Anchorable.Anchor anchor)
    {
        this.cursorImage = image;
        this.cursorAnchor = anchor;
        
        this.cursorImage.setAnchor(anchor);
    }
    
    
    //=============
    // Accessors
    //=============
    
    public Image getImage()
    {
        return this.cursorImage;
    }
    
    public Anchorable.Anchor getAnchor()
    {
        return this.cursorAnchor;
    }
    
}
