package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.core.Scene.Layer;
import com.silvergobletgames.sylver.core.SceneEffectsManager;
import com.silvergobletgames.sylver.core.SceneObject;
import com.silvergobletgames.sylver.core.SceneObjectManager;
import java.util.ArrayList;
import java.util.Arrays;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import javax.media.opengl.glu.gl2.GLUgl2;

/**
 *
 * Rendering pipeline for rendering a scene. This rendering pipeline requires OpenGL 2.0, and GLSL 1.0.
 * Renderes a scene with parallax layers, but no advanced effects
 */
public class RenderingPipelineGL2 
{
    /**
     * Rendering pipeline for rendering a scene. This rendering pipeline requires OpenGL 2.0, and GLSL 1.0.
     * Renderes a scene with parallax layers, but no advanced effects
     * @param gl OpenGl context
     * @param viewport Viewport belonging to the scene
     * @param sceneObjectManager SceneObjectManager of the scene
     * @param sceneEffectsManager SceneEffectsManager of the scene
     * @param excluded optional parameter of layers to exclude from the rendering
     */
    public static void render(GL2 gl,Viewport viewport,SceneObjectManager sceneObjectManager, SceneEffectsManager sceneEffectsManager, Layer... excluded)
    {
         //excluded layers arraylist
        ArrayList<Layer> excludedLayers = new ArrayList(Arrays.asList(excluded));
        
        //clear the framebuffer
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        
        //set up projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        GLUgl2 glu = new GLUgl2();
        glu.gluOrtho2D(0.0, viewport.getWidth(), 0.0, viewport.getHeight());
        
        //===========================================
        //Draw layers Background through FOREGROUND2
        //===========================================

        //background
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
        ArrayList<SceneObject> bg = sceneObjectManager.get(Layer.BACKGROUND);
        for (int i = 0; i < bg.size(); i++) {
            bg.get(i).draw(gl);
        }

        //layers PARRALAX5 through ATTACHED_FG
        ArrayList<SceneObject> sceneObjectLayer;
        for (Layer layer: Layer.values()) 
        {   
            //only draw PARRALAX5 through FOREGROUND2
            if(layer == Layer.BACKGROUND || layer == Layer.WORLD_HUD || layer == Layer.HUD || layer == Layer.MENU || excludedLayers.contains(layer))
                continue;

            //draw the sceneObjects
            sceneObjectLayer = sceneObjectManager.get(layer);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            glu.gluLookAt(viewport.getBottomLeftCoordinate().x * layer.coordinateScalingFactor, viewport.getBottomLeftCoordinate().y * layer.coordinateScalingFactor, 1, viewport.getBottomLeftCoordinate().x * layer.coordinateScalingFactor, viewport.getBottomLeftCoordinate().y * layer.coordinateScalingFactor, 0, 0, 1, 0);
            for (int j = 0; j < sceneObjectLayer.size(); j++) 
            {
                if(viewport.isSceneObjectVisible(sceneObjectLayer.get(j), layer))
                    sceneObjectLayer.get(j).draw(gl);    
            }


        }
        
        //=====================
        //Draw world HUD Layer
        //=====================
        if(!excludedLayers.contains(Layer.WORLD_HUD))
        {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
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
            gl.glMatrixMode(GL2.GL_MODELVIEW);
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
            gl.glMatrixMode(GL2.GL_MODELVIEW);
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
}
