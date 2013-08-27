#version 130

uniform sampler2D s_texture0;
uniform sampler2D s_texture1;
uniform sampler2D s_texture2;
uniform sampler2D s_texture3;

varying vec2 v_texCoord;


void main() 
{
    vec4 color0 = texture2D(s_texture0, v_texCoord);
    vec4 color1 = texture2D(s_texture1, v_texCoord);
    vec4 color2 = texture2D(s_texture2, v_texCoord);
    vec4 color3 = texture2D(s_texture3, v_texCoord);
    gl_FragColor = color0 + color1 + color2 + color3;
}
