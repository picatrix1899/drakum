#version 400 core

#include embeded base "base.fsh"
#include embeded base "light.fsh"
#include embeded base "camera.sh"

in vec2 pass_texCoords;
in vec2 pass_texCoordsA;
in vec3 pass_normal;
in vec3 pass_worldPos;
in mat3 pass_tbn;
in float pass_vis;
in Camera pass_camera;

uniform DirectionalLight directionalLight;
uniform vec3 cameraPos;
uniform float specularPower;
uniform float specularIntensity;
uniform vec3 skyColor;

uniform PointLight lights[4];

uniform sampler2D normalMap;

void main(void)
{
	vec4 textureColor = texture(textureMap, pass_texCoords);
	
	vec3 nrm = normalize(pass_tbn * ((2 * texture(normalMap, pass_texCoords).rgb) - 1));
	
	PointLight l;
	vec3 dir;
	vec4 lightColor;
	float atten;
	vec4 color;
	for(int i = 0; i < 4; i++)
	{
		l = lights[i];
		dir = pass_worldPos - l.position;
		//atten = calcAttenuation(l.attenuation,length(dir));
		
		lightColor = calcPointLight(l, pass_worldPos, nrm);
		atten = 1 / (calcBrightness(lightColor) + (l.base.intensity / 10));
		
		lightColor += calcSpecularReflection(l.base, dir, pass_camera.position, pass_worldPos, nrm, specularIntensity, specularPower, atten);
		color += lightColor;
	}

	out_Color = textureColor * color;
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}