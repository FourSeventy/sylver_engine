#version 130

uniform sampler2D s_texture;
uniform float u_threshold;

varying vec2 v_texCoord;


void main() 
{
    vec4 color = max( texture2D(s_texture, v_texCoord) - u_threshold, vec4(0, 0, 0, 0));
    gl_FragColor = color;
}
