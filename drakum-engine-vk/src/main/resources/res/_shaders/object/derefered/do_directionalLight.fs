#version 400 core

#include "light.fsh"

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform DirectionalLight directionalLight;
uniform vec3 cameraPos;


uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D albedo;
uniform sampler2D specular;

void main()
{
	vec3 nrm = texture(normal, pass_texCoords).rgb;
	vec4 color = texture(albedo, pass_texCoords);
	vec3 pos = texture(position, pass_texCoords).rgb;
	vec2 spec = texture(specular, pass_texCoords).rg;
	
	vec4 dLight = calcDirectionalLight(directionalLight.base, directionalLight.direction, nrm);
	
	float atten = 1 / (calcBrightness(dLight) + (directionalLight.base.intensity / 10));
	
	dLight += calcSpecularReflection(directionalLight.base, directionalLight.direction, cameraPos, pos, nrm, spec.x, spec.y, atten);
	
	out_Color = color * dLight;
}