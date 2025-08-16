#version 330

in vec2 position;
in vec2 textureCoords;

out vec2 pass_textureCoords;

uniform vec2 translation;
uniform vec2 screenSpace;

void main(void)
{
	vec2 v = position;
	
	 v.y = screenSpace.y - v.y;
	
	vec2 halfScreen = screenSpace * vec2(0.5f, 0.5f);
	
	v -= halfScreen;
	v /= halfScreen;
	
	
	
	gl_Position = vec4(v, 0.0f, 1.0f);
	pass_textureCoords = textureCoords;
	
}