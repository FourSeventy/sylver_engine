
package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.graphics.ShaderLoader;
import com.silvergobletgames.sylver.graphics.TextureLoader;

/**
 * This class manages access to the texture and shader loader classes.
 * @author Mike
 */
public class AssetManager {
    
    private TextureLoader textureLoader;
    private ShaderLoader shaderLoader;
    
    //==============
    // Constructor
    //==============
    
    /**
     * Creates a new asset manager instance
     */
    protected AssetManager()
    {
        //initialize our asset loaders
        textureLoader = new TextureLoader();
        shaderLoader = new ShaderLoader();
    }
    
    //===========
    // Acessors
    //===========
    
    /**
     * Gets the TextureLoader
     * @return the TextureLoader
     */
    public TextureLoader getTextureLoader()
    {
        return this.textureLoader;
    }
    
    /**
     * Gets the ShaderLoader
     * @return the ShaderLoader
     */
    public ShaderLoader getShaderLoader()
    {
        return this.shaderLoader;
    }
    
            
    
}
