#version 400 core

in vec2 pos;

out vec2 pass_texCoords;

void main()
{
	gl_Position = vec4(pos, 0.0, 1.0);
	
	pass_texCoords = pos * 0.5 + 0.5;
}