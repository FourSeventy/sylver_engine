package com.silvergobletgames.sylver.core;


import com.silvergobletgames.sylver.audio.AudioRenderer;
import com.silvergobletgames.sylver.graphics.OpenGLGameWindow;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JWindow;

public final class Game
{    
    //private instance
    private static Game instance;
    
    //game configuration
    private GameConfiguration gameConfiguration;
    
    //Map of currently loaded scenes
    private ConcurrentHashMap<Class,Scene> scenes = new ConcurrentHashMap<>();
    //current game scene
    private Class currentScene;
    
    //map of runnables that the game can also manage
    private ConcurrentHashMap<String,Runnable> runnableMap = new ConcurrentHashMap<>();

    //event queue for scene changes etc
    private ConcurrentLinkedQueue<SceneAction> sceneActionQueue = new ConcurrentLinkedQueue<>();
    
    //system exit actions
    private ArrayList<SystemExitAction> systemExitActions = new ArrayList<>();
       
    //asset manager
    private AssetManager assetManager;
    //openGL game window
    private OpenGLGameWindow graphicsWindow;
    //audio renderer
    private AudioRenderer audioRenderer;
    //input handler
    private InputHandler inputHandler;

    
    
    //game running variable
    private boolean gameRunning = true;
    //timing variables
    private long startOfLoopTime = 0;
    private long endOfLoopTime=0;
    private long lastFrameTime;
    //update accumulator variables
    private final long timestep = 16_666_667; //60hz     
    private long accumulator = 0;  

    
        
   

    //==================
    // Constructor
    //==================
    
    /**
     * Create a new instance of Game. For singleton use only.
     */
    private Game()
    {       
                             
    }
    
    /**
     * Sets up and configures game with the provided GameConfiguration. This must be called before you can begin
     * the game with gameLoop()
     * @param configuration GameConfiguration to set up this game instance with
     */
    public void createGame(GameConfiguration configuration)
    {
        //set game configuration
        this.gameConfiguration = configuration;
                       
        // if no game configuration is available, set a default configuration
        if(this.gameConfiguration == null)
        {       
            this.gameConfiguration = new GameConfiguration(URI.create("file:/C:"),URI.create("file:/C:"), new EngineSettings());           
        }
        
        //create input handler
        this.inputHandler = new InputHandler();
        
        //creates asset manager
        this.assetManager = new AssetManager();
        
        //create a new openGL window
        try
        {
            this.graphicsWindow = new OpenGLGameWindow();
            this.graphicsWindow.postInit();
        }
        catch(Exception e)
        {
            //log error to console
            Logger logger =Logger.getLogger(Game.class.getName());
            logger.log(Level.SEVERE, "Error Creating Graphics Window: " + e.toString());
            logger.addHandler(new ConsoleHandler()); 
            
            //exit
            this.uncaughtExceptionHandlingActions(e);
        }

        //init sound
        this.audioRenderer = new AudioRenderer(); 
               
    }
    
    /**
     * 
     * @return A reference to the game object
     */
    public static Game getInstance()
    {
       if(Game.instance == null)
           instance = new Game();
       
       return instance;
    }
    
   
    //==================
    // Game Loop
    //==================
    
    /**
     * The main game loop. This loop is running during all game
     * play as is responsible for the following activities:
     *
     * - Updates current scene
     * - Handles input for current scene
     * - Renders current scene
     * 
     *  Updates 60 times per second, renders 60 times per second, handles input at 60 times per second
     *  The loop goes around every 16.666 milliseconds, or 16,666,667 nanoseconds   
     */
    public void gameLoop()
    {
        try
        {
            //check to see if createGame() was called first
            if(this.gameConfiguration == null)
                throw new RuntimeException("Game must be created before you can call gameLoop()");

            //infinite loop
            while (gameRunning)
            {                

                //find out how long the last frame took
                lastFrameTime = endOfLoopTime - startOfLoopTime;
                //System.out.println("Game Loop: " +(float)(System.nanoTime() - startOfLoopTime)/1_000_000f);

                //save the start of the loop time
                startOfLoopTime = System.nanoTime();
                
                //proccess scene actions
                while(!this.sceneActionQueue.isEmpty())
                {
                    SceneAction sceneAction = this.sceneActionQueue.poll();
                    sceneAction.action();
                    
                }

                //if we have a scene to update
                if(currentScene != null)
                {
                    //get current scene
                    Scene scene = scenes.get(currentScene);

                    //===========================
                    // Timing accumulator (60hz)
                    //===========================
                    accumulator += lastFrameTime;
                    while(accumulator >= timestep)
                    {               

                        //=====================================
                        //sets the input snapshot for the scene
                        //=====================================
                        this.inputHandler.takeInputSnapshot();

                        //==========================
                        //updates the scene (60 hz)
                        //==========================
                        scene.update();                 


                        //=====================================
                        //handles input for the scene ( 60 hz)
                        //=====================================            
                        scene.handleInput();


                        //subtract from accumulator
                        accumulator -= timestep;  

                    }


                    //=================================
                    //tells the scene to render (60hz)  
                    //=================================
                    graphicsWindow.renderScene(scene); 
                }

                //note the current time
                endOfLoopTime = System.nanoTime();


                //flushing output streams for debugging  
                System.out.flush();
                System.err.flush();
                

                //sleep to set frame rate   
                while(endOfLoopTime-startOfLoopTime <= 14_666_666)
                {
                    try
                    { 
                        Thread.sleep(0,250_000);
                    } 
                    catch(java.lang.InterruptedException e)
                    {
                        //log error to console
                        Logger logger =Logger.getLogger(Game.class.getName());
                        logger.log(Level.SEVERE, "Error Sleeping: " + e.toString());
                        logger.addHandler(new ConsoleHandler()); 
                    }   
                    endOfLoopTime = System.nanoTime();
                }                       
                while (endOfLoopTime - startOfLoopTime <= 16_666_667)
                {              
                    Thread.yield();
                    endOfLoopTime = System.nanoTime();
                }    

            }
        }
        catch(Exception e)
        {
            this.uncaughtExceptionHandlingActions(e);        
        }
   
        //perform system shutdown
        this.performSystemShutdown();
        
        
    }
    
    
    //=========================
    // Scene Management Methods
    //=========================
    
    /**
     * Changes the currently active scene. An active scene receives update(), render() and handleInput() calls.
     * This method will always return immediately. The scene change will be queued up until the current scene
     * is finished with its current tick.
     * @param scene The scene to switch to
     */
    public void changeScene(final Class scene,final ArrayList args)
    {
       SceneAction action = new SceneAction(SceneAction.ActionEnum.CHANGESCENE){
           
           public void action()
           {
               
                //exit old scene
                if (currentScene != null)
                    scenes.get(currentScene).sceneExited();

                //Enter the new scene
                currentScene = scene;
                scenes.get(currentScene).sceneEntered(args);

                //reset accumulator
                accumulator = 0;
               
           }
       };
       this.sceneActionQueue.add(action);
       
    }  
    
    /**
     * Loads a scene into memory. A scene must be loaded before it can become active
     * @param scene The scene to load
     */
    public void loadScene(final Scene scene) 
    {
     
        if (scenes.containsKey(scene.getClass()))
                 throw new RuntimeException("Scene map already contains this scene");
             else
                 scenes.put(scene.getClass(), scene);

    }
    
    /**
     * Unloads a scene from memory. If we are unloading the running scene, this method will still return immediately
     * but the unload will wait until the current scene is done with its tick.
     * @param scene Scene to unload
     */
    public void unloadScene(final Class scene) 
    {
       
        SceneAction action = new SceneAction(SceneAction.ActionEnum.CHANGESCENE){
           
           public void action()
           {
               //remove scene
                scenes.remove(scene);

                //set current scene to null if it is equal to the scene we unloaded
                if (scene == currentScene)
                    currentScene = null;
           }
       };
        this.sceneActionQueue.add(action);
        
           
    }
    
    /**
     * Checks to see if a scene is loaded
     * @param scene Scene to check
     * @return True if the scene is loaded, false otherwise.
     */
    public boolean isLoaded(Class scene) 
    {
        return scenes.containsKey(scene);
    }
    
    /**
     * Returns the currently active scene. The returned Scene isn't thread safe. You must implement
     * thread safety within the scene itself.
     * @return The scene that is currently running
     */
    public Scene getCurrentScene() 
    {
        return scenes.get(currentScene);
    }
    
    /**
     * Returns a reference to a particular scene. This method is used to get access to scenes that
     * are loaded, but are not the active scene. The returned Scene isn't thread safe. You must implement
     * thread safety within the scene itself.
     * @param scene Scene to get a reference to.
     * @return 
     */
    public Scene getScene(Class scene) 
    {
        return scenes.get(scene);
    }
    
    
    
    /**
     * Adds a runnable which can be managed and accessed from Game. This method will create a new Thread and start running
     * the runnable when it is called
     * @param key Name by which you can access the runnable, also the name of the Thrad it will run in
     * @param runnable Runnable to start running in a new thread
     */
    public void addRunnable(String key, Runnable runnable)
    {
        //adds it to the map
        this.runnableMap.put(key, runnable);
        
        //starts runnable
        Thread thread = new Thread(runnable);
        thread.setName(key);
        thread.setPriority(8);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){        
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                Game.getInstance().uncaughtExceptionHandlingActions(e);
            }
        });
        thread.start();
    }
    
    /**
     * Provides access to a runnable that Game is managing
     * @param key Reference key for the runnable
     * @return Runnable
     */
    public Runnable getRunnable(String key)
    {
        return this.runnableMap.get(key);
    }
    
    /**
     * Removes runnable from Games management. WARNING: this method will not stop the thread your runnable is in. 
     * Be sure to stop the thread before calling this method to avoid a memory leak.
     * @param key Reference key for the runnable to stop
     */
    public void removeRunnable(String key)
    {
        this.runnableMap.remove(key);
    }
    
    
    //==================
    // Class Methods
    //=================
      
    /**
     * Accessor to the OpenGLGameWindow. Not Thread safe.
     * @return OpenGLGameWindow
     */
    public OpenGLGameWindow getGraphicsWindow()
    {
        return this.graphicsWindow;
    }
    
    /**
     * Accessor to the AudioRenderer. Not Thread safe.
     * @return the AudioRenderer 
     */
    public AudioRenderer getAudioRenderer()
    {
        return this.audioRenderer;
    }
    
    /**
     * Accessor to the AssetManager. Not Thread safe.
     * @return the AssetManager 
     */
    public AssetManager getAssetManager()
    {      
        return this.assetManager;        
    }
    
    /**
     * Access to the GameConfiguration. Not Thread safe.
     * @return The GameConfiguration currently loaded for this game 
     */
    public GameConfiguration getConfiguration()
    {
        return this.gameConfiguration;
    }
    
    /**
     * Get a reference to the games InputHandler
     * @return The InputHandler registered for the Game
     */
    public InputHandler getInputHandler()
    {
        return this.inputHandler;
    }
    
    /**
     * Registers an action that will be performed upon system exit
     * @param action 
     */
    public void registerSystemExitAction(SystemExitAction action)
    {
        this.systemExitActions.add(action);
    }
    
    /**
     * Tells game to begin the shutdown process.
     */
    public void exitGame()
    {
        //tells game to stop looping   
        this.gameRunning = false;
        
    }
    
    /**
     * Method that provides the uncaught exception handling actions of showing an error dialogue, and exiting the game
     * @param e Exception that was thrown
     */
    public void uncaughtExceptionHandlingActions(Throwable e)
    {
        //get window position and size for centering error box
        int width = 0,height = 0;
        javax.media.nativewindow.util.Point windowLocation = new javax.media.nativewindow.util.Point();
        try{           
            this.getGraphicsWindow().glWindow.getLocationOnScreen(windowLocation);
            width =this.getGraphicsWindow().glWindow.getWidth();
            height = this.getGraphicsWindow().glWindow.getHeight();

            //hide glwindow
            this.getGraphicsWindow().glWindow.setVisible(false);
            this.getGraphicsWindow().glWindow.setPointerVisible(true);
        }catch(Exception ex){}
      

        //log error to console
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString(); // stack trace as a string

        Logger logger =Logger.getLogger(Game.class.getName());
        logger.log(Level.SEVERE, "GAME CRASH: {0}", e.toString() + " : " +stackTrace);
        logger.addHandler(new ConsoleHandler()); 
        
        try{
            //cleanup audio     
            this.audioRenderer.cleanupAudioRenderer();
        }
        catch(Exception ex){}
            

        //show error window
        JWindow frame = new JWindow();
        frame.setLocation(windowLocation.getX() + width/2, windowLocation.getY() + height/2);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        
        JOptionPane.showMessageDialog(frame, e.toString() + " : " +stackTrace);

        //shutdown
        try{
        this.performSystemShutdown();
        }
        catch(Exception ex){System.exit(1);}
    }
    
    /**
     * Performs a system shutdown. Including all SystemExitActions registered with Game
     */
    private void performSystemShutdown()
    {
        //perform system exit actions
        for(SystemExitAction action: this.systemExitActions)
        {
            action.action();
        }
        
        //cleanup audio     
        this.audioRenderer.cleanupAudioRenderer();
        
        //system exit
        System.exit(0);
    }
    
    /**
     * Class that is designed to hold any system shutdown actions that need to be performed.
     * To use this class override the action() method;
     */
    public static abstract class SystemExitAction
    {
        //constructor
        public SystemExitAction()
        {
            
        }
        
        //action 
        public abstract void action();
    }


    /**
    * Class used to hold game actions that will be performed by Game
    *
    */
    private static abstract class SceneAction
    {
    	protected ActionEnum action;


    	//private enum used for
        private static enum ActionEnum
        {
            CHANGESCENE,UNLOADSCENE;
        }
        
        protected SceneAction(ActionEnum en)
        {
            this.action = en;
        }

    	public abstract void action();
     
    }
        
}


