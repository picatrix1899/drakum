#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D textureMap;
uniform int height;
uniform float stripWidth;
uniform float intensity;

void main()
{
	
	float f = 1.0 / height * stripWidth;

	out_Color = texture(textureMap, pass_texCoords);
	
	out_Color += mod(pass_texCoords.y, f) * intensity;
}