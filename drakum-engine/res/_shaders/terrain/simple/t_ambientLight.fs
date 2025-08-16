#version 400 core


#include "base.fsh"

in vec2 pass_texCoords;
in float pass_vis;

uniform vec3 ambientColor;
uniform float ambientIntensity;
uniform vec3 skyColor;

void main()
{
	vec4 totalLight = (vec4(ambientColor,1.0) * ambientIntensity);

	out_Color = texture(textureMap, pass_texCoords) * totalLight;
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}