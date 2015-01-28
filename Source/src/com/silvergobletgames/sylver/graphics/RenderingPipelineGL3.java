package com.silvergobletgames.sylver.graphics;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.silvergobletgames.sylver.core.*;
import com.silvergobletgames.sylver.core.Scene.Layer;
import com.silvergobletgames.sylver.core.SceneObject.CoreGroups;
import com.silvergobletgames.sylver.util.Log;
import java.awt.Point;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL3bc;
import javax.media.opengl.glu.gl2.GLUgl2;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL2ES3;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;

/**
 * Rendering pipeline for rendering a scene. This rendering pipeline requires OpenGL 3.0, and GLSL 1.3.
 * Renderes a scene with full parallax layers, lighting and bloom effects.
 */
public class RenderingPipelineGL3 
{
    //profiling queries
     private static IntBuffer query; 
     public static long blurTime; //0
     public static long lightingTime; //1
     public static long bloomTime; //2
     
    //position and coord buffers
    private static ByteBuffer texCoordBuffer = Buffers.newDirectByteBuffer(8); 
    private static FloatBuffer positionBuffer = Buffers.newDirectFloatBuffer(8);
    private static FloatBuffer lightPositionBuffer = Buffers.newDirectFloatBuffer(80);
        
        
    
    /**
     * Rendering pipeline for rendering a scene. This rendering pipeline requires OpenGL 3.0, and GLSL 1.3.
     * Renderes a scene with full parallax layers, lighting, bloom, and layer blur effects.
     * @param gl OpenGl context
     * @param viewport Viewport belonging to the scene
     * @param sceneObjectManager SceneObjectManager of the scene
     * @param sceneEffectsManager SceneEffectsManager of the scene
     * @param excluded optional parameter of layers to exclude from the rendering
     */
    public static void render(GL3bc gl,Viewport viewport,SceneObjectManager sceneObjectManager, SceneEffectsManager sceneEffectsManager, Layer... excluded)
    {         
        //=========================
        //handle profiling queries
        //=========================
        
        boolean profileRendering = Game.getInstance().getConfiguration().getEngineSettings().profileRendering;
        if( profileRendering== true)
        {
            //get last frames query numbers
            if(query != null)
            {
                LongBuffer time = LongBuffer.allocate(1);
                gl.glGetQueryObjectui64v(query.get(0), GL3bc.GL_QUERY_RESULT, time);
                blurTime = time.get(0);
                gl.glGetQueryObjectui64v(query.get(1), GL3bc.GL_QUERY_RESULT, time);
                lightingTime = time.get(0);
                gl.glGetQueryObjectui64v(query.get(2), GL3bc.GL_QUERY_RESULT, time);
                bloomTime = time.get(0);
                
                //log times
                System.err.println( "Blur Time: " + blurTime/1_000_000f + "ms");
                System.err.println( "Lighting Time: " + lightingTime/1_000_000 + "ms");
                System.err.println( "Bloom Time: " + bloomTime/1_000_000 + "ms");
            }
            //allocate queries
            if(query == null)
            {
               query = IntBuffer.allocate(4);
               gl.glGenQueries(4, query);
            }
        }
        
        //====================
        // Rendering Pipeline
        //====================
        
        //excluded layers arraylist
        ArrayList<Layer> excludedLayers = new ArrayList(Arrays.asList(excluded));
        
        //clear the framebuffer
        gl.glBindFramebuffer(GL3bc.GL_FRAMEBUFFER, 0);
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
        
        //set up projection matrix
        gl.glMatrixMode(GL3bc.GL_PROJECTION);
        gl.glLoadIdentity();
        GLUgl2 glu = new GLUgl2();
        glu.gluOrtho2D(0.0, viewport.getWidth(), 0.0, viewport.getHeight());

        //bind the framebuffer object
        gl.glBindFramebuffer(GL3bc.GL_FRAMEBUFFER, Game.getInstance().getGraphicsWindow().getFbo());    
             
        //bind backbuffer texture, and clear its color
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture(), 0);
        gl.glClearColor(0, 0, 0, 0);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
        gl.glPushAttrib(GL3bc.GL_VIEWPORT_BIT);
        //setup viewport
        gl.glViewport(0, 0, Game.getInstance().getGraphicsWindow().viewportPixelSize.x,Game.getInstance().getGraphicsWindow().viewportPixelSize.y);

        //===========================================
        //Draw layers Background through FOREGROUND2
        //===========================================

        //render background layer
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
        ArrayList<SceneObject> bg = sceneObjectManager.get(Layer.BACKGROUND);
        for (int i = 0; i < bg.size(); i++) 
            bg.get(i).draw(gl);
        

        //render layers PARRALAX5 through FOREGROUND2
        for (Layer layer: new Layer[]{Layer.PARALLAX5, Layer.PARALLAX4,Layer.PARALLAX3,Layer.PARALLAX2,Layer.PARALLAX1,Layer.ATTACHED_BG,Layer.MAIN,Layer.ATTACHED_FG,Layer.FOREGROUND1,Layer.FOREGROUND2}) 
        {   
            //skip any excluded layers
            if(excludedLayers.contains(layer))
                continue;           
            
            //build list of visible scene objects
            ArrayList<SceneObject> visibleSceneObjects = new ArrayList<>();
            for(SceneObject sceneObjectToTest: sceneObjectManager.get(layer))
            {
                if(viewport.isSceneObjectVisible(sceneObjectToTest, layer))               
                    visibleSceneObjects.add(sceneObjectToTest);              
            }
            //if we have blur enabled draw to layer buffer texture
            if(Game.getInstance().getConfiguration().getEngineSettings().gaussianBlur && layer.blurFactor != 0 && !visibleSceneObjects.isEmpty())
            {
                gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][0], 0);
                gl.glClearColor(0, 0, 0, 0);
                gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
            }
            
            //set appropriate modelview transform
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(viewport.getBottomLeftCoordinate().x * layer.coordinateScalingFactor, viewport.getBottomLeftCoordinate().y * layer.coordinateScalingFactor, 1, viewport.getBottomLeftCoordinate().x * layer.coordinateScalingFactor, viewport.getBottomLeftCoordinate().y * layer.coordinateScalingFactor, 0, 0, 1, 0);
            
            //draw the visible sceneObjects
            for(SceneObject sceneObjectToRender: visibleSceneObjects) 
            {
                sceneObjectToRender.draw(gl);  
            }

            if(profileRendering == true)
            {
                //start profiling query
                gl.glBeginQuery(GL3bc.GL_TIME_ELAPSED, query.get(0));
            }
          
            //apply the layer blur
            if(Game.getInstance().getConfiguration().getEngineSettings().gaussianBlur && layer.blurFactor != 0 && !visibleSceneObjects.isEmpty())           
                RenderingPipelineGL3.applyGaussianBlur(gl, viewport,layer.blurFactor,Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][0]);
            
            if(profileRendering == true)
            {
               //end performance query
               gl.glEndQuery(GL3bc.GL_TIME_ELAPSED); 
            }
        
        }
      

        //=================
        // Render Lighting
        //=================

        //start profiling query
        if(profileRendering == true)
        {
            gl.glBeginQuery(GL3bc.GL_TIME_ELAPSED, query.get(1));
        }
        
        if(Game.getInstance().getConfiguration().getEngineSettings().lighting)
            renderLighting(gl,sceneObjectManager,viewport,sceneEffectsManager);
        
         //end performance query
        if(profileRendering == true)
        {
             gl.glEndQuery(GL3bc.GL_TIME_ELAPSED); 
        }


        //==============
        // Render Bloom
        //==============

        //start profiling query
        if(profileRendering == true)
        {
            gl.glBeginQuery(GL3bc.GL_TIME_ELAPSED, query.get(2));
        }
        
        if (Game.getInstance().getConfiguration().getEngineSettings().bloom)        
            renderBloom(gl,sceneObjectManager,viewport,sceneEffectsManager);
        
        //end performance query
        if(profileRendering == true)
        {
            gl.glEndQuery(GL3bc.GL_TIME_ELAPSED); 
        }

        //==============
        // Finish FBO
        //==============
        
        // Restore our glEnable and glViewport states
        gl.glPopAttrib();  
         
        // Unbind our fbo so we are drawing to the default framebuffer
        gl.glBindFramebuffer(GL3bc.GL_FRAMEBUFFER, 0); 
              
        //if we did bloom, turn on the tonemapping shader
        if(Game.getInstance().getConfiguration().getEngineSettings().bloom)    
        {
            //turn on our tonemapping shader
            ShaderProgram toneMapper = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("tonemapvert.glsl", "tonemapfrag.glsl");
            gl.glUseProgram(toneMapper.program());

            //Define the lowest value that will be mapped to 1.0 on the screen.
            float bThreshold = 1f; //2.1

            //Pass threshold data to the tonemapping shader
            int bThreshVar = gl.glGetUniformLocation(toneMapper.program(), "bThresh"); //gets handle to glsl variable
            gl.glUniform1f(bThreshVar, bThreshold);
        }
        

        //bind the backbuffer texture
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture());
        
        gl.glEnable(GL3bc.GL_TEXTURE_2D);       
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glDisable(GL3bc.GL_BLEND);
        gl.glColor4f(1,1,1,1);

        //draw the backbuffer to the screne
        gl.glBegin(GL3bc.GL_QUADS);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2f(0, 0);  //bottom left
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex2f(0, viewport.getHeight());  //top left
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex2f(viewport.getWidth(), viewport.getHeight()); //top right
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex2f(viewport.getWidth(), 0);  //bottom right
        gl.glEnd();

        //cleanup
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        gl.glDisable(GL3bc.GL_TEXTURE_2D);
        gl.glUseProgram(0);


        //=====================
        //Draw World HUD Layer
        //=====================
        if(!excludedLayers.contains(Layer.WORLD_HUD))
        {
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 1, viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 0, 0, 1, 0);
            ArrayList<SceneObject> wHud = sceneObjectManager.get(Layer.WORLD_HUD);
            for (int i = 0; i < wHud.size(); i++) {
                wHud.get(i).draw(gl);
            }
        }
        

        //===============
        //Draw  HUD Layer
        //===============    
        if(!excludedLayers.contains(Layer.HUD))
        {
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
            ArrayList<SceneObject> Hud = sceneObjectManager.get(Layer.HUD);
            for (int i = 0; i < Hud.size(); i++) {
                Hud.get(i).draw(gl);
            }
        }


        //================
        //Draw Menu Layer
        //================
        if(!excludedLayers.contains(Layer.MENU))
        {
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
            ArrayList<SceneObject> menu = sceneObjectManager.get(Layer.MENU);
            for (int i = 0; i < menu.size(); i++) {
                menu.get(i).draw(gl);
            }
        }
        
        //==================
        //Draw Scene Effects
        //==================
        sceneEffectsManager.render(gl);
       
                        
    }    
    
    //========================
    // Private Helper Methods
    //========================
    
    /**
     * Private helper method that provides lighting effects to the scene
     * @param gl
     * @param sceneObjectManager
     * @param viewport 
     */
    private static void renderLighting(GL3bc gl, SceneObjectManager sceneObjectManager, Viewport viewport, SceneEffectsManager sceneEffectsManager)
    {
        //texture nicknames
        int lightAccumulationTexture = Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][0];
        int lightTexture = Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][1];
        int shadowTexture = Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][2];
        
        
        //===========================================================
        // Build list of LightSources, DarkSources and ShadowCasters
        //===========================================================
        
        //variable to know if we added something to the light accumulation texture
        boolean lightAccumulationOccupied = false;
        
        //get all the shadow casters
        ArrayList<SceneObject> entityList = sceneObjectManager.get(CoreGroups.SHADOWCASTER);
        ArrayList<ShadowCaster> allShadowCasters = new ArrayList<>();
        for(SceneObject e:entityList)
        {
            allShadowCasters.add((ShadowCaster)e);
        }
        
        //build list of light sources 
        ArrayList<SceneObject> allLightSources  = sceneObjectManager.get(CoreGroups.LIGHTSOURCE); 
        ArrayList<LightSource> visibleLightSources = new ArrayList<>();
        for(SceneObject sceneObject: allLightSources)
        {
            if(viewport.isSceneObjectVisible(sceneObject, Layer.MAIN) && ((LightSource)sceneObject).isOn())
            {
                    visibleLightSources.add((LightSource)sceneObject); 
                    lightAccumulationOccupied = true;
            }
        }
        
        //build list of dark sources 
        ArrayList<SceneObject> allDarkSources  = sceneObjectManager.get(CoreGroups.DARKSOURCE); 
        ArrayList<DarkSource> visibleDarkSources = new ArrayList<>();
        for(SceneObject sceneObject: allDarkSources)
        {
            if(viewport.isSceneObjectVisible(sceneObject, Layer.MAIN))
            {
                    visibleDarkSources.add((DarkSource)sceneObject); 
                    lightAccumulationOccupied = true;
            }
        }

            
        //bind the light accumulation texture and clear it with the ambient color.
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, lightAccumulationTexture, 0);
        gl.glClearColor(sceneEffectsManager.sceneAmbientLight.r, sceneEffectsManager.sceneAmbientLight.g, sceneEffectsManager.sceneAmbientLight.b, 1f);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);

        //build a glu
        GLUgl2 glu = new GLUgl2();


        //================================
        // Draw each light and its shadows        
        //================================
        for (LightSource light : visibleLightSources) 
        {

            //=============
            // Draw Light
            //=============

            //bind the light texture to the FBO (anything we draw will now draw to this texture)
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, lightTexture, 0);
            gl.glClearColor(0, 0, 0, 1);
            gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);

            //matrix transform
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();              
            glu.gluLookAt(viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 1, viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 0, 0, 1, 0);

            //correct blending mode
            gl.glDisable(GL3bc.GL_BLEND);

            //gets the light shader 
            ShaderProgram shaderProgram = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("lightsourcevert.glsl", "lightsourcefrag.glsl");

            //starts running the shader program
            gl.glUseProgram(shaderProgram.program());

            //sets the light color for the glsl shader
            int lightColorUniformLocation = gl.glGetUniformLocation(shaderProgram.program(), "u_lightColor"); //gets handle to glsl variable
            gl.glUniform4f(lightColorUniformLocation,light.getColor().r,light.getColor().g,light.getColor().b,light.getColor().a);  //passes color to that variable

            //sets the light range for the glsl shader
            int lightRangeUniform = gl.glGetUniformLocation(shaderProgram.program(), "u_lightRange"); 
            gl.glUniform1f(lightRangeUniform, light.getSize());

            //sets the position of the light for the glsl shader
            int lightPositionUniform = gl.glGetUniformLocation(shaderProgram.program(), "u_lightPos"); 
            gl.glUniform2f(lightPositionUniform, light.getPosition().x, light.getPosition().y );

            //sets the intensity of the light for the glsl shader
            int lightIntensityUniform = gl.glGetUniformLocation(shaderProgram.program(), "u_lightIntensity");
            gl.glUniform1f(lightIntensityUniform, light.getIntensity());

            //build vertex data 
            lightPositionBuffer.clear();

            //add first vertex data
            lightPositionBuffer.put(new float[]{light.getPosition().x,light.getPosition().y});  

            //calculate the rest of the vertex positions
            int numSubdivisions = 32;
            for (float angle = light.direction - light.conicalRadius / 2; angle <= light.direction + light.conicalRadius / 2; angle += (light.conicalRadius / numSubdivisions)) 
            {
                //correct for overshoot problem
                if (angle + (light.conicalRadius / numSubdivisions) > light.direction + light.conicalRadius / 2) {
                    angle = light.direction + light.conicalRadius / 2;
                }

                //add vertex
                lightPositionBuffer.put(new float[]{light.getSize() * (float) Math.cos(angle) + light.getPosition().x,light.getSize() * (float) Math.sin(angle) + light.getPosition().y});
            }
            lightPositionBuffer.rewind();

            //enables vertex array data
            gl.glEnableClientState(GL3bc.GL_VERTEX_ARRAY);

            //gets the location of the position attribute
            int positionAttributeLocation = gl.glGetAttribLocation(shaderProgram.program(), "a_position");

            //enables the use of the attributes a_position
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, lightPositionBuffer);

            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_TRIANGLE_FAN, 0, numSubdivisions+1);

            //stops the shader program
            gl.glUseProgram(0);
            gl.glDisableClientState(GL3bc.GL_VERTEX_ARRAY); 


            //==============
            // Draw shadows
            //==============
            
            //bind the shadow accumulation texture and clear it with 0,0,0
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, shadowTexture, 0);
            gl.glClearColor(100,100,100, 1f);
            gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);

            //get all casters that are in range of the light
            ArrayList<ShadowCaster> castersInRange = new ArrayList<>();
            for (ShadowCaster shadowCaster : allShadowCasters)
            {
                if (Math.abs(Point.distance(shadowCaster.getPosition().x, shadowCaster.getPosition().y, light.getPosition().getX(), light.getPosition().getY())) < light.getSize() * 2) 
                {
                    castersInRange.add(shadowCaster);
                }
            }

            //for each caster in range, find the back facing edges and draw the shadow
            for (ShadowCaster shadowCaster : castersInRange)
            {
                //find the corners of the entity
                SylverVector2f[] corners;
                corners = shadowCaster.getCorners();

                //mark sides as front facing or back facing               
                boolean[] sides = new boolean[corners.length];       //boolean s12;  boolean s23;  boolean s34;  boolean s41;          
                for (int i = 0; i < corners.length; i++)
                {
                    //get vector from corner to light
                    SylverVector2f toLight = new SylverVector2f(light.getPosition().x - corners[i].getX(), light.getPosition().y - corners[i].getY());

                    //get normal vector of side
                    int next = (i == corners.length - 1) ? 0 : i + 1;//handle wrap around
                    float dx = corners[next].getX() - corners[i].getX();
                    float dy = corners[next].getY() - corners[i].getY();

                    SylverVector2f sideNormal = new SylverVector2f(dy, -dx);


                    //dot vectors, mark corresponding side as back facing or not
                    if (toLight.dot(sideNormal) > 0)                       
                        sides[i] = false; //front facing

                    else                      
                        sides[i] = true; //back facing  

                }

                //draw shadows for all back facing sides
                for (int i = 0; i <= corners.length -1; i++) 
                {
                    //if the side is back facing
                    if(sides[i] == true) 
                    {
                        //========= get our two shadow vectors =================
                        
                        //get both shadow vectors
                        SylverVector2f corner1 = corners[i];
                        SylverVector2f shadowVector1 = new SylverVector2f(corner1.getX() - light.getPosition().x, corner1.getY() - light.getPosition().y);
                        shadowVector1.normalise();
                        SylverVector2f shadowPoint1A = new SylverVector2f(corner1.getX(),corner1.getY());
                        SylverVector2f shadowPoint1B = new SylverVector2f(corner1.getX() + shadowVector1.x * light.getSize(), corner1.getY() + shadowVector1.y * light.getSize());
                        
                        SylverVector2f corner2 = corners[(i + 1)%corners.length];
                        SylverVector2f shadowVector2 = new SylverVector2f(corner2.getX() - light.getPosition().x, corner2.getY() - light.getPosition().y);
                        shadowVector2.normalise();
                        SylverVector2f shadowPoint2A = new SylverVector2f(corner2.getX(),corner2.getY());
                        SylverVector2f shadowPoint2B = new SylverVector2f(corner2.getX() + shadowVector2.x * light.getSize(), corner2.getY() + shadowVector2.y * light.getSize());
                                                               
                        // =========== draw the shadow =================
                        
                        //correct blending mode
                        gl.glDisable(GL3bc.GL_BLEND);

                        gl.glDisable(GL3bc.GL_TEXTURE_2D);
                        gl.glColor4f(0, 0, 0, 1f);
                        gl.glShadeModel(GL3bc.GL_FLAT);
                        gl.glBegin(GL3bc.GL_TRIANGLE_STRIP);

                        //draw first vector verticies for the triangle strip               
                        gl.glVertex2f(shadowPoint1A.x, shadowPoint1A.y);
                        gl.glVertex2f(shadowPoint1B.x, shadowPoint1B.y);

                        //draw second vector verticies for the triangle strip                   
                        gl.glVertex2f(shadowPoint2A.x, shadowPoint2A.y);
                        gl.glVertex2f(shadowPoint2B.x, shadowPoint2B.y);

                        gl.glEnd();
                        gl.glColor4f(1, 1, 1, 1);
                        
                    }
                }
                
                
                //====================
                // Fix Self Shadowing
                //====================
                
                //build tesselator for polygon shape
                GLUtessellator tesselator = GLUgl2.gluNewTess();
                TessCallBack callback = new TessCallBack(gl,glu);
                GLUgl2.gluTessCallback(tesselator, GLUgl2.GLU_TESS_VERTEX, callback);// glVertex3dv);
                GLUgl2.gluTessCallback(tesselator, GLUgl2.GLU_TESS_BEGIN, callback);// beginCallback);
                GLUgl2.gluTessCallback(tesselator, GLUgl2.GLU_TESS_END, callback);// endCallback);
                GLUgl2.gluTessCallback(tesselator, GLUgl2.GLU_TESS_ERROR, callback);// errorCallback);
    
                int list =gl.glGenLists(1);
                
                gl.glNewList(list, GL3bc.GL_COMPILE);
                    gl.glShadeModel(GL3bc.GL_FLAT);
                    GLUgl2.gluTessBeginPolygon(tesselator, null);
                    GLUgl2.gluTessBeginContour(tesselator);
                    for (int i = 0; i <= corners.length +1; i++) 
                    {
                        SylverVector2f point = corners[(i + 1)%corners.length];
                        double[] rect= {point.getX(), point.getY(),0};
                        GLUgl2.gluTessVertex(tesselator, rect, 0, rect);
                    }
                    GLUgl2.gluTessEndContour(tesselator);
                    GLUgl2.gluTessEndPolygon(tesselator);
                gl.glEndList();
                GLUgl2.gluDeleteTess(tesselator);
                
                //draw dark over the shape
                //correct blending mode
                gl.glDisable(GL3bc.GL_BLEND);
            
                gl.glDisable(GL3bc.GL_TEXTURE_2D);
                gl.glColor4f(100, 100, 100, 1f);
                gl.glCallList(list);
                gl.glColor4f(1, 1, 1, 1f);
                
                      
                //cleans up lists
                gl.glDeleteLists(list, 1);
                gl.glShadeModel(GL3bc.GL_SMOOTH);

                
                
                
                
            }
            
            
                //====draw shadow texture to light texture====
                //Switch to the accumulation texture
                gl.glEnable(GL3bc.GL_TEXTURE_2D);
                gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, lightTexture, 0);
                gl.glMatrixMode(GL3bc.GL_MODELVIEW);
                gl.glLoadIdentity();

                //Blend mode
                gl.glEnable(GL3bc.GL_BLEND);
                gl.glBlendEquation(GL2ES3.GL_MIN);  
                gl.glBlendFunc(GL3bc.GL_ONE, GL3bc.GL_ONE);  

                //Bind the light texture
                gl.glBindTexture(GL3bc.GL_TEXTURE_2D, shadowTexture);

                //Draw the light texture to the accum. with additive blending
                gl.glBegin(GL3bc.GL_QUADS);
                    gl.glTexCoord2d(0.0, 0.0);
                    gl.glVertex2f(0, 0);  //bottom left
                    gl.glTexCoord2d(0.0, 1.0);
                    gl.glVertex2f(0, viewport.getHeight());  //top left
                    gl.glTexCoord2d(1.0, 1.0);
                    gl.glVertex2f(viewport.getWidth(), viewport.getHeight()); //top right
                    gl.glTexCoord2d(1.0, 0.0);
                    gl.glVertex2f(viewport.getWidth(), 0);  //bottom right
                gl.glEnd();

                gl.glDisable(GL3bc.GL_BLEND);
                gl.glDisable(GL3bc.GL_TEXTURE_2D);

            //Switch to the accumulation texture
            gl.glEnable(GL3bc.GL_TEXTURE_2D);
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, lightAccumulationTexture, 0);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();

            //Blend mode
            gl.glEnable(GL3bc.GL_BLEND);
            gl.glBlendEquation(GL3bc.GL_FUNC_ADD); 
            gl.glBlendFunc(GL3bc.GL_ONE, GL3bc.GL_ONE);

            //Bind the light texture
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, lightTexture);

            //Draw the light texture to the accum. with additive blending
            gl.glBegin(GL3bc.GL_QUADS);
                gl.glTexCoord2d(0.0, 0.0);
                gl.glVertex2f(0, 0);  //bottom left
                gl.glTexCoord2d(0.0, 1.0);
                gl.glVertex2f(0, viewport.getHeight());  //top left
                gl.glTexCoord2d(1.0, 1.0);
                gl.glVertex2f(viewport.getWidth(), viewport.getHeight()); //top right
                gl.glTexCoord2d(1.0, 0.0);
                gl.glVertex2f(viewport.getWidth(), 0);  //bottom right
            gl.glEnd();

            gl.glDisable(GL3bc.GL_BLEND);
            gl.glDisable(GL3bc.GL_TEXTURE_2D);

        } //=============end foreach lightsource============


        //=================
        //Draw DarkSources
        //=================
        if(!visibleDarkSources.isEmpty())
        {
            //Switch to the accumulation texture
            gl.glEnable(GL3bc.GL_TEXTURE_2D);
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, lightAccumulationTexture, 0);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();

            //matrix transform
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();              
            glu.gluLookAt(viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 1, viewport.getBottomLeftCoordinate().x, viewport.getBottomLeftCoordinate().y, 0, 0, 1, 0);

            //Blend mode
            gl.glEnable(GL3bc.GL_BLEND);
            gl.glBlendEquation(GL3bc.GL_FUNC_REVERSE_SUBTRACT);  
            gl.glBlendFunc(GL3bc.GL_ONE, GL3bc.GL_ONE);           
        }
        
        for(DarkSource darkSource: visibleDarkSources)
        {           
            //Bind the darkness texture
            darkSource.getTexture().bind(gl);     
            float intensity = darkSource.getIntensity();
            gl.glColor4f(intensity,intensity,intensity,intensity);

            //Draw the light texture to the accum. with additive blending
            gl.glBegin(GL3bc.GL_QUADS);
                gl.glTexCoord2d(0.0, 0.0);
                gl.glVertex2f(darkSource.getPosition().x, darkSource.getPosition().y);  //bottom left
                gl.glTexCoord2d(0.0, 1.0);
                gl.glVertex2f(darkSource.getPosition().x, darkSource.getPosition().y + darkSource.getHeight());  //top left
                gl.glTexCoord2d(1.0, 1.0);
                gl.glVertex2f(darkSource.getPosition().x + darkSource.getWidth(), darkSource.getPosition().y + darkSource.getHeight()); //top right
                gl.glTexCoord2d(1.0, 0.0);
                gl.glVertex2f(darkSource.getPosition().x + darkSource.getWidth(), darkSource.getPosition().y );  //bottom right
            gl.glEnd();
                       
        }
        //cleanup
        gl.glColor4f(1,1,1,1);
        gl.glBlendEquation(GL3bc.GL_FUNC_ADD);  
        gl.glDisable(GL3bc.GL_BLEND);
        gl.glDisable(GL3bc.GL_TEXTURE_2D);
        

        //===================================================
        // Render Lightmap to Backbuffer With Gaussian Blur
        //===================================================
         if(lightAccumulationOccupied == true )
         {
             //switch so we are drawing to the downsample texture
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0], 0);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glClearColor(0, 0, 0, 0f);
            gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
            gl.glDisable(GL3bc.GL_BLEND);
            //gl.glColor4f(1,1,1,1);

            //bind the input texture
            gl.glEnable(GL3bc.GL_TEXTURE_2D);  
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, lightAccumulationTexture);

            //draw the fbotexture to the downsample texture
            gl.glBegin(GL3bc.GL_QUADS);
                gl.glTexCoord2d(0.0, 0.0);
                gl.glVertex2f(0, 0);  //bottom left
                gl.glTexCoord2d(0.0, 1.0);
                gl.glVertex2f(0, viewport.getHeight()/2);  //top left
                gl.glTexCoord2d(1.0, 1.0);
                gl.glVertex2f(viewport.getWidth()/2, viewport.getHeight()/2); //top right
                gl.glTexCoord2d(1.0, 0.0);
                gl.glVertex2f(viewport.getWidth()/2, 0);  //bottom right
            gl.glEnd();
            
            positionBuffer.clear();
            positionBuffer.put(new float[]{0,0}); //x, y,
            positionBuffer.put(new float[]{0,viewport.getHeight()/2});
            positionBuffer.put(new float[]{viewport.getWidth()/2,viewport.getHeight()/2});
            positionBuffer.put(new float[]{viewport.getWidth()/2,0});
            positionBuffer.rewind();

            //creates vertex data array for texCoords
            texCoordBuffer.clear();
            texCoordBuffer.put(new byte[]{(byte)0,(byte)0});
            texCoordBuffer.put(new byte[]{(byte)0,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)0});
            texCoordBuffer.rewind();

            // =============================== Horizontal Pass ================================
            
            //draw to gaussian blur vertical
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D,Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][1] , 0); 
            gl.glClearColor(0, 0, 0, 1);
            gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
            gl.glEnable(GL3bc.GL_TEXTURE_2D);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();   
            
            //Draw the first texture to the second using a horizontal blur shader.
            ShaderProgram blur = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("fastGaussianVert.glsl", "fastGaussianFrag.glsl");
            gl.glUseProgram(blur.program());
            
            //gets the location of the position and texCoord attributes
            int texCoordAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_texCoord0");
            int positionAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer);

            //send render size uniform to shader
            int uniformSizeLocation = gl.glGetUniformLocation(blur.program(), "u_size"); //gets handle to glsl variable
            gl.glUniform2f(uniformSizeLocation, viewport.getWidth()/2,viewport.getHeight()/2);
            
            //send render direction uniform to shader
            int uniformDirectionLocation = gl.glGetUniformLocation(blur.program(), "u_dir"); //gets handle to glsl variable
            gl.glUniform2f(uniformDirectionLocation, 1f,0f);

            //bind texture to draw from
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0]);
            
            //sets the texture sampler
            gl.glUniform1i(gl.glGetUniformLocation(blur.program(), "s_texture"), 0);

            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

            //cleanup
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
            gl.glUseProgram(0);

            //==========================Vertical ====================================

            positionBuffer.clear();
            positionBuffer.put(new float[]{0,0}); //x, y,
            positionBuffer.put(new float[]{0,viewport.getHeight()});
            positionBuffer.put(new float[]{viewport.getWidth(),viewport.getHeight()});
            positionBuffer.put(new float[]{viewport.getWidth(),0});
            positionBuffer.rewind();

            //creates vertex data array for texCoords
            texCoordBuffer.clear();
            texCoordBuffer.put(new byte[]{(byte)0,(byte)0});
            texCoordBuffer.put(new byte[]{(byte)0,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)0});
            texCoordBuffer.rewind();
            //Attach the backbuffer
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture(), 0);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glEnable(GL3bc.GL_BLEND);
            gl.glBlendEquation(GL3bc.GL_FUNC_ADD);
            gl.glBlendFunc(GL3bc.GL_DST_COLOR, GL3bc.GL_ZERO);
            
            //Draw the second texture to the first using a vertical blur shader.
            blur = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("fastGaussianVert.glsl", "fastGaussianFrag.glsl");
            gl.glUseProgram(blur.program());
            
            //gets the location of the position and texCoord attributes
            texCoordAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_texCoord0");
            positionAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer);
            
            //send render size uniform to shader
            uniformSizeLocation = gl.glGetUniformLocation(blur.program(), "u_size"); //gets handle to glsl variable
            gl.glUniform2f(uniformSizeLocation, viewport.getWidth()/2,viewport.getHeight()/2);
            
            //send render direction uniform to shader
            uniformDirectionLocation = gl.glGetUniformLocation(blur.program(), "u_dir"); //gets handle to glsl variable
            gl.glUniform2f(uniformDirectionLocation, 0,1);

            //bind texture to draw from
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][1]);
            
            //sets the texture sampler
            gl.glUniform1i(gl.glGetUniformLocation(blur.program(), "s_texture"), 0);

            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

            //cleanups
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
            gl.glUseProgram(0);
         }
         else//render without blur
         {
            gl.glEnable(GL3bc.GL_TEXTURE_2D);
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture(), 0);
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glEnable(GL3bc.GL_BLEND);
            gl.glBlendEquation(GL3bc.GL_FUNC_ADD);
            gl.glBlendFunc(GL3bc.GL_DST_COLOR, GL3bc.GL_ZERO);

            //binding the light accumulation texture
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D,lightAccumulationTexture);

            gl.glBegin(GL3bc.GL_QUADS);
                gl.glTexCoord2d(0.0, 0.0);
                gl.glVertex2f(0, 0);  //bottom left
                gl.glTexCoord2d(0.0, 1.0);
                gl.glVertex2f(0, viewport.getHeight());  //top left
                gl.glTexCoord2d(1.0, 1.0);
                gl.glVertex2f(viewport.getWidth(), viewport.getHeight()); //top right
                gl.glTexCoord2d(1.0, 0.0);
                gl.glVertex2f(viewport.getWidth(), 0);  //bottom right
            gl.glEnd();
         }
        
        
    }
    
    /**
     * Private helper method that provides bloom post processing for the scene.
     * @param gl
     * @param sceneObjectManager
     * @param viewport 
     */
    private static void renderBloom(GL3bc gl, SceneObjectManager sceneObjectManager, Viewport viewport, SceneEffectsManager sceneEffectsManager)
    {
        
        
        gl.glEnable(GL3bc.GL_TEXTURE_2D);
        gl.glDisable(GL3bc.GL_BLEND);

        //For each bloom level:
        for (int i = 0; i < Game.getInstance().getGraphicsWindow().getFboTextureArray().length ; i++) 
        {
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            
            //Attach the current bloom level's first texture to the FBO.
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i][0], 0);

            int tx = (int) viewport.getWidth() / (int) (Math.pow(2, i));
            int ty = (int) viewport.getHeight() / (int) (Math.pow(2, i));

            //If it's the first bloom level: draw the back buffer to the bloom texture using the brightness reducer shader.
            if (i == 0) 
            {
                ShaderProgram darkener = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("darkenervert.glsl", "darkenerfrag.glsl");
                gl.glUseProgram(darkener.program());

                //Colors below this threshold will not be used when calculating bloom.
                float bloomThreshold = 1;
                
                //set threshold var in shader
                int thresholdVar = gl.glGetUniformLocation(darkener.program(), "u_threshold"); //gets handle to glsl variable
                gl.glUniform1f(thresholdVar, bloomThreshold);

                //bind the backbuffer texture
                gl.glActiveTexture(GL3bc.GL_TEXTURE0);
                gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture());
                
                //sets the texture sampler
                gl.glUniform1i(gl.glGetUniformLocation(darkener.program(), "s_texture"), 0);

                gl.glBegin(GL3bc.GL_QUADS);
                    gl.glTexCoord2d(0.0, 0.0);
                    gl.glVertex2f(0, 0);  //bottom left
                    gl.glTexCoord2d(0.0, 1.0);
                    gl.glVertex2f(0, viewport.getHeight());  //top left
                    gl.glTexCoord2d(1.0, 1.0);
                    gl.glVertex2f(viewport.getWidth(), viewport.getHeight()); //top right
                    gl.glTexCoord2d(1.0, 0.0);
                    gl.glVertex2f(viewport.getWidth(), 0);  //bottom right
                gl.glEnd();

                gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
                gl.glUseProgram(0);
            } 
            else //Downsample the previous bloom level.
            {
                gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i - 1][0]);

                gl.glBegin(GL3bc.GL_QUADS);
                    gl.glTexCoord2d(0.0, 0.0);
                    gl.glVertex2f(0, 0);  //bottom left
                    gl.glTexCoord2d(0.0, 1.0);
                    gl.glVertex2f(0, ty);  //top left
                    gl.glTexCoord2d(1.0, 1.0);
                    gl.glVertex2f(tx, ty); //top right
                    gl.glTexCoord2d(1.0, 0.0);
                    gl.glVertex2f(tx, 0);  //bottom right
                gl.glEnd();

                gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
            }

            //===================
            //For each blur pass
            //===================
            
            //========================== HORIZONTAL ==============================
            //creates vertex data array for position
            positionBuffer.clear();
            positionBuffer.put(new float[]{0,0}); //x, y,
            positionBuffer.put(new float[]{0,ty});
            positionBuffer.put(new float[]{tx,ty});
            positionBuffer.put(new float[]{tx,0});
            positionBuffer.rewind();

            //creates vertex data array for texCoords
            texCoordBuffer.clear();
            texCoordBuffer.put(new byte[]{(byte)0,(byte)0});
            texCoordBuffer.put(new byte[]{(byte)0,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)1});
            texCoordBuffer.put(new byte[]{(byte)1,(byte)0});
            texCoordBuffer.rewind();

            //Attach the second texture to the FBO.
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i][1], 0);
            
            //Draw the first texture to the second using a horizontal blur shader.
            ShaderProgram blur = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("fastGaussianVert.glsl", "fastGaussianFrag.glsl");
            gl.glUseProgram(blur.program());
            
            //gets the location of the position and texCoord attributes
            int texCoordAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_texCoord0");
            int positionAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer);

            //send render size uniform to shader
            int uniformSizeLocation = gl.glGetUniformLocation(blur.program(), "u_size"); //gets handle to glsl variable
            gl.glUniform2f(uniformSizeLocation, tx,ty);
            
            //send render direction uniform to shader
            int uniformDirectionLocation = gl.glGetUniformLocation(blur.program(), "u_dir"); //gets handle to glsl variable
            gl.glUniform2f(uniformDirectionLocation, 1f,0f);

            //bind texture
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i][0]);
            
            //sets the texture sampler
            gl.glUniform1i(gl.glGetUniformLocation(blur.program(), "s_texture"), 0);

            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

            //cleanup
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
            gl.glUseProgram(0);
            
            //==========================VERTICAL ====================================

            //Attach the first texture to the FBO.
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i][0], 0);
            
            //Draw the second texture to the first using a vertical blur shader.
            blur = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("fastGaussianVert.glsl", "fastGaussianFrag.glsl");
            gl.glUseProgram(blur.program());
            
            //gets the location of the position and texCoord attributes
            texCoordAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_texCoord0");
            positionAttributeLocation = gl.glGetAttribLocation(blur.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer);
            
            //send render size uniform to shader
            uniformSizeLocation = gl.glGetUniformLocation(blur.program(), "u_size"); //gets handle to glsl variable
            gl.glUniform2f(uniformSizeLocation, tx,ty);
            
            //send render direction uniform to shader
            uniformDirectionLocation = gl.glGetUniformLocation(blur.program(), "u_dir"); //gets handle to glsl variable
            gl.glUniform2f(uniformDirectionLocation, 0,1);

            //bind texture
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[i][1]);
            
            //sets the texture sampler
            gl.glUniform1i(gl.glGetUniformLocation(blur.program(), "s_texture"), 0);

            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

            //cleanups
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
            gl.glUseProgram(0);
        }
            //==============================================================
            // Draw all bloom levels to the backbuffer
            // with additive blending using a single pass multitexturing shader.
            //=========================================================
        
              
            gl.glMatrixMode(GL3bc.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture(), 0);
            gl.glEnable(GL3bc.GL_BLEND);
            gl.glBlendFunc(GL3bc.GL_ONE, GL3bc.GL_ONE);
            
            //bind the shader
            ShaderProgram multiTex = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("bloommultivert.glsl", "bloommultifrag.glsl");
            gl.glUseProgram(multiTex.program());
            
            //creates vertex data array for texCoords
            texCoordBuffer.clear();
            texCoordBuffer.put(new byte[]{0,0});
            texCoordBuffer.put(new byte[]{0,1});
            texCoordBuffer.put(new byte[]{1,1});
            texCoordBuffer.put(new byte[]{1,0});
            texCoordBuffer.rewind();
            
            //creates vertex data array for position
            positionBuffer.clear();
            positionBuffer.put(new float[]{0,0}); //x, y,
            positionBuffer.put(new float[]{0, viewport.getHeight()});
            positionBuffer.put(new float[]{viewport.getWidth(), viewport.getHeight()});
            positionBuffer.put(new float[]{viewport.getWidth(), 0});
            positionBuffer.rewind();
            
            //enables vertex array data
            gl.glEnableClientState(GL3bc.GL_VERTEX_ARRAY);

            //gets the location of the position and texCoord attributes
            int texCoordAttributeLocation = gl.glGetAttribLocation(multiTex.program(), "a_texCoord");
            int positionAttributeLocation = gl.glGetAttribLocation(multiTex.program(), "a_position");

            //enables the use of the attributes a_color, and a_position
            gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
            gl.glEnableVertexAttribArray(positionAttributeLocation); 

            //points the attributes to their corresponding data buffers
            gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
            gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer); 

            //binds the textures
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[0][0]);
            gl.glActiveTexture(GL3bc.GL_TEXTURE1);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D,Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0]);             
            gl.glActiveTexture(GL3bc.GL_TEXTURE2);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D,Game.getInstance().getGraphicsWindow().getFboTextureArray()[2][0]);
            gl.glActiveTexture(GL3bc.GL_TEXTURE3);
            gl.glBindTexture(GL3bc.GL_TEXTURE_2D,Game.getInstance().getGraphicsWindow().getFboTextureArray()[3][0]);
            
            gl.glActiveTexture(GL3bc.GL_TEXTURE0);//have to do this for some horse shit reason

            //sets the sampler uniforms in the shader
            gl.glUniform1i(gl.glGetUniformLocation(multiTex.program(), "s_texture0"), 0); //first texture sampler
            gl.glUniform1i(gl.glGetUniformLocation(multiTex.program(), "s_texture1"), 1); //second texture sampler.
            gl.glUniform1i(gl.glGetUniformLocation(multiTex.program(), "s_texture2"), 2); //third texture sampler.
            gl.glUniform1i(gl.glGetUniformLocation(multiTex.program(), "s_texture3"), 3); //fourth texture sampler
            
            //draws the primitive
            gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

            //cleanup
            gl.glUseProgram(0);
            gl.glDisableClientState(GL3bc.GL_VERTEX_ARRAY);    
    }
    
    /**
     * Private helper method that takes the input texture, downsamples it, blurs it, then draws it to the backbuffer 
     * @param gl gl context
     * @param viewport viewport
     * @param blurStrength blur strength wanted
     * @param inputTexture input texture
     */
    private static void applyGaussianBlur(GL3bc gl, Viewport viewport, float blurStrength, int inputTexture)
    {
        
              
        //==============================
        //Draw to the DownsampleTexture
        //==============================
        
        //switch so we are drawing to the downsample texture
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0], 0);
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClearColor(0, 0, 0, 0f);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
        gl.glDisable(GL3bc.GL_BLEND);
        //gl.glColor4f(1,1,1,1);
        
        //bind the input texture
        gl.glEnable(GL3bc.GL_TEXTURE_2D);  
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, inputTexture);
        
        //draw the fbotexture to the downsample texture
        gl.glBegin(GL3bc.GL_QUADS);
            gl.glTexCoord2d(0.0, 0.0);
            gl.glVertex2f(0, 0);  //bottom left
            gl.glTexCoord2d(0.0, 1.0);
            gl.glVertex2f(0, viewport.getHeight()/2);  //top left
            gl.glTexCoord2d(1.0, 1.0);
            gl.glVertex2f(viewport.getWidth()/2, viewport.getHeight()/2); //top right
            gl.glTexCoord2d(1.0, 0.0);
            gl.glVertex2f(viewport.getWidth()/2, 0);  //bottom right
        gl.glEnd();


        //======================
        // Vertical Blur Pass
        //======================
        
        //switch so we are drawing to the vertical blur texture
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][1], 0);
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClearColor(0, 0, 0, 0f);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);
        
        //attach the vertical shader
        ShaderProgram gBlur =Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("gaussianVert.glsl", "gaussianFragV.glsl");
        gl.glUseProgram(gBlur.program());

        //set the sigma value for the glsl shader
        int sigma = gl.glGetUniformLocation(gBlur.program(), "sigma"); //gets handle to glsl variable               
        gl.glUniform1f(sigma, blurStrength);

        //set the blur size for the glsl shader
        int blurSize = gl.glGetUniformLocation(gBlur.program(), "blurSize"); //gets handle to glsl variable               
        gl.glUniform1f(blurSize, 1f/((float)viewport.getHeight()/2));

        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0]);

        gl.glBegin(GL3bc.GL_QUADS);
            gl.glTexCoord2d(0.0, 0.0);
            gl.glVertex2f(0, 0);  //bottom left
            gl.glTexCoord2d(0.0, 1.0);
            gl.glVertex2f(0, viewport.getHeight()/2);  //top left
            gl.glTexCoord2d(1.0, 1.0);
            gl.glVertex2f(viewport.getWidth()/2, viewport.getHeight()/2); //top right
            gl.glTexCoord2d(1.0, 0.0);
            gl.glVertex2f(viewport.getWidth()/2, 0);  //bottom right
        gl.glEnd();          

        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        gl.glUseProgram(0);

        
        //==================
        // Horizontal Pass
        //==================

        //switch so we draw to the the horizontal texture
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0], 0);
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClearColor(0, 0, 0, 0f);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);

        //attach the horizontal shader
        gBlur = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("gaussianVert.glsl", "gaussianFragH.glsl");
        gl.glUseProgram(gBlur.program());

        //set the sigma value for the glsl shader
        sigma = gl.glGetUniformLocation(gBlur.program(), "sigma"); //gets handle to glsl variable               
        gl.glUniform1f(sigma, blurStrength);

        blurSize = gl.glGetUniformLocation(gBlur.program(), "blurSize"); //gets handle to glsl variable               
        gl.glUniform1f(blurSize, 1f/((float)viewport.getWidth()/2));

        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][1]);

        gl.glBegin(GL3bc.GL_QUADS);
            gl.glTexCoord2d(0.0, 0.0);
            gl.glVertex2f(0, 0);  //bottom left
            gl.glTexCoord2d(0.0, 1.0);
            gl.glVertex2f(0, viewport.getHeight()/2);  //top left
            gl.glTexCoord2d(1.0, 1.0);
            gl.glVertex2f(viewport.getWidth()/2, viewport.getHeight()/2); //top right
            gl.glTexCoord2d(1.0, 0.0);
            gl.glVertex2f(viewport.getWidth()/2, 0);  //bottom right
        gl.glEnd();                  

        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        gl.glUseProgram(0);
        
        //============================
        // Upsample back to the backbuffer
        //============================

        //switch so we draw to the the backbuffer texture
        gl.glFramebufferTexture2D(GL3bc.GL_FRAMEBUFFER, GL3bc.GL_COLOR_ATTACHMENT0, GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getBackbufferTexture(), 0);
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GL3bc.GL_BLEND);
        gl.glBlendFunc(GL3bc.GL_SRC_ALPHA, GL3bc.GL_ONE_MINUS_SRC_ALPHA); 

        //creates vertex data array for position
        positionBuffer.clear();
        positionBuffer.put(new float[]{0,0}); //x, y,
        positionBuffer.put(new float[]{viewport.getWidth(),0});
        positionBuffer.put(new float[]{viewport.getWidth(),viewport.getHeight()});
        positionBuffer.put(new float[]{0,viewport.getHeight()});
        positionBuffer.rewind();

        //creates vertex data array for texCoords
        texCoordBuffer.clear();
        texCoordBuffer.put(new byte[]{(byte)0,(byte)0});
        texCoordBuffer.put(new byte[]{(byte)1,(byte)0});
        texCoordBuffer.put(new byte[]{(byte)1,(byte)1});
        texCoordBuffer.put(new byte[]{(byte)0,(byte)1});
        texCoordBuffer.rewind();

        //get the shader
        ShaderProgram imageShader = Game.getInstance().getAssetManager().getShaderLoader().getShaderProgram("textureToFBOvert.glsl", "textureToFBOfrag.glsl");

        //starts running the shader program
        gl.glUseProgram(imageShader.program());     

        //enables vertex array data
        gl.glEnableClientState(GL3bc.GL_VERTEX_ARRAY);

        //gets the location of the position and texCoord attributes
        int texCoordAttributeLocation = gl.glGetAttribLocation(imageShader.program(), "a_texCoord");
        int positionAttributeLocation = gl.glGetAttribLocation(imageShader.program(), "a_position");

        //enables the use of the attributes a_texCoord and a_position
        gl.glEnableVertexAttribArray(texCoordAttributeLocation); 
        gl.glEnableVertexAttribArray(positionAttributeLocation); 

        //points the attributes to their corresponding data buffers
        gl.glVertexAttribPointer(texCoordAttributeLocation, 2, GL3bc.GL_BYTE, false, 0, texCoordBuffer);
        gl.glVertexAttribPointer(positionAttributeLocation, 2, GL3bc.GL_FLOAT, false, 0, positionBuffer);            

        //binds our texture to texture unit 0    
        gl.glEnable(GL3bc.GL_TEXTURE_2D);  
        gl.glActiveTexture(GL3bc.GL_TEXTURE0);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, Game.getInstance().getGraphicsWindow().getFboTextureArray()[1][0]);     

        //sets the texture sampler
        gl.glUniform1i(gl.glGetUniformLocation(imageShader.program(), "s_texture"), 0);

        //draws the primitive
        gl.glDrawArrays(GL3bc.GL_QUADS, 0, 4);

        //stops the shader program
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        gl.glDisable(GL3bc.GL_TEXTURE_2D);  
        gl.glUseProgram(0);
        gl.glDisableClientState(GL3bc.GL_VERTEX_ARRAY);
        
       
    }
    
    
    //tesselator callback
    private static class TessCallBack implements GLUtessellatorCallback   
    {
    private GL3bc gl;
    private GLU glu;
    

    public TessCallBack(GL3bc gl, GLU glu)
    {
      this.gl = gl;
      this.glu = glu;
    }

    public void begin(int type)
    {
      gl.glBegin(type);
    }

    public void end()
    {
      gl.glEnd();
    }

    public void vertex(Object vertexData)
    {
      double[] pointer;
      if (vertexData instanceof double[])
      {
        pointer = (double[]) vertexData;
        if (pointer.length == 6) gl.glColor3dv(pointer, 3);
        gl.glVertex3dv(pointer, 0);
      }
      
      

    }

    public void vertexData(Object vertexData, Object polygonData)
    {
    }

    /*
     * combineCallback is used to create a new vertex when edges intersect.
     * coordinate location is trivial to calculate, but weight[4] may be used to
     * average color, normal, or texture coordinate data. In this program, color
     * is weighted.
     */
    public void combine(double[] coords, Object[] data, //
        float[] weight, Object[] outData)
    {
      double[] vertex = new double[6];
      int i;

      vertex[0] = coords[0];
      vertex[1] = coords[1];
      vertex[2] = coords[2];
      for (i = 3; i < 6/* 7OutOfBounds from C! */; i++)
        vertex[i] = weight[0] //
                    * ((double[]) data[0])[i] + weight[1]
                    * ((double[]) data[1])[i] + weight[2]
                    * ((double[]) data[2])[i] + weight[3]
                    * ((double[]) data[3])[i];
      outData[0] = vertex;
    }

    public void combineData(double[] coords, Object[] data, //
        float[] weight, Object[] outData, Object polygonData)
    {
    }

    public void error(int errnum)
    {
      String estring;

      estring = glu.gluErrorString(errnum);
      
      //log error to console
        Log.error( "Tessellation Error: " + estring);
            
        throw new RuntimeException("Tesselation Error");
    }

    public void beginData(int type, Object polygonData)
    {
    }

    public void endData(Object polygonData)
    {
    }

    public void edgeFlag(boolean boundaryEdge)
    {
    }

    public void edgeFlagData(boolean boundaryEdge, Object polygonData)
    {
    }

    public void errorData(int errnum, Object polygonData)
    {
    }
  }
    
    
    
}
