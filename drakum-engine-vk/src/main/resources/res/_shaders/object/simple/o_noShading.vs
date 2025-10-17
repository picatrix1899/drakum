#version 400 core


#include "baseObject.vsh"

out vec2 pass_texCoords;

void main()
{
	gl_Position = T_projection * camera.T_view * T_model * vec4(vertexPos, 1.0);
	
	pass_texCoords = texCoords;
}