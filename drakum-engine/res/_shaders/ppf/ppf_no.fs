#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;

void main()
{
	out_Color += texture(textureMap, pass_texCoords);
}