#version 130


varying vec2 v_pixelPos;

void main()
{
    v_pixelPos = gl_Vertex.xy;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}