#version 400 core

#include embeded base "light.fsh"
#include embeded base "camera.sh"
#include embeded base "material.fsh"

in vec2 pass_texCoords;
in vec3 pass_normal;
in vec3 pass_worldPos;
in mat3 pass_tbn;
in float pass_vis;
in Camera pass_camera;

out vec4 out_Color;

uniform vec3 skyColor;

uniform PointLight light;

uniform Material material;

void main(void)
{
	vec4 textureColor = texture(material.albedoMap, pass_texCoords);
	
	vec3 nrm = normalize(pass_tbn * ((2 * texture(material.normalMap, pass_texCoords).rgb) - 1));
	
		vec3 dir = pass_worldPos - light.position;
		//atten = calcAttenuation(light.attenuation,length(dir));
		
		vec4 lightColor = calcPointLight(light, pass_worldPos, nrm);
		float atten = 1 / (calcBrightness(lightColor) + (light.base.intensity / 10));
		
		lightColor += calcSpecularReflection(light.base, dir, pass_camera.position, pass_worldPos, nrm, specularIntensity, specularPower, atten);
		vec4 color += lightColor;
	}

	out_Color = textureColor * color;
	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}