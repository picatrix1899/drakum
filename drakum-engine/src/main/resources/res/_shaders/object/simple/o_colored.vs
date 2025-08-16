#version 400 core


#include "base.vsh"

void main()
{
	gl_Position = T_projection * T_view * T_model * vec4(vertexPos, 1.0);
}