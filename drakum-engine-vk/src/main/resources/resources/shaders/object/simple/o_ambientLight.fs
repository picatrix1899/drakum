#version 400 core

#include embeded base "light.fsh"
#include embeded base "material.fsh"

in vec2 pass_texCoords;
in float pass_vis;

out vec4 out_Color;

uniform AmbientLight ambientLight;
uniform vec3 skyColor;

uniform Material material;

void main(void)
{

	vec4 totalLight = calcAmbientLight(ambientLight);

	out_Color = texture(material.albedoMap, pass_texCoords) * totalLight;
//	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}