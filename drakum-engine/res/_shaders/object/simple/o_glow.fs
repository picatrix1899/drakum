#version 400 core

#include "base.fsh"
#include "light.fsh"

in vec2 pass_texCoords;
in float pass_vis;

uniform Glow glow;
uniform vec3 skyColor;
uniform sampler2D glowMap;

void main(void)
{
	vec4 gloww = texture(glowMap, pass_texCoords);

	out_Color = calcGlow(glow, gloww);
	
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}