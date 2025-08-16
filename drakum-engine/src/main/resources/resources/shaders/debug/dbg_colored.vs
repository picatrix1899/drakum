#version 400 core

in vec3 vertexPos;

uniform mat4 T_model;
uniform mat4 T_projection;
uniform mat4 T_view;

void main()
{
    gl_Position = T_projection * T_model * vec4(vertexPos, 1.0);
}