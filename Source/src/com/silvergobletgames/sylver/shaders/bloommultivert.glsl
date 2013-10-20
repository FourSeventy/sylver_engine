#version 130

attribute vec2 a_texCoord;
attribute vec2 a_position;

varying vec2 v_texCoord;

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * vec4(a_position,0,1);
    v_texCoord = a_texCoord;
}

