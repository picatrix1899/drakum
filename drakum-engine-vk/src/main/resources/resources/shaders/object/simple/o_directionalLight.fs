#version 400 core

#include embeded base "light.fsh"
#include embeded base "camera.sh"
#include embeded base "material.fsh"

in vec2 pass_texCoords;
in vec3 pass_normal;
in vec3 pass_worldPos;
in float pass_vis;
in Camera pass_camera;

out vec4 out_Color;

uniform DirectionalLight directionalLight;
uniform vec3 skyColor;

uniform Material material;

void main(void)
{

	vec4 textureColor = texture(material.albedoMap, pass_texCoords);
	
	vec4 dLight = calcDirectionalLight(directionalLight.base, directionalLight.direction, pass_normal);
	
	float atten = 1 / (calcBrightness(dLight) + (directionalLight.base.intensity / 10));
	
	dLight += calcSpecularReflection(directionalLight.base, directionalLight.direction, pass_camera.position, pass_worldPos, pass_normal, material.specularIntensity, material.specularPower, atten);

	out_Color = textureColor * dLight;
//	out_Color = mix(vec4(skyColor,1.0), out_Color, pass_vis);
}