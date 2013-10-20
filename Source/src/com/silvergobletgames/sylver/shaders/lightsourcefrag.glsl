#version 130

uniform vec2 u_lightPos;
uniform vec4 u_lightColor;
uniform float u_lightRange;
uniform float u_lightIntensity;

varying vec2 v_pixelPos;


void main() 
{
      vec2 dxy = u_lightPos - v_pixelPos;
      float t = 1.0 - sqrt(dot(dxy, dxy)) / u_lightRange;

      // Enable this line if you want sigmoid function on the light interpolation
      t = 1.0 / (1.0 + exp(-(t*12.0 - 6.0)));

      gl_FragColor = u_lightIntensity * vec4(u_lightColor.r,u_lightColor.g,u_lightColor.b,u_lightColor.a) * t;	
   
}
