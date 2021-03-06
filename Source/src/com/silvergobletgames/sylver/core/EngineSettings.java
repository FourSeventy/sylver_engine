package com.silvergobletgames.sylver.core;

import com.silvergobletgames.sylver.util.Log;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.nativewindow.util.Dimension;

/**
 * This is a class that contains all of the settings for the engines graphics and audio pipelines. 
 * These settings are intended to be player facing settings.
 * This class is passed into the game as part of the GameConfiguration. This class comes with helper methods
 * that provides read/write functionality to an .ini file to save the settings.
 * @author Mike
 */
public class EngineSettings
{
         
    //==================
    // Graphics Settings
    //==================
    
    public boolean vSync = true;
    public boolean fullScreen = true;
    public boolean bloom = true; 
    public boolean lighting = true;
    public boolean gaussianBlur = false;
    public boolean profileRendering = false;
    public boolean profileGameLoop = false;
    public ParticleDensity particleDensity = ParticleDensity.HIGH;
    public Dimension screenResolution = new Dimension (0,0);
    public int logLevel = Log.LEVEL_INFO;
    
    public static enum ParticleDensity{
        //-WARNING
        //changing the names of these enums will break the cfg file
        UBER(1.25f), HIGH(1),MEDIUM(.75f),LOW(.25f),OFF(0);
        
        public float value;
       
        ParticleDensity(float value)
        {
            this.value = value;
        }
    }
        
 
    //================
    // Audio Settings
    //================
    
    public float masterVolume = 10;
    
    
    //===============
    // Class Methods
    //=============== 
    
    /**
     * Dump the engine settings to an .ini file with given path. Path must include the filename
     * @param filePath URI to dump the file. Must include the filename. eg. C:\folder\engineSettings.ini
     */
    public void dumpSettingsToFile(URI filePath)
    {
       
        try
        {
            //set properties
            Properties iniSaver = new Properties();
            iniSaver.setProperty("masterVolume", Float.toString(this.masterVolume));
            iniSaver.setProperty("vSync", Boolean.toString(this.vSync));
            iniSaver.setProperty("fullScreen", Boolean.toString(this.fullScreen));
            iniSaver.setProperty("bloom", Boolean.toString(this.bloom));
            iniSaver.setProperty("lighting", Boolean.toString(this.lighting));
            iniSaver.setProperty("gaussianBlur", Boolean.toString(this.gaussianBlur));
            iniSaver.setProperty("particleDensity", this.particleDensity.name());
            iniSaver.setProperty("screenResolutionWidth",Integer.toString(this.screenResolution.getWidth()));
            iniSaver.setProperty("screenResolutionHeight",Integer.toString(this.screenResolution.getHeight()));
            iniSaver.setProperty("profileRendering",Boolean.toString(this.profileRendering));
            iniSaver.setProperty("profileGameLoop",Boolean.toString(this.profileGameLoop));
            iniSaver.setProperty("logLevel",Integer.toString(this.logLevel));

            //open output stream
            OutputStream out = Files.newOutputStream(Paths.get(filePath));
            iniSaver.store(out, "Sylver Engine Settings");
        }
        catch(Exception e)
        {
             //log error to console
            Log.error( "Could not dump engineSettings.ini to file: " + e.getMessage(),e);
 
        }
    }
    
    /**
     * Load the settings in from a file.
     * @param filePath URI to .ini file that contains the settings to load
     * @return returns a constructed EngineSettings object with the settings from the file
     */
    public static EngineSettings constructFromFile(URI filePath)
    {       
        try
        {
            //open file
            InputStream inputStream = Files.newInputStream(Paths.get(filePath));
           
            //load file
            Properties iniLoader = new Properties();
            iniLoader.load(inputStream);
            
            //get values
            float masterVolume = Float.parseFloat(iniLoader.getProperty("masterVolume"));
            boolean vSync = Boolean.parseBoolean(iniLoader.getProperty("vSync"));
            boolean bloom = Boolean.parseBoolean(iniLoader.getProperty("bloom"));
            boolean lighting = Boolean.parseBoolean(iniLoader.getProperty("lighting"));
            boolean blur = Boolean.parseBoolean(iniLoader.getProperty("gaussianBlur"));
            boolean fullscreen = Boolean.parseBoolean(iniLoader.getProperty("fullScreen"));
            boolean profileRendering = Boolean.parseBoolean(iniLoader.getProperty("profileRendering"));
            boolean profileGameLoop = Boolean.parseBoolean(iniLoader.getProperty("profileGameLoop"));
            ParticleDensity density = ParticleDensity.valueOf(iniLoader.getProperty("particleDensity"));
            int xResolution = Integer.parseInt(iniLoader.getProperty("screenResolutionWidth"));
            int yResolution = Integer.parseInt(iniLoader.getProperty("screenResolutionHeight"));
            int logLevel = Integer.parseInt(iniLoader.getProperty("logLevel"));
            
            //assign settings
            EngineSettings settings = new EngineSettings();
            settings.masterVolume = masterVolume;
            settings.vSync = vSync;
            settings.bloom = bloom;
            settings.lighting = lighting;
            settings.gaussianBlur = blur;
            settings.fullScreen = fullscreen;
            settings.particleDensity = density;
            settings.screenResolution = new Dimension(xResolution,yResolution);
            settings.profileRendering = profileRendering;
            settings.profileGameLoop = profileGameLoop;
            settings.logLevel = logLevel;
            
            //return
            return settings;
        }
        catch(Exception e)
        {
            //log error to console
            Log.error( "Could not open engineSettings.ini file: " + e.getMessage(),e);
        
            
            //return a default settings object
            return new EngineSettings();
        }
        
        
    }
    
}
