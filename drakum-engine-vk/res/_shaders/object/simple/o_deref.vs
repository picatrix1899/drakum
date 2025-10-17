#version 400 core

#include "baseObject.vsh"

out vec2 pass_texCoords;
out vec3 pass_pos;
out vec3 pass_normal;
out mat3 pass_tbn;

void main()
{
	gl_Position = T_projection * camera.T_view * T_model * vec4(vertexPos, 1.0);
	
	pass_texCoords = texCoords;
	pass_pos = (T_projection * camera.T_view * T_model * vec4(vertexPos, 1.0)).xyz;
	
	vec3 n = normalize(T_model * vec4(normal, 0.0f)).xyz;
	vec3 t = normalize(T_model * vec4(tangent, 0.0f)).xyz;
	
	t = normalize(t - dot(t,n) * n);
	
	vec3 b = cross(t,n);
	
	pass_tbn = mat3(t,b,n);
}