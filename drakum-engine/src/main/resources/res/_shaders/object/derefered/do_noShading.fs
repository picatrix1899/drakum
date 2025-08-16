#version 400 core

#include "light.fsh"

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform AmbientLight ambientLight;

uniform sampler2D albedo;

void main()
{
	out_Color = texture(albedo, pass_texCoords);
}