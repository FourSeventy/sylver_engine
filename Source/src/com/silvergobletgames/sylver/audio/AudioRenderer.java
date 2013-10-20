
package com.silvergobletgames.sylver.audio;

import com.silvergobletgames.sylver.core.Game;
import com.silvergobletgames.sylver.graphics.TextureLoader;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryJOAL;
import paulscode.sound.libraries.LibraryJavaSound;

/**
 * The AudioRenderer is responsible for playing and managing all of the audio in your game.
 * @author Mike
 */
public class AudioRenderer {
    
    //Sound system
    private SoundSystem soundSystem; 
    
    //=============
    // Constructor
    //=============
    
    /**
     * Creates a new audio renderer instance using PaulsCode audio stuff
     */
    public AudioRenderer()
    {
         try
        {            
            SoundSystemConfig.addLibrary(LibraryJOAL.class );
            SoundSystemConfig.addLibrary(LibraryJavaSound.class );
            SoundSystemConfig.setCodec("wav", CodecWav.class );
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);           
        }
        catch( SoundSystemException e )
        {
            //log error to console
            Logger logger =Logger.getLogger(AudioRenderer.class.getName());
            logger.log(Level.SEVERE, "Sound System Config Problem: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
        }
        Class libraryType;
        boolean jSCompatible = true; // SoundSystem.libraryCompatible( LibraryJavaSound.class );// TODO - library fix
        boolean aLCompatible = SoundSystem.libraryCompatible( LibraryJOAL.class );
        
        if(aLCompatible)
           libraryType = LibraryJOAL.class; // OpenAL
        else if(jSCompatible)
           libraryType = LibraryJavaSound.class; // Java Sound
        else
           libraryType = Library.class;
        
        try
        {
           soundSystem = new SoundSystem( libraryType );
        }
        catch(SoundSystemException sse)
        {
            //log error to console
            Logger logger =Logger.getLogger(AudioRenderer.class.getName());
            logger.log(Level.SEVERE, "Sound System Initialization Error: " + sse.toString());
            logger.addHandler(new ConsoleHandler()); 
            
            return;
        }
        
        //set some default variables
        SoundSystemConfig.setDefaultRolloff(.0025f);
        SoundSystemConfig.setMasterGain(0);        
        this.soundSystem.setMasterVolume(.5f);  
        
    }
    
    //===============
    // Class Methods
    //===============
    
    
    
    /**
     * Loads all the sounds in the given directory into memory. URI can point to a directory in a .jar file or on disk.
     * @param resourceURI URI pointing to the directory to load.
     * @throws IOException 
     */
    public void loadAllSounds(URI resourceURI) throws IOException
    {       
        
        //open url connection
        URLConnection urlConnection = resourceURI.toURL().openConnection();

        //if we are in a jar
        if(urlConnection instanceof JarURLConnection) 
        {

             //get the jarUrlConnection
            JarURLConnection jarUrlConnection = (JarURLConnection)urlConnection;
            jarUrlConnection.connect();
            
            //get the directory name
            String soundDirectory = jarUrlConnection.getJarEntry().getName();
            
            //iterate through the jar file
            Enumeration<JarEntry> entries = jarUrlConnection.getJarFile().entries();
            for(JarEntry jarEntry = entries.nextElement(); entries.hasMoreElements(); jarEntry = entries.nextElement())
            {
                //if the jar entry is null break
                if (jarEntry == null) 
                    break;  
                //check to see if we are in the right directory
                if(jarEntry.getName().contains(soundDirectory))
                {
                    //check to see if the file has the right extension
                    if ((jarEntry.getName().endsWith(".wav") || jarEntry.getName().endsWith(".ogg"))) 
                    {
                                               
                        //get the sound name
                        String[] parts = jarEntry.getName().split("/");  
                        String filename = parts[parts.length - 1].toLowerCase();

                        //load the texture sound
                        soundSystem.loadSound(this.getClass().getClassLoader().getResource(jarEntry.getName()),filename); 
                                            
                    }
                }
            }
        
        } 
        else //if(urlConnection instanceof FileURLConnection) 
        {          
            //get the Path
            Path resourcePath = Paths.get(resourceURI);
            
            //walk the file tree
            Files.walkFileTree(resourcePath, new SimpleFileVisitor<Path>(){
            
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr)
                {
                    //get file name
                    String fileName = file.getFileName().toString().toLowerCase();
                    
                    //load sound
                    if (fileName.endsWith(".wav") || fileName.endsWith(".ogg")) 
                        try
                        {
                            soundSystem.loadSound(file.toUri().toURL(), fileName);
                        }
                        catch (MalformedURLException ex)
                        {
                            Logger logger =Logger.getLogger(AudioRenderer.class.getName());
                            logger.log(Level.SEVERE, null, ex);
                            logger.addHandler(new ConsoleHandler()); 
                        }
                    
                    return FileVisitResult.CONTINUE;
                }
            
            });
        
        }       
    }
    
    /**
     * Sets the listener velocity in 3d space
     * @param x x velocity
     * @param y y velocity
     * @param z z velocity
     */
    public void setListenerVelocity(float x, float y, float z)
    {
        this.soundSystem.setListenerVelocity(x, y, z);
    }
    
    /**
     * Sets the listener position in 3d space
     * @param x x position
     * @param y y position
     * @param z z position
     */
    public void setListenerPosition(float x, float y, float z)
    {
        this.soundSystem.setListenerPosition(x, y, z); 
    }
    
    /**
     * Plays the given sound
     * @param sound Sound to play
     */
    public static void playSound(Sound sound)
    {
            switch (sound.type)
            { 
                // play ambient without location
                case PlayAmbient:
                {
                    try
                    { 
                        //resolve path of the sound file
                        URI rootPath = Game.getInstance().getConfiguration().getSoundRootFolder();
                        URI relativePath = new URI(null, sound.ref, null);
                        URL resolvedPath = new URL(rootPath.toURL(), relativePath.toString());
                        
                        if(true)
                            Game.getInstance().getAudioRenderer().getSoundSystem().quickPlay(sound.priority, resolvedPath,sound.ref, false, Game.getInstance().getAudioRenderer().getSoundSystem().getListenerData().position.x, Game.getInstance().getAudioRenderer().getSoundSystem().getListenerData().position.y, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
                    
                    }
                    catch (URISyntaxException | MalformedURLException ex)
                    {
                        //log error to console
                        Logger logger =Logger.getLogger(AudioRenderer.class.getName());
                        logger.log(Level.SEVERE, "Couldnt get sound path: " + ex.toString());
                        logger.addHandler(new ConsoleHandler()); 
                    }
                   break;
                }
                //play with location, volume, and pitch
                case PlayLocation: 
                {      
                    try{
                        //resolve path of the sound file
                        URI rootPath = Game.getInstance().getConfiguration().getSoundRootFolder();
                        URI relativePath = new URI(null, sound.ref, null);
                        URL resolvedPath = new URL(rootPath.toURL(), relativePath.toString());
                        
                        if(true)
                        {
                            String name =UUID.randomUUID().toString();
                            Game.getInstance().getAudioRenderer().getSoundSystem().newSource(false, name, resolvedPath, relativePath.toString(), false, sound.x, sound.y, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
                            Game.getInstance().getAudioRenderer().getSoundSystem().setVolume(name, sound.volume);
                            Game.getInstance().getAudioRenderer().getSoundSystem().setPitch(name, sound.pitchValue);
                            Game.getInstance().getAudioRenderer().getSoundSystem().setTemporary(name, true);
                            Game.getInstance().getAudioRenderer().getSoundSystem().play(name);
                        }
                    }
                    catch (URISyntaxException| MalformedURLException ex)
                    {
                        //log error to console
                        Logger logger =Logger.getLogger(AudioRenderer.class.getName());
                        logger.log(Level.SEVERE, "Couldnt get sound path: " + ex.toString());
                        logger.addHandler(new ConsoleHandler()); 
                    }
                    
                    break;
                }
                //New bgm (no fade)
                case NewBGM:
                {
                    try{
                        //resolve path to the sound file
                        URI rootPath = Game.getInstance().getConfiguration().getSoundRootFolder();
                        URI relativePath = new URI(null, sound.ref, null);
                        URL resolvedPath = new URL(rootPath.toURL(), relativePath.toString());

                        if(true)
                            Game.getInstance().getAudioRenderer().getSoundSystem().backgroundMusic("BGM", resolvedPath,relativePath.toString(), true);
                    }
                    catch (URISyntaxException | MalformedURLException ex)
                    {
                        //log error to console
                        Logger logger =Logger.getLogger(AudioRenderer.class.getName());
                        logger.log(Level.SEVERE, "Couldnt get sound path: " + ex.toString());
                        logger.addHandler(new ConsoleHandler()); 
                    }
                    
                    break;
                }
                //BGM fadeinout
                case BGMFadeInOut:
                {
                    try{
                        URI rootPath = Game.getInstance().getConfiguration().getSoundRootFolder();
                        URI relativePath = new URI(null, sound.ref, null);
                        URL resolvedPath = new URL(rootPath.toURL(), relativePath.toString());
                    
                        if(true)
                            Game.getInstance().getAudioRenderer().getSoundSystem().fadeOutIn("BGM", resolvedPath,relativePath.toString(),(long)sound.outmillis, (long)sound.inmillis);
                    }
                    catch(URISyntaxException | MalformedURLException ex)
                    {
                        //log error to console
                        Logger logger =Logger.getLogger(AudioRenderer.class.getName());
                        logger.log(Level.SEVERE, "Couldnt get sound path: " + ex.toString());
                        logger.addHandler(new ConsoleHandler()); 
                    }
                    
                    break;
                }
                case AdjustSourceVolume:
                {
                    Game.getInstance().getAudioRenderer().getSoundSystem().setVolume(sound.ref, sound.volume);
                    break;
                }
            }
        }
    
    /**
     * Sets the master volume
     * @param volume 
     */
    public void setMasterVolume(float volume)
    {
        this.soundSystem.setMasterVolume(volume); 
    }
    
    /**
     * Cleans up any running threads or open dependencies.
     */
    public void cleanupAudioRenderer()
    {
        this.soundSystem.cleanup();
    }
    
    
    //==================
    // Private Methods
    //==================
    
    private SoundSystem getSoundSystem()
    {
        return this.soundSystem;
    }
    
    
}
