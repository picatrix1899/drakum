#version 400 core

in vec2 vertexPos;
in vec2 texCoords;

out vec2 pass_texCoords;
out vec2 pass_pos;

uniform vec2 screenSpace;

void main()
{
	vec2 v = vertexPos;
	
	pass_pos = v;
	
	v.y = screenSpace.y - v.y;
	
	vec2 halfScreen = screenSpace * vec2(0.5f);
	
	v -= halfScreen;
	v /= halfScreen;
	
	gl_Position = vec4(v, 0.0f, 1.0f);
	
	pass_texCoords = texCoords * 1.0f;
}