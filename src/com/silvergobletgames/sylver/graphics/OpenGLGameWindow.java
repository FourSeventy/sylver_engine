package com.silvergobletgames.sylver.graphics;

import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.util.MonitorModeUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.silvergobletgames.sylver.core.*;
import com.silvergobletgames.sylver.graphics.Text.CoreTextType;
import com.silvergobletgames.sylver.graphics.Text.TextType;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.security.CodeSource;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.media.nativewindow.WindowClosingProtocol;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import com.silvergobletgames.sylver.util.SylverVector2f;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import javax.media.nativewindow.util.RectangleImmutable;

public class OpenGLGameWindow implements GLEventListener 
{
    //the GLWindow that we render to
    public GLWindow glWindow; 
    //a drawable that should only be used for getting a context for loading textures and shaders
    public GLPbuffer loadingBuffer;   
    //the scene queued for rendering
    private Scene sceneToRender;   
    //vsync toggle
    private boolean waitingToToggleVSync = false;
    //Last render time
    public static long lastRenderTime;
    
    //map containing text renderers
    public static HashMap<TextType,TextRenderer> textRenderers = new HashMap();
    //map containing the font metrics for the different text types
    public static HashMap<TextType,FontMetrics> fontMetricsMap = new HashMap();
    
    //current cursor
    public Cursor cursor;
   
       
    //FBO and FBO Texture variables    
    public static int fbo;
    public static int backbufferTexture;
    public static int[][] textureArray = new int[4][3]; // x = 0: 1x,    x = 1: 2x,    x = 2: 4x,    x = 3: 8x;
    
    //aspect ratio variables
    private final Point ASPECT_RATIO_16_9 = new Point(1600,900);
    private final Point ASPECT_RATIO_16_10 = new Point(1440,900);
    private final Point ASPECT_RATIO_5_4 = new Point(1125,900);
    private final Point ASPECT_RATIO_4_3 = new Point(1200,900);
    public Point viewportPixelSize = new Point();   
    private Point currentAspectRatio;
         
    
    
    //=================
    // Constructor
    //=================

    public OpenGLGameWindow() 
    {  
        
        //supposed mandatory call
        GLProfile.initSingleton();
        
        //gets the glProfile and checks that it is GL3 compatible
        GLProfile glProfile = GLProfile.getDefault();  
        Logger logger =Logger.getLogger(OpenGLGameWindow.class.getName()); 
        logger.log(Level.INFO, "Initializing OpenGL:");
        logger.log(Level.INFO, " Is capable of GL2: " + glProfile.isGL2());
        logger.log(Level.INFO, " Is capable of GL3: " + glProfile.isGL3());
        logger.log(Level.INFO, " Is capable of GLSL: " + glProfile.hasGLSL()  + "\n");
        
        //build the glCapabilities, and set some settings
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setDoubleBuffered(true);
        glCapabilities.setHardwareAccelerated(true);            
        
        //build and initialize our window
        glWindow = GLWindow.create(glCapabilities);
        glWindow.setTitle("Lead Crystal");
        glWindow.setAnimator(new Animator());
        glWindow.addGLEventListener(this);
        glWindow.setAutoSwapBufferMode(true);  
        glWindow.setAlwaysOnTop(false);
        glWindow.setUpdateFPSFrames(10, null);
        glWindow.setPosition(20 , 30);
                  
        //set visible and set the screen size and fullscreen 
        glWindow.setVisible(true);  
        
        //set up loading buffer
        GLDrawableFactory factor = GLDrawableFactory.getFactory(glProfile);
        GLCapabilitiesChooser glcc = new DefaultGLCapabilitiesChooser(); 
        GLPbuffer buffer =factor.createGLPbuffer(factor.getDefaultDevice(), glCapabilities, glcc, 800, 600, glWindow.getContext());
        buffer.createContext(glWindow.getContext());
        loadingBuffer = buffer;
            
          
    }
    
    public void postInit()
    {
         //if we dont have a screen resolution in our user settings, set a default one
        if(Game.getInstance().getConfiguration().getEngineSettings().screenResolution.getWidth() == 0)
        {
            //get our screen mode, and set the screen size 
            Screen screen = glWindow.getScreen();
            MonitorMode currentScreenMode = glWindow.getMainMonitor().getOriginalMode();
            Game.getInstance().getConfiguration().getEngineSettings().screenResolution.setWidth(currentScreenMode.getSurfaceSize().getResolution().getWidth());
            Game.getInstance().getConfiguration().getEngineSettings().screenResolution.setHeight(currentScreenMode.getSurfaceSize().getResolution().getHeight()); 
        }
        
        //set screen size
        this.setDisplayResolution(Game.getInstance().getConfiguration().getEngineSettings().screenResolution);
        
        //set fullscreen
        if(Game.getInstance().getConfiguration().getEngineSettings().fullScreen == true)
            this.toggleFullScreen();      
        
                
        //init key listeners
        this.glWindow.addKeyListener(Game.getInstance().getInputHandler()); 
        this.glWindow.addMouseListener(Game.getInstance().getInputHandler());
        this.glWindow.addWindowListener(new WindowListener(){

            @Override
            public void windowResized(com.jogamp.newt.event.WindowEvent we)
            {
                
            }

            @Override
            public void windowMoved(com.jogamp.newt.event.WindowEvent we)
            {
                
            }

            @Override
            public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent we)
            {
                Game.getInstance().exitGame();
            }

            @Override
            public void windowDestroyed(com.jogamp.newt.event.WindowEvent we)
            {
            }

            @Override
            public void windowGainedFocus(com.jogamp.newt.event.WindowEvent we)
            {
               
            }

            @Override
            public void windowLostFocus(com.jogamp.newt.event.WindowEvent we)
            {
              
            }

            @Override
            public void windowRepaint(WindowUpdateEvent wue)
            {
                
            }
        });
        
        //set up close operation ([x] button)
        this.glWindow.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DO_NOTHING_ON_CLOSE);
   
        
        //load system textures
        URL textureURL = OpenGLGameWindow.class.getClassLoader().getResource("com/silvergobletgames/sylver/systemtextures");
        try
        {
            Game.getInstance().getAssetManager().getTextureLoader().loadAllTextures(textureURL.toURI());
        }
        catch (IOException |URISyntaxException ex)
        {
            //log error to console
            Logger errorLogger =Logger.getLogger(OpenGLGameWindow.class.getName());
            errorLogger.log(Level.SEVERE, "Error Loading System Texture: " + ex.toString());
            errorLogger.addHandler(new ConsoleHandler()); 
        }
        
        //load system shaders
        this.loadSystemShaders();
                             
        //init text renderers
        registerDefaultTextRenderers();     

    }
    
 
    
    
    //====================
    // GL Drawable Methods
    //====================

    //openGL dispose callback
    public void dispose(GLAutoDrawable drawable) 
    {
       
    }

    //openGL init callback
    public void init(GLAutoDrawable drawable) 
    {
        //get graphics context
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = new GLU();

        //determine the correct aspect ratio
        this.determineAspectRatio();
        
        //initialize modelview matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);

        //initialize projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, this.currentAspectRatio.x, 0.0,  this.currentAspectRatio.y);  //set up the world window
    
        //set up the viewport ( this tells openGL how big your screen is/ the window is)
        gl.glViewport(0, 0, glWindow.getWidth(), glWindow.getHeight());   
        
        //disables color clamping
        gl.glClampColor(GL2.GL_CLAMP_VERTEX_COLOR, GL2.GL_FALSE); //Disables clamping of glColor
        gl.glClampColor(GL2.GL_CLAMP_READ_COLOR, GL2.GL_FALSE); //Kind of unnecessary but whatever
        gl.glClampColor(GL2.GL_CLAMP_FRAGMENT_COLOR, GL2.GL_FALSE); //Disables clamping of the fragment color output from fragment shaders
        
        //sets up bilinear texture filtering
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
                 
        // Enable VSync
        gl.setSwapInterval(Game.getInstance().getConfiguration().getEngineSettings().vSync? 1: 0);       
        
        //set display gamma
        //com.jogamp.opengl.util.Gamma.setDisplayGamma(gl, 3, 0, 1);
                  
    }
    
    //openGL display callback
    public void display(GLAutoDrawable drawable) 
    {
        long start = System.nanoTime();    
        
        //gets the appropriate graphics context from the glWindow
        GL2 gl;
        if(drawable.getGL().getGLProfile().isGL4bc())
            gl = drawable.getGL().getGL4bc();
        else if(drawable.getGL().getGLProfile().isGL3bc())
            gl = drawable.getGL().getGL3bc();
        else
            gl = drawable.getGL().getGL2();
        
        //toggles vsync on or off
        if(waitingToToggleVSync)
        {
            gl.setSwapInterval(Game.getInstance().getConfiguration().getEngineSettings().vSync ? 1: 0);
            waitingToToggleVSync = false;
        }               
           
        //tells the scene to render itself
        if (sceneToRender != null) 
        {
            sceneToRender.render(gl);                
        }

        //reconfigures matrices for cursor draw
        Point aspectRatio = this.getCurrentAspectRatio();
        gl.glMatrixMode(GL3bc.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0, aspectRatio.x, 0.0, aspectRatio.y);
        
        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
        
        //Draw cursor
        if(this.cursor != null)
        {
            this.cursor.getImage().update();
            this.cursor.getImage().setPositionAnchored(Game.getInstance().getInputHandler().getInputSnapshot().getScreenMouseLocation().x, Game.getInstance().getInputHandler().getInputSnapshot().getScreenMouseLocation().y);
            this.cursor.getImage().draw(gl);
        }
        
        
        //flushes pending openGL commands from the context, !!needs to be here!!
        gl.glFlush();
              
        //Save the last render time before we call swap
        OpenGLGameWindow.lastRenderTime = System.nanoTime() - start;   
        //System.out.println("Render Time: " +lastRenderTime);       
        
    }

    //openGL reshape callback
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        
        //gets the appropriate graphics context from the glWindow
        GL2 gl;
        if(drawable.getGL().getGLProfile().isGL3bc())
            gl = drawable.getGL().getGL3bc();
        else
            gl = drawable.getGL().getGL2();
        
        GLU glu = new GLU();
        
        //determine the matching aspect ratio
        this.determineAspectRatio();

        // calculate any cropping we have to do if the aspect ratio isn't quite right
        float newAspectRatio = (float)width/(float)height;
        float scale = 1f;
        SylverVector2f crop = new SylverVector2f(0f, 0f); 
        
        if(newAspectRatio > (float)this.currentAspectRatio.x/(float)this.currentAspectRatio.y)
        {
            scale = (float)height/(float)this.currentAspectRatio.y;
            crop.x = (width - (float)this.currentAspectRatio.x*scale)/2f;
        }
        else if(newAspectRatio < (float)this.currentAspectRatio.x/(float)this.currentAspectRatio.y)
        {
            scale = (float)width/(float)this.currentAspectRatio.x;
            crop.y = (height - (float)this.currentAspectRatio.y*scale)/2f;
        }
        else
        {
            scale = (float)width/(float)this.currentAspectRatio.x;
        }

        //calculate the final viewport pixel size
        this.viewportPixelSize.x = (int)(this.currentAspectRatio.x*scale);
        this.viewportPixelSize.y = (int)(this.currentAspectRatio.y*scale);
        
        //change the viewport in accordance to the shape of the frame
        gl.glViewport((int)crop.x, (int)crop.y, this.viewportPixelSize.x,this.viewportPixelSize.y);
             
        //re-initialize our fbo's
        if(drawable.getGL().getGLProfile().isGL3bc())
            initFrameBufferObjects((GL3bc)gl,this.viewportPixelSize.x,this.viewportPixelSize.y);  
        
        
         //re-initialize projection matrix
        gl.glMatrixMode(GL3bc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, this.currentAspectRatio.x, 0.0,  this.currentAspectRatio.y);
    }

    //openGL display changed callback
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
    {
    }
    
    
    //====================
    // Class Methods
    //====================
    
    /**
     * Gets the aspect ratio that the game is currently set to
     * @return The current aspect ratio
     */
    public Point getCurrentAspectRatio()
    {
        return this.currentAspectRatio;
    } 
 
    /**
     * Toggles fullscreen on and off. This should put the game into full-screen exclusive mode
     */
    public final void toggleFullScreen()
    {
        if(glWindow.isFullscreen())
        {
            //switches to windowed mode and reverts the display device back to its original resolution
            glWindow.setFullscreen(false);
            glWindow.getMainMonitor().setCurrentMode(glWindow.getMainMonitor().getOriginalMode());
        }
        else
        {
            //switches to fullscreen mode and sets the display device to the correct resolution
            glWindow.setFullscreen(true);
            //setDisplayResolution(SystemSettings.getInstance().screenResolution);
        }
        
        //makes sure we are not AlwaysOnTop
        glWindow.setAlwaysOnTop(false);
    }
    
    /**
     * Sets the resolution the game will display at. If we are currently in fullscreen mode this method will
     * change the resolution of the display device. If we are in windowed mode it will simply change the size
     * of the window.
     * @param resolution New resolution to display at 
     */
    public final void setDisplayResolution(javax.media.nativewindow.util.Dimension resolution)
    {
       // change the window size
        glWindow.setSize(resolution.getWidth(),resolution.getHeight());
        
        //if we are in fullscreen change the screen resolution
        if(glWindow.isFullscreen() == true)
        {
            //get our screen mode
            Screen screen = glWindow.getScreen();
            MonitorMode currentMonitorMode = glWindow.getMainMonitor().getOriginalMode();
            
            //filters screen modes
            ArrayList<MonitorMode> monitorModes = new ArrayList(screen.getMonitorModes());
            if(monitorModes.size()>1) 
            { 
                monitorModes = new ArrayList(MonitorModeUtil.filterByRate(monitorModes, currentMonitorMode.getRefreshRate())); 
                monitorModes = new ArrayList(MonitorModeUtil.filterByRotation(monitorModes, 0)); 
                monitorModes = new ArrayList (MonitorModeUtil.filterByResolution(monitorModes, resolution)); 
                monitorModes = new ArrayList (MonitorModeUtil.getHighestAvailableBpp(monitorModes)); 

                MonitorMode sm = (MonitorMode) monitorModes.get(0); 
                glWindow.getMainMonitor().setCurrentMode(sm); 
            } 
        }
             
        // Change aspect ratio right away
        this.determineAspectRatio();
        
       
    }
    
    /**
     * Toggle vsync onn and off
     */
    public void toggleVSync()
    {
        this.waitingToToggleVSync = true;
    }
     
    /**
     * Sets the current cursor
     * @param cursor Cursor that will be rendered
     */
    public final void setCursor(Cursor cursor)
    {
        //set cursor
        this.cursor = cursor;
        
        if(cursor != null)       
            glWindow.setPointerVisible(false);                
        else       
            glWindow.setPointerVisible(true);
        
            
         
    }

    /**
     * Returns the current cursor
     * @return the current Cursor
     */
    public Cursor getCursor()
    {
        return this.cursor;
    }
    
    /**
     * Registers a TextType. TextTypes must be registered with OpenGLGameWindow before than can be used.
     * @param type The TextType enum to associate with
     * @param textRenderer the TextRenderer containing all the font information
     * @param metrics  the FontMetrics containing the font metric data 
     */
    public void registerTextRenderer(TextType type, TextRenderer textRenderer, FontMetrics metrics)
    {
        textRenderers.put(type, textRenderer);
        fontMetricsMap.put(type, metrics);      
    }
    
    /**
     * Gets the FPS that the game is currently rendering at
     * @return FPS that the game is rendering at
     */
    public float getFPS()
    {
       float fps = this.glWindow.getTotalFPS();
       
       this.glWindow.resetFPSCounter();
       return fps;
    }   
    
    //=====================
    // Private Methods
    //====================
    
    /**
     * Load the glsl shaders that are used by the Sylver engine.
     */
    private void loadSystemShaders()
    {
        try
        {
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/lightsourcevert.glsl").toURI(),this.getClass().getClassLoader().getResource( "com/silvergobletgames/sylver/shaders/lightsourcefrag.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/tonemapvert.glsl").toURI(), this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/tonemapfrag.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/darkenervert.glsl").toURI(), this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/darkenerfrag.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/bloommultivert.glsl").toURI(), this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/bloommultifrag.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/gaussianVert.glsl").toURI(), this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/gaussianFragH.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/gaussianVert.glsl").toURI(), this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/gaussianFragV.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/imageVert.glsl").toURI(),this.getClass().getClassLoader().getResource( "com/silvergobletgames/sylver/shaders/imageFrag.glsl").toURI());
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/fastGaussianVert.glsl").toURI(),this.getClass().getClassLoader().getResource( "com/silvergobletgames/sylver/shaders/fastGaussianFrag.glsl").toURI());    
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/lightsourceverttess.glsl").toURI(),this.getClass().getClassLoader().getResource( "com/silvergobletgames/sylver/shaders/lightsourcefrag.glsl").toURI());    
            Game.getInstance().getAssetManager().getShaderLoader().loadShaderProgram(this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/textureToFBOvert.glsl").toURI(),this.getClass().getClassLoader().getResource("com/silvergobletgames/sylver/shaders/textureToFBOfrag.glsl").toURI());    
        }
        catch(URISyntaxException e)
        {
            throw new RuntimeException("Error loading Sylver system shaders");
        }
    }
   
    
    /**
     * Registers the default text renderers
     */
    private void registerDefaultTextRenderers()
    {
        //build a java 2d graphics context to get the font matrics from
        BufferedImage bufferedImage = new BufferedImage ( 2 ,2 ,BufferedImage.TYPE_4BYTE_ABGR_PRE );
        Graphics2D java2d = ( Graphics2D)( bufferedImage.createGraphics());
              
        //DEFAULT
        Font font = new Font("CALIBRI", Font.BOLD, 24);
        TextRenderer def = new TextRenderer(font, true, true, new SylverRenderDelegate());
        def.setUseVertexArrays(false);
        FontMetrics fm = java2d.getFontMetrics(font);
        fontMetricsMap.put(CoreTextType.DEFAULT, fm);
        textRenderers.put(CoreTextType.DEFAULT, def);
 
        //MENU
        font = new Font("CALIBRI", Font.BOLD, 96);
        TextRenderer menu = new TextRenderer(font, true, true,new SylverRenderDelegate(true,3));
        menu.setUseVertexArrays(false);
        fm = java2d.getFontMetrics(font);
        fontMetricsMap.put(CoreTextType.MENU, fm);
        textRenderers.put(CoreTextType.MENU, menu);
        
        //CODE
        font = new Font("COURIER NEW", Font.PLAIN, 12);
        TextRenderer code = new TextRenderer(font);
        code.setUseVertexArrays(false);
        fm = java2d.getFontMetrics(font);
        fontMetricsMap.put(CoreTextType.CODE, fm);
        textRenderers.put(CoreTextType.CODE, code);
        
    }
    
    /**
     * Initializes the FBOs used for lighting and rendering
     * @param gl 
     */
    private void initFrameBufferObjects(GL3bc gl, int w, int h) 
    {
        //generates the fbo
        IntBuffer returnedFrameBuffers = GLBuffers.newDirectIntBuffer(1);
        gl.glGenFramebuffers(1, returnedFrameBuffers);
        fbo = returnedFrameBuffers.get(0);
        
        //generates a bunch of textures that we will use
        IntBuffer generatedTextures = GLBuffers.newDirectIntBuffer(10);
        gl.glGenTextures(10, generatedTextures);

        //TEXTURE: BackBuffer
        backbufferTexture = generatedTextures.get(0);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, backbufferTexture);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE); //clamp texture coords to edge
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE); //clamp texture coords to edge
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR); //billinear filtering
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR); //billinear filtering
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 1 - Vertical
        textureArray[0][0] = generatedTextures.get(1);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[0][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 1 - Horizontal
        textureArray[0][1] = generatedTextures.get(2);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[0][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 1 - Vertical
        textureArray[0][2] = generatedTextures.get(9);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[0][2]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Vertical
        textureArray[1][0] = generatedTextures.get(3);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[1][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/2, h/2, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Horizontal
        textureArray[1][1] = generatedTextures.get(4);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[1][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/2, h/2, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Vertical
        textureArray[2][0] = generatedTextures.get(5);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[2][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/4, h/4, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Horizontal
        textureArray[2][1] = generatedTextures.get(6);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[2][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/4, h/4, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 3 - Vertical
        textureArray[3][0] = generatedTextures.get(7);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[3][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/8, h/8, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 3 - Horizontal
        textureArray[3][1] = generatedTextures.get(8);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, textureArray[3][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/8, h/8, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
    
              
    }
    
    /**
     * Determines the correct aspect ratio
     */
    private void determineAspectRatio()
    {
       //the aspect ratio of the window
       float screenAspectRatio = (float)glWindow.getWidth()/(float)glWindow.getHeight();
       
       //find the aspect ratio that most closely matches the ratio of the window
       Point[] ratioList = new Point[4];
       ratioList[0] = ASPECT_RATIO_16_9; // 16:9
       ratioList[1] = ASPECT_RATIO_16_10; // 16:10
       ratioList[2] =ASPECT_RATIO_5_4; // 5:4
       ratioList[3] = ASPECT_RATIO_4_3; // 4:3
       
       int indexOfClosest =0;
       float difference = Float.MAX_VALUE;
       
       for(int i= 0; i <=3; i++)
       {
           float ratio = (float)ratioList[i].x / (float)ratioList[i].y;
           
           if(Math.abs(screenAspectRatio - ratio) < difference)
           {
               difference = Math.abs(screenAspectRatio - ratio);
               indexOfClosest = i;
           }
       }
       
       this.currentAspectRatio = ratioList[indexOfClosest];
       
       //debug
       Logger logger =Logger.getLogger(OpenGLGameWindow.class.getName());
       switch(indexOfClosest)
       {
           case 0: logger.log(Level.INFO,"Detected Aspect Ratio 16:9"); break;
           case 1: logger.log(Level.INFO,"Detected Aspect Ratio 16:10"); break;
           case 2: logger.log(Level.INFO,"Detected Aspect Ratio 5:4"); break;
           case 3: logger.log(Level.INFO,"Detected Aspect Ratio 4:3"); break;
       }
                      
    }
    
    /**
     * Pass a scene to this method to render it. This method is for engine use only
     * @param s Scene to render
     */
    public void renderScene(Scene s) 
    {
        sceneToRender = s;
        glWindow.display();
        
    }
    
    
    //====================
    // Inner classes
    //====================     
    
     /**
     * This class is used by the TextRenderer to do fancy things.  It is instantiated
     * when we build a new TextRenderer.
     */
    public static class SylverRenderDelegate implements TextRenderer.RenderDelegate 
    {

        boolean drawOutline = true;
        float strokeWidth = .8f;
        
        public SylverRenderDelegate() {
        }

        public SylverRenderDelegate(boolean outline, float strokeWidth){
            this.drawOutline = outline;
            this.strokeWidth = strokeWidth;
        }
        
        public boolean intensityOnly() {
            return false;
        }

        public Rectangle2D getBounds(CharSequence str,
                Font font,
                FontRenderContext frc) {
            return getBounds(str.toString(), font, frc);
        }

        public Rectangle2D getBounds(String str,
                Font font,
                FontRenderContext frc) {
            return getBounds(font.createGlyphVector(frc, str), frc);
        }

        public Rectangle2D getBounds(GlyphVector gv, FontRenderContext frc) {
            Rectangle2D stringBounds = gv.getPixelBounds(frc, 0, 0);
            return new Rectangle2D.Double(stringBounds.getX(),
                    stringBounds.getY(),
                    stringBounds.getWidth(),
                    stringBounds.getHeight());
        }

        public void drawGlyphVector(Graphics2D g2, GlyphVector str, int x, int y) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

//            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
//                    RenderingHints.VALUE_RENDER_QUALITY);

            g2.setStroke(new BasicStroke(strokeWidth));
            g2.setColor(java.awt.Color.white);
            g2.drawGlyphVector(str, x, y);

            if (drawOutline){
                Shape outline = str.getOutline(x, y);
                g2.setColor(java.awt.Color.black);
                g2.draw(outline);
            }
        }

        public void draw(Graphics2D g2, String str, int x, int y) {
        }
    }
    
}
