#version 400 core

in vec2 pass_texCoords;
in vec2 pass_pos;

out vec4 out_Color;

uniform sampler2D textureMap;
uniform int time;


void main()
{

	vec4 o = texture(textureMap, pass_texCoords);

	out_Color = o;
}