#version 130

varying vec2 v_texCoord;

uniform sampler2D s_texture;

void main()
{
    vec4 texel = texture2D(s_texture, v_texCoord);

    //discard fragments that are below alpha threshold
    if(texel.a < 0.1)
        discard;
   
    gl_FragColor =  texel;

}