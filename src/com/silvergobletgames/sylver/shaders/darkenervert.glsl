#version 130

varying vec2 v_texCoord;

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    v_texCoord = vec2(gl_MultiTexCoord0);
}

