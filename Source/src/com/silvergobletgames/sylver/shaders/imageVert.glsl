# version 130

attribute vec2 a_position;
attribute vec2 a_texCoord;

uniform vec4 color;

varying vec2 v_texCoord;


void main()
{
    v_texCoord = a_texCoord;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(a_position,0,1);
}