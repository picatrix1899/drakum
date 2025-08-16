#version 400 core

in vec2 pass_texCoords;
in vec2 pass_pos;

out vec4 out_Color;

uniform sampler2D textureMap;

void main()
{
	out_Color = texture(textureMap, pass_texCoords);
}