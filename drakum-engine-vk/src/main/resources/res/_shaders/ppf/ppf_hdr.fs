#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;
uniform float exposure;

void main()
{
	const float gamma = 2.2;
	vec3 hdrColor = texture(textureMap, pass_texCoords).rgb;

	vec3 mapped = vec3(1.0) - exp(-hdrColor * exposure);
	
	//mapped = pow(mapped, vec3(1.0/gamma));

	out_Color = vec4(mapped, 1.0);
}