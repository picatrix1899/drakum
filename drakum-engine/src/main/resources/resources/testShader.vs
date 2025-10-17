#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoords;

out vec2 pass_TexCoords;

uniform mat4 m_proj;
uniform mat4 m_view;

void main()
{
    gl_Position = m_proj * m_view * vec4(aPos.x, aPos.y, aPos.z, 1.0);
    
    pass_TexCoords = aTexCoords;
}