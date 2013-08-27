#version 130

uniform vec4 color;

varying vec2 v_texCoord;

uniform sampler2D s_texture;

uniform bool u_cullAlpha = true;

void main()
{
    vec4 texel = texture2D(s_texture, v_texCoord) * color;

    //discard fragments that are below alpha threshold
    if(u_cullAlpha)
    {
        if(texel.a < 0.1)
            discard;
    }
    else
    {
        if(texel.a == 0)
            discard;
    }

    gl_FragColor =  texel;

}