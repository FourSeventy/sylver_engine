package com.silvergobletgames.sylver.graphics;

public abstract class ImageParticleEmitter extends ParticleEmitter 
{

	//Texture to draw particles with
    protected Image image; 


    //========================
    // Constructors
    //========================

    public ImageParticleEmitter(Image image)
    {
    	super();

    	this.image = image;
    }




    //===================
    // Methods
    //===================

    /**
     * Draws each particle
     * @param gl2 
     */
    public void draw(GL2 gl2)
    {
    	
    	//=====================
        // Draw with a texture
        //=====================
        if(this.image != null)
        {
            //enable texturing and blending
            gl2.glEnable(GL2.GL_TEXTURE_2D);
            gl2.glEnable(GL2.GL_BLEND);
            gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl2.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            gl2.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);

            //clamp the texture
            Texture texture = image.getTexture();
            texture.bind(gl2);

            //render all the particles
            LinkedHashSet<Particle> updateList = new LinkedHashSet(particles);
            for(Particle particle: updateList)
            {                

                Color drawColor = particle.color;
                float drawWidth = image.getTexture().getWidth() * particle.scale;
                float drawHeight = image.getTexture().getHeight() * particle.scale;
                float x = particle.position.x;
                float y = particle.position.y;

                if (this.useRelativeParticles)
                {
                    x += this.position.x - particle.originalEmitterPosition.x;
                    y += this.position.y - particle.originalEmitterPosition.y;
                }
                
                //bind the draw color
                drawColor.bind(gl2);

                //draw the image
                gl2.glBegin(GL2.GL_QUADS);
                {
                    //bottom left
                    gl2.glTexCoord2d(0, 1);
                    gl2.glVertex2f(x - drawWidth/2, y - drawHeight/2); 

                    //bottom right
                    gl2.glTexCoord2d(1, 1);
                    gl2.glVertex2f(x + drawWidth/2, y - drawHeight/2);  

                    //top right
                    gl2.glTexCoord2d(1, 0);
                    gl2.glVertex2f(x + drawWidth/2, y + drawHeight/2);  

                    //top left    
                    gl2.glTexCoord2d(0, 0);
                    gl2.glVertex2f(x - drawWidth/2, y + drawHeight/2);          
                }
                gl2.glEnd();    
            }


            //reset bound color to white
            Color.white.bind(gl2);

            //disable texture and blend modes
            gl2.glDisable(GL2.GL_TEXTURE_2D);
            gl2.glDisable(GL2.GL_BLEND);    
        }
    }






    //====================
    // Accessor Methods
    //====================

      /**
     * Get the image that this emitter uses for its particles
     * @return 
     */
    public Image getImage()
    {
        return this.image;
    }
    
    /**
     * Sets the image that this emitter uses for its particles
     * @param image 
     */
    public void setImage(Image image)
    {
        this.image = image;
    }









}