#version 400 core

#include "light.fsh"

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform AmbientLight ambientLight;
uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D albedo;

void main()
{
	vec4 totalLight = calcAmbientLight(ambientLight);

	out_Color = texture(albedo, pass_texCoords) * totalLight;
}