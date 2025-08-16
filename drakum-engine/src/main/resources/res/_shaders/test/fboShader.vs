#version 400 core


in vec3 vertexPos;
in vec2 texCoords;
in vec3 normal;
in vec3 tangent;

uniform mat4 T_model;
uniform mat4 T_projection;
uniform mat4 T_view;

out vec2 pass_texCoords;

void main()
{
	gl_Position = T_projection * T_view * T_model * vec4(vertexPos, 1.0);
	
	pass_texCoords = texCoords;
}