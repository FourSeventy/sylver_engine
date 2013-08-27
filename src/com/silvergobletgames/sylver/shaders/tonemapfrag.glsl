uniform sampler2D sampler;
varying vec2 tex_coord;
 
// Max bright
uniform float bThresh;
 
float toneMap(float luminance){
    //This is the function in the article
    return luminance * ( float(1) + luminance / (bThresh*bThresh)) / (float(1) + luminance);
}
   
void main()
{
    vec4 color = texture2D(sampler, tex_coord);
    vec4 lumVect = vec4(0.2126, 0.7152, 0.0722, 0);
    float L = dot(lumVect, color);
    float nL = toneMap(L);
    float scale = nL / L;
    gl_FragColor = color * scale;
}


