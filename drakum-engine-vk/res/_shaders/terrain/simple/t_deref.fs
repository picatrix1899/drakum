#version 400 core

layout(location = 0) out vec3 gPosition;
layout(location = 1) out vec3 gNormal;
layout(location = 2) out vec4 gAlbedo;
layout(location = 3) out vec4 gGlow;
layout(location = 4) out vec4 gSpecular;

in vec2 pass_texCoords;
in vec3 pass_pos;
in mat3 pass_tbn;

uniform float specularIntensity;
uniform float specularPower;

uniform sampler2D textureMap;
uniform sampler2D normalMap;
uniform sampler2D glowMap;

void main()
{
	gPosition = pass_pos;
	gNormal = normalize(pass_tbn * ((2 * texture(normalMap, pass_texCoords).rgb) - 1));
	gAlbedo = texture(textureMap, pass_texCoords);
	gGlow = texture(glowMap, pass_texCoords);
	gSpecular = vec4(specularIntensity, specularPower, 1.0, 1.0);
}