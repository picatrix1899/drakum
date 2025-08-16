#version 400 core


#include embeded base "base.fsh"
#include embeded base "material.fsh"

in vec2 pass_texCoords;

uniform Material material;

void main()
{
	out_Color = texture(material.albedoMap, pass_texCoords);
}