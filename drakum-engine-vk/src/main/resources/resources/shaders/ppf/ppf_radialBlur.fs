#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;

uniform float amplitude; // 0.5
uniform int cycles; // 20
uniform float delta; // 0.992

void main()
{

	vec2 tex = pass_texCoords - 0.5f;
	
	vec4 v = texture(textureMap, 0.5f + tex);

	for(int i = 0; i < cycles; i++)
	{
		tex *= delta;
		v += texture(textureMap, 0.5f + tex)*exp(-i * amplitude);
	}
	out_Color =  v;
}