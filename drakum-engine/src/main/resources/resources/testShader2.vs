#version 460 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoords;
layout (location = 5) in ivec4 boneIds;
layout (location = 6) in vec4 boneWeights;

layout(std430, binding = 3) buffer Bones {
    mat4 boneMatrices[];
};

out vec2 pass_TexCoords;

uniform mat4 m_proj;
uniform mat4 m_view;

void main()
{
	vec4 skinnedPos = vec4(0.0);
	
	for (int i = 0; i < 4; i++) {
		int id = boneIds[i];
    	float w = boneWeights[i];
    	
		if(w > 0) {
		
	   	 skinnedPos += (boneMatrices[id] * vec4(aPos, 1.0)) * w;
	   	 }
	}

    gl_Position = m_proj * m_view * skinnedPos;
    pass_TexCoords = aTexCoords;
}