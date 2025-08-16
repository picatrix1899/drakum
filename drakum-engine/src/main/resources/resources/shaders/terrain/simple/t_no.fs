#version 400 core


#include embeded base "base.fsh"

in vec2 pass_texCoords;

void main()
{
	out_Color = texture(textureMap, pass_texCoords);
}