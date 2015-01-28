package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.util.Log;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.util.MonitorModeUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
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
    private GLWindow glWindow; 
    //a drawable that should only be used for getting a context for loading textures and shaders
    private GLOffscreenAutoDrawable offscreenLoadingBuffer;   
    
    //the scene queued for rendering
    private Scene sceneToRender;   
    //Last render time
    private static long lastRenderTime;
    
    //map containing text renderers
    private HashMap<TextType,TextRenderer> textRenderers = new HashMap<>();
    //map containing the font metrics for the different text types
    private HashMap<TextType,FontMetrics> fontMetricsMap = new HashMap<>();
    
    //current cursor
    private Cursor cursor;
       
    //FBO and FBO Texture variables    
    private int fbo;
    private int backbufferTexture;
    private int[][] fboTextureArray = new int[4][3]; // x = 0: 1x,    x = 1: 2x,    x = 2: 4x,    x = 3: 8x;
    
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
        //gets the glProfile and checks for GL capabilities
        GLProfile glProfile = GLProfile.getDefault();  
        Log.info("Initializing OpenGL:");
        Log.info("\n GL Implementation: " + glProfile.getImplName() +
                               "\n Is capable of GL2: " + glProfile.isGL2() + 
                               "\n" + " Is capable of GL3: " + glProfile.isGL3()+ 
                               "\n" + " Is capable of GLSL: " + glProfile.hasGLSL()  + "\n" );
        
        //build the glCapabilities, and set some settings
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setDoubleBuffered(true);
        glCapabilities.setHardwareAccelerated(true);   
        glCapabilities.setBackgroundOpaque(true);
        glCapabilities.setOnscreen(true);
        glCapabilities.setSampleBuffers(false);
        
        //build and initialize our NEWT glWindow
        glWindow = GLWindow.create(glCapabilities);
        glWindow.setTitle("Lead Crystal");
        glWindow.addGLEventListener(this);
        glWindow.setAutoSwapBufferMode(false);  //important to manually swap our buffers !dont change!
        glWindow.setAlwaysOnTop(false);
        glWindow.setUpdateFPSFrames(10, null);
        glWindow.setPosition(10 , 30);
                       
        //local fullscreen variable
        boolean isFullscreen = Game.getInstance().getConfiguration().getEngineSettings().fullScreen;
        javax.media.nativewindow.util.Dimension screenResolution = Game.getInstance().getConfiguration().getEngineSettings().screenResolution;
       

        //set visible and set the screen size and fullscreen //!! ABSOLUTELY MUST SET VISIBLE BEFORE ANYTHING ELSE!!!
        glWindow.setVisible(true); 
        
        //this is to have a more clean entrance into fullscreen mode
        if(isFullscreen)
        {
           glWindow.setPosition(0, 0);
           glWindow.setUndecorated(true);
        }
       
        //if we dont have a screen resolution in our user settings, set a default one
        if(screenResolution.getWidth() == 0)
        {
            //get our screen mode, and set the screen size 
            MonitorMode currentScreenMode = glWindow.getMainMonitor().getOriginalMode();
            Game.getInstance().getConfiguration().getEngineSettings().screenResolution.setWidth(currentScreenMode.getSurfaceSize().getResolution().getWidth());
            Game.getInstance().getConfiguration().getEngineSettings().screenResolution.setHeight(currentScreenMode.getSurfaceSize().getResolution().getHeight()); 
        }
        
        //set size of window
        this.setDisplayResolution(screenResolution);
              
        //set fullscreen 
        if(isFullscreen)
        {          
             
            this.toggleFullScreen();             
            this.setVSync(Game.getInstance().getConfiguration().getEngineSettings().vSync);
        }  
        else
        {
          this.setVSync(false);
        }

     
        //set up loading buffer      
        GLDrawableFactory glDrawableFactory = GLDrawableFactory.getFactory(glProfile);
        GLCapabilities glOffscreenCapabilities = new GLCapabilities(glProfile);
        glOffscreenCapabilities.setOnscreen(false);
        glOffscreenCapabilities.setFBO(false);
        glOffscreenCapabilities.setPBuffer(true);        
        GLCapabilitiesChooser glcc = new DefaultGLCapabilitiesChooser(); 
        GLOffscreenAutoDrawable offscreenDrawable = glDrawableFactory.createOffscreenAutoDrawable(glDrawableFactory.getDefaultDevice(),glWindow.getChosenGLCapabilities() , glcc, 2, 2); 
        offscreenDrawable.setSharedContext(this.glWindow.getContext());
        offscreenDrawable.display(); //to initialize lazy context loading
        offscreenLoadingBuffer = offscreenDrawable;
        
        //attach fakey animator to ignore repaint events
       glWindow.setAnimator(new AnimatorBase(){
           {
               
           }
           @Override
           public boolean isStarted()
           {
               return true;
           }
           
           @Override
           public boolean isAnimating()
           {
               return true;
           }
           
           @Override
           public boolean start()
           {
               return true;
           }

            @Override
            protected String getBaseName(String string)
            {
              return "animator";
            }

            @Override
            public boolean isPaused()
            {
              return false;
            }

            @Override
            public boolean stop()
            {
              return true;
            }

            @Override
            public boolean pause()
            {
                return true;
            }

            @Override
            public boolean resume()
            {
                return true;
            }
       });      
       glWindow.getAnimator().start();

    }
    
    public void postInit()
    {                                
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
        this.loadSystemTextures();
        
        //load system shaders
        this.loadSystemShaders();
                             
        //init text renderers
        this.registerDefaultTextRenderers(); 

    }
    
  
    //====================
    // GL Drawable Methods
    //====================

    //openGL dispose callback
    public void dispose(GLAutoDrawable drawable) 
    {
        Log.trace("OpenGL dispose()");
    }

    //openGL init callback
    public void init(GLAutoDrawable drawable) 
    {
           
        Log.trace( "OpenGL init()");
                        
        //get graphics context
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = new GLU();
        
        //determine aspect ratio
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
                      
        //paint the screen black while system stuff loads
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);                   
        glWindow.swapBuffers();
        
        //clear both buffers
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT); 
    }
    
    //openGL display callback
    public void display(GLAutoDrawable glAutoDrawable) 
    {
        
        long start = System.nanoTime();    

        //gets the appropriate graphics context from the glWindow
        GL2 gl;
        if(glAutoDrawable.getGL().getGLProfile().isGL4bc())
            gl = glAutoDrawable.getGL().getGL4bc();
        else if(glAutoDrawable.getGL().getGLProfile().isGL3bc())
            gl = glAutoDrawable.getGL().getGL3bc();
        else
            gl = glAutoDrawable.getGL().getGL2();              

        //clears the frame buffer
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);

        //tells the scene to render itself
        if (sceneToRender != null) 
        {
            sceneToRender.render(gl);                
        }

        //reconfigures matrices for cursor draw
        Point aspectRatio = getCurrentAspectRatio();
        gl.glMatrixMode(GL3bc.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0, aspectRatio.x, 0.0, aspectRatio.y);

        gl.glMatrixMode(GL3bc.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);

        //Draw cursor
        if(cursor != null)
        {
            cursor.getImage().update();
            cursor.getImage().setPositionAnchored(Game.getInstance().getInputHandler().getInputSnapshot().getScreenMouseLocation().x, Game.getInstance().getInputHandler().getInputSnapshot().getScreenMouseLocation().y);
            cursor.getImage().draw(gl);
        }

        //Save the last render time before we call swap
        OpenGLGameWindow.lastRenderTime = System.nanoTime() - start; 
        boolean profileRendering = Game.getInstance().getConfiguration().getEngineSettings().profileRendering;
        if( profileRendering== true)
        {
            //log times
            System.err.println( "Total Render Time: " +lastRenderTime);
        }

       //flushes pending openGL commands from the context, !!needs to be here!!
       gl.glFlush();

       //swaps the back buffer !!needs to be here!!
       glWindow.swapBuffers();
                                                                  
    }

    //openGL reshape callback
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        Log.info( "OpenGL reshape(): " +"Width: " + width + ", Height: "+height);
        
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
        {
            initFrameBufferObjects((GL3bc)gl,this.viewportPixelSize.x,this.viewportPixelSize.y);  
        }
              
        //re-initialize projection matrix
        gl.glMatrixMode(GL3bc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, this.currentAspectRatio.x, 0.0,  this.currentAspectRatio.y);
        
        //paint the screen black while system stuff loads
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT);                   
        glWindow.swapBuffers();
        
        //clear both buffers
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT); 
    }

    //openGL display changed callback
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
    {
        Log.trace( "OpenGL displayChanged()");
    }
    
    
    //====================
    // Class Methods
    //====================
    
    /**
     * Pass a scene to this method to render it. This method is for engine use only
     * @param s Scene to render
     */
    public void renderScene(Scene s) 
    {
        sceneToRender = s;
       
        glWindow.display();
            
        
    }
    
    /**
     * Gets the FBO handle that has been initalized by this window
     * @return the FBO handle
     */
    public int getFbo()
    {
        return this.fbo;
    }
    
    /**
     * Gets the backbuffer texture handle that has been initalized by this window
     * @return the backbuffer texture handle
     */
    public int getBackbufferTexture()
    {
        return this.backbufferTexture;
    }
    
    /**
     * Gets the FboTextureArray handle that has been initalized by this window
     * @return the FboTextureArray handle
     */
    public int[][] getFboTextureArray()
    {
        return this.fboTextureArray;
    }
    
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
    public void toggleFullScreen()
    {
        if(glWindow.isFullscreen())
        {
            //switches to windowed mode and reverts the display device back to its original resolution               
            glWindow.setFullscreen(false);  
            glWindow.setUndecorated(false);
            glWindow.setPosition(10, 30);
            setDisplayResolution(Game.getInstance().getConfiguration().getEngineSettings().screenResolution);
            glWindow.getMainMonitor().setCurrentMode(glWindow.getMainMonitor().getOriginalMode());          
            this.setVSync(false);
            this.glWindow.confinePointer(false);
                  
        }
        else
        {
            //switches to fullscreen mode and sets the display device to the correct resolution
            glWindow.setFullscreen(true);        
            setDisplayResolution(Game.getInstance().getConfiguration().getEngineSettings().screenResolution);
            this.glWindow.confinePointer(true);
        }     
    }
    
    /**
     * Returns if the OpenGLGameWindow is in fullscreen or not
     * @return true if in fullscreen, false otherwise
     */
    public boolean isFullscreen()
    {
        return this.glWindow.isFullscreen();
    }
    
    /**
     * Turns Vsync on or off
     * @param turnOn true to turn Vsync on, false to turn it off
     */
    public void setVSync(final boolean turnOn)
    {
        //set the global vsync variable
        Game.getInstance().getConfiguration().getEngineSettings().vSync = turnOn;
        
       //inject the vsync action into the glpipeline
         glWindow.invoke(false, new GLRunnable() {
                @Override
                public boolean run(final GLAutoDrawable glAutoDrawable)
                {
                     //makes the glContext current
                    glWindow.getContext().makeCurrent();
                    
                    if(turnOn)
                    {
                        glWindow.getGL().setSwapInterval(1);
                    }
                    else
                    {
                        glWindow.getGL().setSwapInterval(0);
                    }
                    
                    //releases the glContext
                   glWindow.getContext().release();   
                                      
                    return true;
                }
            });
       
    }
    
    /**
     * Sets the resolution the game will display at. If we are currently in fullscreen mode this method will
     * change the resolution of the display device. If we are in windowed mode it will simply change the size
     * of the window.
     * @param resolution New resolution to display at 
     */
    public void setDisplayResolution(javax.media.nativewindow.util.Dimension resolution)
    {        
        //change the window size
        glWindow.setSize(resolution.getWidth(),resolution.getHeight());

        if( glWindow.isRealized() && glWindow.isFullscreen() == true)
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
                monitorModes = new ArrayList(MonitorModeUtil.filterByResolution(monitorModes, resolution)); 
                monitorModes = new ArrayList(MonitorModeUtil.getHighestAvailableBpp(monitorModes));               
            } 

            //set the monitor mode
            MonitorMode sm = (MonitorMode) monitorModes.get(0); 
            glWindow.getMainMonitor().setCurrentMode(sm); 

            Log.info( "Set to monitor mode: " + sm.toString());
        }
             
    }
       
    /**
     * Sets the current cursor
     * @param cursor Cursor that will be rendered
     */
    public void setCursor(Cursor cursor)
    {
        //set cursor
        this.cursor = cursor;
        
        if(cursor != null)       
            glWindow.setPointerVisible(false);                
        else       
            glWindow.setPointerVisible(true);
               
    }
    
    /**
     * Sets the visibility of the windows pointer 
     * @param value true makes the pointer visible, false hides it
     */
    public void setPointerVisible(boolean value)
    {
        this.glWindow.setPointerVisible(value);
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
    
    /**
     * Gets the width of the window
     * @return width of the window
     */
    public int getWidth()
    {
        return this.glWindow.getWidth();
    }
    
    /**
     * Gets the height of the window
     * @return height of the window
     */
    public int getHeight()
    {
        return this.glWindow.getHeight();
    }
    
    /**
     * Returns the MonitorDevice which viewport covers this window the most.
     * @return 
     */
    public MonitorDevice getMainMonitor()
    {
        return this.glWindow.getMainMonitor();
    }
    
    /**
     * Returns the location of the upper left point of the window in screen coordinates
     * @return window location
     */
    public javax.media.nativewindow.util.Point getLocationOnScreen()
    {
        return this.glWindow.getLocationOnScreen(null);
    }
    
    @Deprecated
    public void setVisible(boolean value)
    {
        this.glWindow.setVisible(value);
    }
    
    /**
     * Returns the offscreen loading buffer which has been set up to handle loading textures and shaders on a seperate thread
     * @return offscreenLoadingBuffer
     */
    protected GLOffscreenAutoDrawable getOffscreenLoadingBuffer()
    {
        return this.offscreenLoadingBuffer;
    }
    
    /**
     * Returns a map of all the registered TextRenderers
     * @return map of registered TextRenderers
     */
    protected HashMap<TextType,TextRenderer> getTextRenderers()
    {
        return this.textRenderers;
    }
    
    /**
     * Returns a map of the FontMetrics for all the registered TextRenderers 
     * @return map of FontMetrics
     */
    protected HashMap<TextType,FontMetrics> getFontMetrics()
    {
        return this.fontMetricsMap;
    }
    
    //=====================
    // Private Methods
    //====================
    
    /**
     * Load the glsl shaders that are used by the Sylver Engine.
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
     * Loads the textures that are used by the Sylver Engine
     */
    private void loadSystemTextures()
    {
        URL textureURL = OpenGLGameWindow.class.getClassLoader().getResource("com/silvergobletgames/sylver/systemtextures");
        try
        {
            Game.getInstance().getAssetManager().getTextureLoader().loadAllTexturesInDirectory(textureURL.toURI());
        }
        catch (IOException |URISyntaxException ex)
        {
            //log error to console
            Log.error( "Error Loading System Texture: " + ex.getMessage(), ex);
       
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
        Font font = new Font("CALIBRI", Font.BOLD, 26);
        TextRenderer def = new TextRenderer(font, true, true, new SylverRenderDelegate());
        def.setUseVertexArrays(false);
        FontMetrics fm = java2d.getFontMetrics(font);
        fontMetricsMap.put(CoreTextType.DEFAULT, fm);
        textRenderers.put(CoreTextType.DEFAULT, def);
 
        //MENU
        font = new Font("CALIBRI", Font.BOLD, 85);
        TextRenderer menu = new TextRenderer(font, true, true,new SylverRenderDelegate(false,0));
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
        //============================
        // Delete Old FBO and Textures
        //============================
        
        //delete framebuffer
        IntBuffer fboBuffer = IntBuffer.allocate(1);
        fboBuffer.put(fbo);
        fboBuffer.rewind();
        gl.glDeleteFramebuffers(1, fboBuffer);
        
        //delete textures
        IntBuffer textureBuffer = IntBuffer.allocate(10);
        textureBuffer.put(backbufferTexture);  
        textureBuffer.put(fboTextureArray[0][0]);
        textureBuffer.put(fboTextureArray[0][1]);
        textureBuffer.put(fboTextureArray[0][2]);
        textureBuffer.put(fboTextureArray[1][0]);
        textureBuffer.put(fboTextureArray[1][1]);
        textureBuffer.put(fboTextureArray[2][0]);
        textureBuffer.put(fboTextureArray[2][1]);
        textureBuffer.put(fboTextureArray[3][0]);
        textureBuffer.put(fboTextureArray[3][1]);       
        textureBuffer.rewind();
        gl.glDeleteTextures(10, textureBuffer);
        
        
        //============================
        // Create new FBO and Textures
        //============================
        
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
        fboTextureArray[0][0] = generatedTextures.get(1);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[0][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 1 - Horizontal
        fboTextureArray[0][1] = generatedTextures.get(2);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[0][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 1 - Vertical
        fboTextureArray[0][2] = generatedTextures.get(9);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[0][2]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w, h, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Vertical
        fboTextureArray[1][0] = generatedTextures.get(3);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[1][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/2, h/2, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Horizontal
        fboTextureArray[1][1] = generatedTextures.get(4);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[1][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/2, h/2, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Vertical
        fboTextureArray[2][0] = generatedTextures.get(5);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[2][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/4, h/4, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 2 - Horizontal
        fboTextureArray[2][1] = generatedTextures.get(6);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[2][1]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/4, h/4, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 3 - Vertical
        fboTextureArray[3][0] = generatedTextures.get(7);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[3][0]);
        gl.glTexImage2D(GL3bc.GL_TEXTURE_2D, 0, GL3bc.GL_RGBA16F, w/8, h/8, 0, GL3bc.GL_RGBA, GL3bc.GL_UNSIGNED_BYTE, null);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_S, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_WRAP_T, GL3bc.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MAG_FILTER, GL3bc.GL_LINEAR);
        gl.glTexParameteri(GL3bc.GL_TEXTURE_2D, GL3bc.GL_TEXTURE_MIN_FILTER, GL3bc.GL_LINEAR);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, 0);
        
        //TEXTURE: Bloom - Level 3 - Horizontal
        fboTextureArray[3][1] = generatedTextures.get(8);
        gl.glBindTexture(GL3bc.GL_TEXTURE_2D, fboTextureArray[3][1]);
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
        
        //return if we would have division by zero
        if(glWindow.getHeight() == 0)
        {
            this.currentAspectRatio = ASPECT_RATIO_4_3;
            return;
        }
        
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
       switch(indexOfClosest)
       {
           case 0: Log.info("Detected Aspect Ratio 16:9"); break;
           case 1: Log.info("Detected Aspect Ratio 16:10"); break;
           case 2: Log.info("Detected Aspect Ratio 5:4"); break;
           case 3: Log.info("Detected Aspect Ratio 4:3"); break;
       }
                      
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
