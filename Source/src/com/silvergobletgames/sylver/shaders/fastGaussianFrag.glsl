#version 130

//precision lowp float; //change to lowp to get blazing fast shader but with some glitches may occur

uniform sampler2D s_texture;

varying vec2 v_texCoords0;
varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying vec2 v_texCoords3;
varying vec2 v_texCoords4;

const float center = 0.2270270270; 
const float close = 0.3162162162;
const float far = 0.0702702703;

void main()
{	 
   gl_FragColor.rgb = far * texture2D(s_texture, v_texCoords0).rgb
                    + close * texture2D(s_texture, v_texCoords1).rgb
                    + center * texture2D(s_texture, v_texCoords2).rgb
                    + close * texture2D(s_texture, v_texCoords3).rgb
                    + far * texture2D(s_texture, v_texCoords4).rgb; 
}