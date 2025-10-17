#version 400 core

in vec2 pass_texCoords[11];

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;

void main()
{
	out_Color = vec4(0.0);
	out_Color += texture(textureMap, pass_texCoords[0]) * 0.0093;
	out_Color += texture(textureMap, pass_texCoords[1]) * 0.028002;
	out_Color += texture(textureMap, pass_texCoords[2]) * 0.065984;
	out_Color += texture(textureMap, pass_texCoords[3]) * 0.121703;
	out_Color += texture(textureMap, pass_texCoords[4]) * 0.175713;
	out_Color += texture(textureMap, pass_texCoords[5]) * 0.198596;
	out_Color += texture(textureMap, pass_texCoords[6]) * 0.175713;
	out_Color += texture(textureMap, pass_texCoords[7]) * 0.121703;
	out_Color += texture(textureMap, pass_texCoords[8]) * 0.065984;
	out_Color += texture(textureMap, pass_texCoords[9]) * 0.028002;
	out_Color += texture(textureMap, pass_texCoords[10]) * 0.0093;
}