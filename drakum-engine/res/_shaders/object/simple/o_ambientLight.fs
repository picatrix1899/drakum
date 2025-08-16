#version 400 core


#include "base.fsh"
#include "light.fsh"

in vec2 pass_texCoords;
in float pass_vis;

uniform AmbientLight ambientLight;
uniform vec3 skyColor;

void main(void)
{
	vec4 totalLight = calcAmbientLight(ambientLight);

	out_Color = texture(textureMap, pass_texCoords) * totalLight;
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}