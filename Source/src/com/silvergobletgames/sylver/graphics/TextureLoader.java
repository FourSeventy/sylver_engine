package com.silvergobletgames.sylver.graphics;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.silvergobletgames.sylver.core.Game;
import com.sun.nio.zipfs.ZipFileSystem;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLException;

/**
 * This class manages the loading of textures from disk. It will load textures
 * and store them in memory for retreival. 
 * @author Mike
 */
public class TextureLoader 
{

    // The map of textures that have been loaded in this loader
    private HashMap<String, Texture> loadedTextures = new HashMap();

    
    //==============
    // Constructor
    //==============
    
    /**
     * Creates a texture loader instance
     */
    public TextureLoader() {
    }

    
    //===============
    // Class Methods
    //===============
    
    /**
     * Retrieves a loaded texture from the  texture map.
     * @param identifier Filename of the texture to retrieve
     * @return JOGL Texture object representing the texture
     */
    public Texture getTexture(String identifier) 
    {
        //If we have previously loaded this texture, grab it from the cache
        if (loadedTextures.get(identifier.toLowerCase()) != null)         
            return loadedTextures.get(identifier.toLowerCase());        
        else 
        {
             //log error to console
            Logger logger =Logger.getLogger(TextureLoader.class.getName());
            logger.log(Level.SEVERE, "Error getting texture: " + identifier + ":  " + Thread.currentThread().toString());
           
                
            return loadedTextures.get("texturemissing.jpg");
        }
    }
    
    /**
     * Gets the key of a loaded texture.
     * @param texture
     * @return 
     */
    public String reverseLookup(Texture texture)
    {
        for(Entry<String,Texture> entry :this.loadedTextures.entrySet())
        {
            if( entry.getValue() == texture)
                return entry.getKey();
        }
        
        return null;
    }
    
    /**
     * Loads a texture into memory.  Works for a URI pointing to a location inside a .jar, or on disk. The texture 
     * can be retrieved by the given identifier. Can load .png, .jpg, .gif, .tga files.
     * @param resourceURI URI pointing to the location of the texture
     * @param identifier  String identifier identifying the texture
     */
    public void loadTexture(URI resourceURI, String identifier) 
    {                      
        //make the loading buffer context current
        Game.getInstance().getGraphicsWindow().loadingBuffer.getContext().makeCurrent();
        
        //load the texture from the resource
        Texture tex = new Texture(0);
        try 
        {         
            //gets the filename from the path
            String resourceRef = resourceURI.toString();
                    
            //make the correct call to textureIO based on the file extension
            if(resourceRef.endsWith(".png"))           
                tex = TextureIO.newTexture(resourceURI.toURL(), false, TextureIO.PNG);           
            else if(resourceRef.endsWith(".jpg"))           
                tex = TextureIO.newTexture(resourceURI.toURL(), false, TextureIO.JPG);
            else if(resourceRef.endsWith(".gif"))
                tex = TextureIO.newTexture(resourceURI.toURL(), false, TextureIO.GIF);
            else if(resourceRef.endsWith(".tga"))
                tex = TextureIO.newTexture(resourceURI.toURL(), false, TextureIO.TGA);
            
            
            //store the texture in the map with the filename as the key
            loadedTextures.put(identifier, tex);
                
        } 
        catch (Exception e) 
        { 
            //log error to console
            Logger logger =Logger.getLogger(TextureLoader.class.getName());
            logger.log(Level.SEVERE, "Error Loading Texture: " + resourceURI.toString() + " : " + e.getMessage(),e);
        
        }
        finally
        {
            //release context
            Game.getInstance().getGraphicsWindow().loadingBuffer.getContext().release();
        }    
        
    }
    
    /**
     * Loads all textures located in the directory specified by the URI. Works for a URI pointing to a location inside a .jar, or on disk
     * @param directoryURI URI pointing to a directory of textures
     * @throws IOException 
     */
    public void loadAllTextures(URI directoryURI) throws IOException 
    {    
            
        //open url connection
        URLConnection urlConnection = directoryURI.toURL().openConnection();

        //if we are in a jar
        if(urlConnection instanceof JarURLConnection) 
        {
            
            //get the jarUrlConnection
            JarURLConnection jarUrlConnection = (JarURLConnection)urlConnection;
            jarUrlConnection.connect();
            
            //get the directory name
            String textureDirectory = jarUrlConnection.getJarEntry().getName();
            
            //iterate through the jar file
            Enumeration<JarEntry> entries = jarUrlConnection.getJarFile().entries();
            for(JarEntry jarEntry = entries.nextElement(); entries.hasMoreElements(); jarEntry = entries.nextElement())
            {
                //if the jar entry is null break
                if (jarEntry == null) 
                    break;  
                //check to see if we are in the right directory
                if(jarEntry.getName().contains(textureDirectory))
                {
                    //check to see if the file has the right extension
                    if ((jarEntry.getName().endsWith(".png") || jarEntry.getName().endsWith(".jpg")) || jarEntry.getName().endsWith(".gif") || jarEntry.getName().endsWith(".tga")) 
                    {
                        //load the texture
                        try
                        {
                           
                            String[] parts = jarEntry.getName().split("/");  
                            String filename = parts[parts.length - 1].toLowerCase();
                    
                            loadTexture(this.getClass().getClassLoader().getResource(jarEntry.getName()).toURI(),filename);   
                         
                        }
                        catch(URISyntaxException e)
                        {
                            //log error to console
                            Logger logger =Logger.getLogger(TextureLoader.class.getName());
                            logger.log(Level.SEVERE, "Error Loading All Textures: " + e.getMessage(),e);
                       
                        }
                    }
                }
            }

        } 
        else //if(urlConnection instanceof FileURLConnection) 
        {        
            //get the Path
            Path resourcePath = Paths.get(directoryURI);
            
            //walk the file tree
            Files.walkFileTree(resourcePath, new SimpleFileVisitor<Path>(){
            
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr)
                {
                    //get file name
                    String fileName = file.getFileName().toString().toLowerCase();
                    
                    //load texture
                    if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".gif") || fileName.endsWith(".tga")) 
                        loadTexture(file.toUri(),fileName); 
                    
                    return FileVisitResult.CONTINUE;
                }
            
            });
          
        }       
        
    }
 


    
    
}
