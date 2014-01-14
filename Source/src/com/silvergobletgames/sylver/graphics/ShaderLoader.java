package com.silvergobletgames.sylver.graphics;


import java.util.HashMap;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.silvergobletgames.sylver.core.Game;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * This class manages the loading of shaders from disk. It will load and compile 
 * shaders and store them in memory for retreival. 
 * @author Mike
 */
public class ShaderLoader {
    
    
    //map that holds all the shaders
    private HashMap<String,ShaderProgram> shaderMap;
    
    //=============
    // Constructor
    //=============
    
    public ShaderLoader()
    {
        shaderMap = new HashMap();
    }
    
    
    //===============
    // Class Methods
    //===============
    
    /**
     * Get loaded shader program. Specify the vertex and frag shader filenames of a loaded shader program to get
     * access to a compiled ShaderProgram
     * @param vertex name of the vertex shader
     * @param fragment name of the fragment shader
     * @return built ShaderProgram
     */
    public ShaderProgram getShaderProgram(String vertex, String fragment)
    {
        //If we have previously loaded this shader, grab it from the cache
         if (shaderMap.get(vertex + fragment) != null)
	    return shaderMap.get(vertex + fragment);
         else
         {
             //log error to console
            Logger logger =Logger.getLogger(ShaderLoader.class.getName());
            logger.log(Level.SEVERE, "Failed to get shader: " +vertex +","+fragment);
        
            
   
           throw new RuntimeException("Failed to get shader: " +vertex +","+fragment);
         }      
    }
       
    /**
     * Loads compiles and links given shader program
     * @param vertexURI vertex shader to load
     * @param fragmentURI fragment shader to load
     */
    public void loadShaderProgram(URI vertexURI, URI fragmentURI) 
    {

        //make the loading buffer context current
        Game.getInstance().getGraphicsWindow().getOffscreenLoadingBuffer().getContext().makeCurrent();
        
        //get the gl context
        GL2 gl =Game.getInstance().getGraphicsWindow().getOffscreenLoadingBuffer().getGL().getGL2();
        
     
        //build the shader code objects from the files on disk
        ShaderCode vertexCode = readShaderFromDisk(vertexURI, GL2.GL_VERTEX_SHADER);
        ShaderCode fragmentCode = readShaderFromDisk(fragmentURI, GL2.GL_FRAGMENT_SHADER);
        
        //compile the shaders 
        boolean vertexCompile = vertexCode.compile(gl);
        boolean fragCompile = fragmentCode.compile(gl);
        
        //chop names down
        String vertexName,fragName;
        vertexName = vertexURI.toString().substring(vertexURI.toString().lastIndexOf("/") + 1);
        fragName = fragmentURI.toString().substring(fragmentURI.toString().lastIndexOf("/") + 1);
        
        //compilation error handling
        if(vertexCompile == false)
            throw new RuntimeException("Vertex Shader: " + vertexName +", failed to compile");
        if(fragCompile == false)
            throw new RuntimeException("Fragment Shader: " + fragName + ", failed to compile");      
                    
        //make program
        ShaderProgram program = new ShaderProgram();
        //adds the shaders to the program
        program.add(vertexCode);
        program.add(fragmentCode);
        
        //links the program
        program.link(gl,System.err);
        //linking error handling
        if(!program.linked())
            throw new RuntimeException("Shader Program: " + vertexName + " " + fragName + "failed to link");
        
        //validates program
        program.validateProgram(gl, System.err);   
        
        //saves the program to the hashmap       
        shaderMap.put(vertexName + fragName, program);
        
        //release context
        Game.getInstance().getGraphicsWindow().getOffscreenLoadingBuffer().getContext().release();
        
    }
    
    private ShaderCode readShaderFromDisk(URI shaderURI, int type)
    {      
        //read the shader from the disk and get it into a string
         ArrayList<String> sourceCode = new ArrayList<>();
         try
         {
             //get url connection
             URLConnection urlConnection = shaderURI.toURL().openConnection();
             urlConnection.connect();
             
            InputStream in = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
         
            while (bufferedReader.ready()) {
                sourceCode.add(bufferedReader.readLine()+"\n");
            }
         }
        catch(Exception e)
        {
            //log error to console
            Logger logger =Logger.getLogger(ShaderLoader.class.getName());
            logger.log(Level.SEVERE, "Error Loading Shader From Disk: " + e.getMessage(),e);
        
        }
        
        String[] strings =sourceCode.toArray(new String[sourceCode.size()]);
        String[][] derp = new String[1][strings.length];
        
        
        for(int i = 0; i < strings.length; i ++)
        {
            derp[0][i] = strings[i];
        }

        //build the shader code object
        return new ShaderCode(type,1,new String[][]{strings});
    }
  
   
}
