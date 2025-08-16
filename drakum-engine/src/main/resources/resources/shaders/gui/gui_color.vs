#version 400 core

in vec2 vertexPos;

uniform vec2 screenSpace;

void main()
{
	vec2 v = vertexPos;
	
	v.y = screenSpace.y - v.y;
	
	vec2 halfScreen = screenSpace * vec2(0.5f, 0.5f);
	
	v -= halfScreen;
	v /= halfScreen;
	
	gl_Position = vec4(v, 0.0f, 1.0f);
}