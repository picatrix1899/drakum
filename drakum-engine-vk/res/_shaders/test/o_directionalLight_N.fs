#version 400 core

#include "base.fsh"
#include "light.fsh"

in vec2 pass_texCoords;
in vec3 pass_normal;
in vec3 pass_worldPos;
in mat3 pass_tbn;
in float pass_vis;

uniform DirectionalLight directionalLight;
uniform vec3 cameraPos;
uniform float specularPower;
uniform float specularIntensity;
uniform vec3 skyColor;

uniform sampler2D normalMap;


void main(void)
{
	vec4 textureColor = texture(textureMap, pass_texCoords);
	
	vec3 nrm = normalize(pass_tbn * ((2 * texture(normalMap, pass_texCoords).rgb) - 1));
	
	vec4 dLight = calcDirectionalLight(directionalLight.base, directionalLight.direction, nrm);
	
	float atten = 1 / (calcBrightness(dLight) + (directionalLight.base.intensity / 10));
	
	dLight += calcSpecularReflection(directionalLight.base, directionalLight.direction, cameraPos, pass_worldPos, nrm, specularIntensity, specularPower, atten);

	out_Color = textureColor * dLight;
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}