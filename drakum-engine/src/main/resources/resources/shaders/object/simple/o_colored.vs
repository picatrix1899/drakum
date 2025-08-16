#version 400 core


#include embeded base "base.vsh"

uniform mat4 T_view;

void main()
{
	gl_Position = T_projection * T_view * T_model * vec4(vertexPos, 1.0);
}