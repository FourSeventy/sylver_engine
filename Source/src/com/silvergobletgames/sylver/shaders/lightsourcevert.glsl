#version 130

attribute vec2 a_position;

varying vec2 v_pixelPos;

void main()
{
    v_pixelPos = a_position;
    gl_Position = gl_ModelViewProjectionMatrix * vec4(a_position, 0.0, 1.0);	
}