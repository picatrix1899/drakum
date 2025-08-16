#version 400 core

#include "baseObject.vsh"

out vec2 pass_texCoords;
out vec3 pass_normal;
out vec3 pass_worldPos;
out float pass_vis;
out Camera pass_camera;

const float density = 0.02;
const float gradient = 8;

void main(void)
{

	vec4 posRelativeToCam = camera.T_view * T_model * vec4(vertexPos, 1.0);
	gl_Position = T_projection * posRelativeToCam;
	

	
	pass_texCoords = texCoords * 80.0f;
	pass_normal = normal;
	pass_worldPos = (T_model * vec4(vertexPos, 1.0)).xyz;
	
	float dist = length(posRelativeToCam.xyz);
	pass_vis = exp(-pow((dist * density), gradient));
	pass_vis = clamp(pass_vis, 0.0, 1.0);
	pass_camera = camera;
}