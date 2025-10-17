#version 400 core

layout(location=0) out vec4 r;
layout(location=1) out vec4 albedo;
layout(location=2) out vec3 b;


uniform sampler2D textureMap;

in vec2 pass_texCoords;

void main()
{
	albedo = texture(textureMap, pass_texCoords);
	r.rgb = vec3(albedo.r);
	b.rgb = vec3(albedo.b);
}