
package com.silvergobletgames.sylver.graphics;

import com.silvergobletgames.sylver.util.SylverRandom;
import java.util.Random;
import com.silvergobletgames.sylver.util.SylverVector2f;


public class ConcreteParticleEmitters {
    
    
    public static class SparkEmitter extends PointParticleEmitter
    {

        public SparkEmitter()
        {
            super(new Color(Color.white),1);
        }
    
        public PointParticleEmitter.Particle buildParticle()
        {
            Random rand = SylverRandom.random;
            SylverVector2f pos = new SylverVector2f(this.getPosition().x, this.getPosition().y);
            float randomedAngle = getAngle() + (rand.nextFloat() - .5f) * 90;
            SylverVector2f velocity = new SylverVector2f((float)Math.random() *5 * (float)Math.cos(randomedAngle * Math.PI/180) , (float)Math.random()*5 * (float)Math.sin(randomedAngle * Math.PI/180));
            SylverVector2f acceleration = new SylverVector2f(0,-.1f);
            Color color = new Color(5f,.5f,.5f,1f);
            if(Math.random() < .5){
                color.r += 1f;
            }
            color.a = 1f;
            int ttl = 40 + (int)(Math.random()*20);
            return new Particle( pos, velocity, acceleration, color, .18f, -.1f/ttl, ttl);
        }       
    }
}
