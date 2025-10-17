#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;

const float contrast = 0.8;

void main()
{
	
	out_Color = 1.0 - texture(textureMap, pass_texCoords);
}