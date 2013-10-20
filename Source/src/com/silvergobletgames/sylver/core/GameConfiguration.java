package com.silvergobletgames.sylver.core;

import java.net.URI;
import java.nio.file.Path;

/**
 * This class contains the configuration for the game. It includes the EngineSettings class which are 
 * the settings for graphics and audio. This class is passed into Game as a parameter in Game.createGame()
 * @author Mike
 */
public class GameConfiguration {
    
    private URI textureRootFolder;
    private URI soundRootFolder;
    private EngineSettings engineSettings;
    
    //===============
    // Constructor
    //===============
    
    /**
     * Constructs a new game configuration.
     * @param textureRootFolder URI to the folder where the textures are located
     * @param soundRootFolder URI to the folder where the sounds are located
     * @param engineSettings  EngineSettings object containing the engine settings
     */
    public GameConfiguration(URI textureRootFolder, URI soundRootFolder, EngineSettings engineSettings)
    {
        this.textureRootFolder = textureRootFolder;
        this.soundRootFolder = soundRootFolder;
        this.engineSettings = engineSettings;
    }
     
    
    //=============
    // Accessors
    //=============
    
    /**
     * 
     * @return The URI to the texture root folder
     */
    public URI getTextureRootFolder()
    {
        return textureRootFolder;
    }
    
    /**
     * 
     * @return The URI to the sound root folder
     */
    public URI getSoundRootFolder()
    {
        return soundRootFolder;
    }
    
    /**
     * 
     * @return the GraphicsSettings object
     */
    public EngineSettings getEngineSettings()
    {
        return this.engineSettings;
    }

}
