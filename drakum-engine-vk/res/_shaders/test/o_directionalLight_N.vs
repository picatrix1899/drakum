#version 400 core

#include "base.vsh"

out vec2 pass_texCoords;
out vec3 pass_normal;
out vec3 pass_worldPos;
out mat3 pass_tbn;
out float pass_vis;

const float density = 0.02;
const float gradient = 40;

void main(void)
{
	vec4 posRelativeToCam = T_view * T_model * vec4(vertexPos, 1.0);
	gl_Position = T_projection * posRelativeToCam;
	
	pass_texCoords = texCoords;
	pass_normal = normal;
	pass_worldPos = (T_model * vec4(vertexPos, 1.0)).xyz;
	
	vec3 n = normalize(T_model * vec4(normal, 0.0f)).xyz;
	vec3 t = normalize(T_model * vec4(tangent, 0.0f)).xyz;
	
	t = normalize(t - dot(t,n) * n);
	
	vec3 b = cross(t,n);
	
	pass_tbn = mat3(t,b,n);
	float dist = length(posRelativeToCam.xyz);
	pass_vis = exp(-pow((dist * density), gradient));
	pass_vis = clamp(pass_vis, 0.0, 1.0);
}