#version 400 core

#include "light.fsh"

out vec4 out_Color;
in vec2 pass_texCoords;

uniform vec3 cameraPos;
uniform vec3 skyColor;

uniform PointLight lights[4];

uniform sampler2D position;
uniform sampler2D normal;
uniform sampler2D albedo;
uniform sampler2D specular;


void main(void)
{
	vec4 diffuse = texture(albedo, pass_texCoords);
	vec4 nrm = texture(normal, pass_texCoords);
	vec4 pos = texture(position, pass_texCoords);
	
	PointLight l;
	vec3 dir;
	vec4 lightColor;
	float atten;
	vec4 color;
	
	for(int i = 0; i < 4; i++)
	{
		l = lights[i];
		dir = pos.xyz - l.position;
		//atten = calcAttenuation(l.attenuation,length(dir));
		
		lightColor = calcPointLight(l, pos.xyz, nrm.xyz);
		atten = 1 / (calcBrightness(lightColor) + (l.base.intensity / 10));
		
		//lightColor += calcSpecularReflection(l.base, dir, cameraPos, pos.xyz, nrm, specularIntensity, specularPower, atten);
		color += lightColor;
	}

	out_Color = diffuse * color;
	//out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}