#version 400 core

in vec2 pass_texCoords;

layout(location=0) out vec4 out_Color;

uniform sampler2D frame;

uniform float near;
uniform float far;


float LinearizeDepth(float depth)
{
    float z = depth * 2.0 - 1.0; // Back to NDC 
    return (2.0 * near * far) / (far + near - z * (far - near));
}

void main()
{             
    float depthValue = texture(frame, pass_texCoords).r;
    out_Color = vec4(vec3(LinearizeDepth(depthValue) / far), 1.0); // perspective
    // FragColor = vec4(vec3(depthValue), 1.0); // orthographic
}  