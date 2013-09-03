package com.silvergobletgames.sylver.graphics;

public abstract class PointParticleEmitter extends ParticleEmitter 
{

    //particle color
	protected Color particleColor; 

    //particle size
    protected int particleSize;


    //================
    // Constructor
    //================

    public PointParticleEmitter(Color color, int size)
    {
    	super();

    	this.particleColor = color;
    	this.particleSize = size;
    }




    //================
    // Methods
    //================

    /**
     * Draws each particle
     * @param gl2 
     */
    public void draw(GL2 gl2)
    {
    	//=====================
        // Draw with GLPoints
        //=====================
        
        //make sure texturing is disabled
        gl2.glDisable(GL2.GL_TEXTURE_2D);

        //render all the particles
        for(Particle p: particles)
        { 
        	float x = p.position.x;
            float y = p.position.y;

            if (this.useRelativeParticles)
            {
                x += this.position.x - particle.originalEmitterPosition.x;
                y += this.position.y - particle.originalEmitterPosition.y;
            }
           
            gl2.glPointSize(this.particleSize);
            
            //bind the draw color
            Color drawColor = p.color;  
            drawColor.bind(gl2);
            
            gl2.glBegin(GL2.GL_POINTS);
            {
                gl2.glVertex2f(x,y);
            }
            gl2.glEnd();
            
            //reset bound color to white
            Color.white.bind(gl2);
        }
        

    }





}