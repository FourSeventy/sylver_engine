
package com.silvergobletgames.sylver.graphics;

import java.io.Serializable;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;

/**
 * A simple wrapper round the values required for a color
 * 
 * @author Kevin Glass
 */
public class Color implements Serializable
{

    //seral version id
    private static final long serialVersionUID = 01L;
    
    //The red component of the color 
    public float r;
    //The green component of the color 
    public float g;
    //The blue component of the color 
    public float b;
    //The alpha component of the color 
    public float a = 1.0f;
    

    public static final Color transparent = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    public static final Color white = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public static final Color yellow = new Color(1.0f, 1.0f, 0, 1.0f);
    public static final Color red = new Color(1.0f, 0, 0, 1.0f);
    public static final Color blue = new Color(0, 0, 1.0f, 1.0f);
    public static final Color green = new Color(0, 1.0f, 0, 1.0f);
    public static final Color black = new Color(0, 0, 0, 1.0f);
    public static final Color gray = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    public static final Color cyan = new Color(0, 1.0f, 1.0f, 1.0f);
    public static final Color darkGray = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    public static final Color lightGray = new Color(0.7f, 0.7f, 0.7f, 1.0f);
    public final static Color pink = new Color(255, 175, 175, 255);
    public final static Color orange = new Color(255, 200, 0, 255);
    public final static Color magenta = new Color(255, 0, 255, 255);
    public final static Color nullColor = new Color(-1,-1,-1,1);
    

    //==============
    // Constructors
    //==============
    
    public Color()
    {
        
    }
    
    /**
     * Copy constructor
     * 
     * @param color The color to copy into the new instance
     */
    public Color(Color color) 
    {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
    }

    /**
     * Create a 3 component color
     * 
     * @param r The red component of the color (0.0 -> 1.0)
     * @param g The green component of the color (0.0 -> 1.0)
     * @param b The blue component of the color (0.0 -> 1.0)
     */
    public Color(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1;
    }
    
    /**
     * Create a 4 component color
     * 
     * @param r The red component of the color (0.0 -> 1.0)
     * @param g The green component of the color (0.0 -> 1.0)
     * @param b The blue component of the color (0.0 -> 1.0)
     * @param a The alpha component of the color (0.0 -> 1.0)
     */
    public Color(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = Math.min(a, 1);
    }
    
    public Color(int r, int g, int b) 
    {
        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = 1;
    }

    /**
     * Construct a color from an existing color, but with a new alpha.
     * @param col
     * @param a 
     */
    public Color(Color col, float a)
    {
        this.r = col.r;
        this.g = col.g;
        this.b = col.b;
        this.a = a;
    }
    
    

    //===============
    // Class Methods
    //===============
    
    /**
     * Returns a random color.
     * @param min Minimum value of rgb
     * @return 
     */
    public static Color random(float min) {
        float r = min + (float) Math.random() * (1f - min);
        float g = min + (float) Math.random() * (1f - min);
        float b = min + (float) Math.random() * (1f - min);
        return new Color(r, g, b, 1.0f);
    }

    /**
     * Returns a random grayscale
     */
    public static Color randomGray(float min) {
        float rgb = min + (float) Math.random() * (1f - min);
        return new Color(rgb, rgb, rgb, 1);
    }

    /**
     * Returns an instance of the java.awt color when needed.
     * @return 
     */
    public java.awt.Color getawt() {
        return new java.awt.Color(Math.min(r, 1), Math.min(g, 1), Math.min(b, 1), Math.min(a, 1));
    }

    /**
     * Bind this color to the GL context
     */
    public void bind(GL2 gl) {
        gl.glColor4f(r, g, b, a);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ((int) (r + g + b + a) * 255);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {
        if (other instanceof Color) {
            Color o = (Color) other;
            return ((o.r == r) && (o.g == g) && (o.b == b) && (o.a == a));
        }

        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Color (" + r + "," + g + "," + b + "," + a + ")";
    }

    /**
     * Add another color to this one
     * 
     * @param c The color to add 
     */
    public void add(Color c) {
        r += c.r;
        g += c.g;
        b += c.b;
        a += c.a;
    }

    /**
     * Scale the components of the color by the given value
     * 
     * @param value The value to scale by
     */
    public void scale(float value) {
        r *= value;
        g *= value;
        b *= value;
    }

    
    

}
