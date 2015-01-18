package com.silvergobletgames.sylver.audio;

/**
 * The sound class represents a sound that will be played in the game. This class
 * contains all of the information about the sound that is needed.
 * @author Mike
 */
public class Sound 
{
          
    public SoundType type;
    
    public String ref; 

    public float x;
    public float y;
   
    public String name = "";
    public boolean priority = false;
    public float pitchValue =1;
    public float volume = 1;
    public float inmillis;
    public float outmillis;
    public boolean loop = false;

    public static enum SoundType{
            PlayAmbient, PlayLocation, NewBGM, BGMFadeInOut, AdjustSourceVolume          
        }

              
        //===============
        // Constructors
        //===============
        
        protected Sound()
        {
            
        }
        
        /**
        * 
        * Constructs an ambient sound
        * @param ref path of the sound that is relative to the path registered in GameConfiguration
        * @param priority if this sound has priority
        */
        public static Sound ambientSound(String ref, boolean priority)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.type = SoundType.PlayAmbient;
            sound.priority = priority;
            return sound;
        }
        
        /**
         * Constructs a sound that will play at the given location
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @param x x position
         * @param y y position
         * @param priority if tihs sound has priority
         * @return 
         */
        public static Sound locationSound(String ref, float x, float y, boolean priority)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.x = x;
            sound.y = y;
            sound.type = SoundType.PlayLocation;
            sound.priority = priority;
            return sound;
        }
        
        /**
         * Constructs a sound that will play at the given location
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @param x x position
         * @param y y position
         * @param priority if this sound has priority
         * @param volume the volume this will play at
         * @return 
         */
        public static Sound locationSound(String ref, float x, float y, boolean priority, float volume)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.x = x;
            sound.y = y;
            sound.type = SoundType.PlayLocation;
            sound.priority = priority;
            sound.volume = volume;
            return sound;
        }
        
        /**
         * Constructs a sound that will play at the given location
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @param x x position
         * @param y y position
         * @param priority if this sound has priority
         * @param volume the volume this will play at
         * @param pitch the pitch this will play at
         * @return 
         */
        public static Sound locationSound(String ref, float x, float y, boolean priority, float volume, float pitch)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.x = x;
            sound.y = y;
            sound.type = SoundType.PlayLocation;
            sound.priority = priority;
            sound.volume = volume;
            sound.pitchValue = pitch;
            return sound;
        }
        
        /**
         * Constructs a sound that will play at the given location
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @param x x position
         * @param y y position
         * @param priority if this sound has priority
         * @param volume the volume this will play at
         * @param pitch the pitch this will play at
         * @return 
         */
        public static Sound locationSound(String ref, float x, float y, boolean priority, float volume, float pitch, boolean loop)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.x = x;
            sound.y = y;
            sound.type = SoundType.PlayLocation;
            sound.priority = priority;
            sound.volume = volume;
            sound.pitchValue = pitch;
            sound.loop = loop;
            return sound;
        }

        /**
         * Constructs a new background music sound.
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @return 
         */
        public static Sound newBGM(String ref)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.type = SoundType.NewBGM;
            return sound;
        }
        
        /**
         * Changes the background music to the given ref, while fading in and out
         * @param ref path of the sound that is relative to the path registered in GameConfiguration
         * @param fadeOutMillis fade out time
         * @param fadeInMillis fade in time
         * @return 
         */
        public static Sound changeBGM(String ref, float fadeOutMillis, float fadeInMillis)
        {
            Sound sound = new Sound();
            sound.ref = ref;
            sound.type = SoundType.BGMFadeInOut;
            sound.inmillis = fadeInMillis;
            sound.outmillis = fadeOutMillis;
            return sound;
        }

        /**
        * adjusts the volume of an already playing source
        * @param source
        * @param volume
        * @return 
        */
        public static Sound adjustSourceVolume(String source, float volume)
        {
             Sound data = new Sound();
             data.type = SoundType.AdjustSourceVolume;
             data.ref = source;
             data.volume = volume;
             return data;
        }
        
}
