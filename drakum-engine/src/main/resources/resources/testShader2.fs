#version 330 core

in vec2 pass_TexCoords;

out vec4 FragColor;

//uniform sampler2D albedo;

void main()
{
    FragColor = vec4(1, 0, 1, 1); //texture2D(albedo, pass_TexCoords);
}