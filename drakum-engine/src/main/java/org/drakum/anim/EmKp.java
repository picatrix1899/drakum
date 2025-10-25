package org.drakum.anim;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL46C.*;

public class EmKp
{
	public FloatBuffer buffer;

	private FloatBuffer uploadBoneMatrices(List<Bone> bones)
	{
		if (buffer == null || buffer.capacity() < bones.size() * 16)
			buffer = BufferUtils.createFloatBuffer(bones.size() * 16);
		else
			buffer.clear();

		for (int i = 0; i < bones.size(); i++)
		{
			bones.get(i).finalMatrix.get(i * 16, buffer);
		}

		buffer.position(bones.size() * 16);
		buffer.flip();
		return buffer;
	}

	public int boneSSBO;

	public void createSSBO(int boneCount)
	{
		boneSSBO = glGenBuffers();
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, boneSSBO);
		glBufferData(GL_SHADER_STORAGE_BUFFER, boneCount * 16L * Float.BYTES, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, boneSSBO);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
	}

	public void updateSSBO(List<Bone> bones)
	{
		FloatBuffer fb = uploadBoneMatrices(bones);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, boneSSBO);
		glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, fb);
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
	}
}
