package org.drakum.anim;

import static org.lwjgl.opengl.GL46C.*;

public class SkinnedMesh
{
	public final float[] positions;
	public final float[] normals;
	public final float[] texcoords;
	public final int[] indices;

	public int vao;
	public int vboVertices;
	public int vboNormals;
	public int vboUVs;
	public int vboBoneIds;
	public int vboWeights;
	public int ebo;
	public int indexCount;

	// Bone-Influence-Daten (für Skinning)
	public final int[] boneIds; // 4 × numVertices
	public final float[] weights; // 4 × numVertices

	public final int vertexCount;

	public SkinnedMesh(float[] positions, float[] normals, float[] texcoords, int[] indices, int[] boneIds, float[] weights, int vertexCount)
	{
		this.positions = positions;
		this.normals = normals;
		this.texcoords = texcoords;
		this.indices = indices;
		this.boneIds = boneIds;
		this.weights = weights;
		this.vertexCount = vertexCount;
	}

	public void uploadToGPU(float[] positions, float[] normals, float[] uvs, int[] boneIds, float[] weights, int[] indices)
	{

		indexCount = indices.length;

		vao = glGenVertexArrays();
		glBindVertexArray(vao);

		// Positions
		vboVertices = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
		glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);

		// UVs
		if (uvs != null)
		{
			vboUVs = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboUVs);
			glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(1);
		}

		// Normals
		vboNormals = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboNormals);
		glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(2);

		// Bone IDs (Integer Attribute!)
		vboBoneIds = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboBoneIds);
		glBufferData(GL_ARRAY_BUFFER, boneIds, GL_STATIC_DRAW);
		glVertexAttribIPointer(5, 4, GL_INT, 0, 0);
		glEnableVertexAttribArray(5);

		// Bone Weights
		vboWeights = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboWeights);
		glBufferData(GL_ARRAY_BUFFER, weights, GL_STATIC_DRAW);
		glVertexAttribPointer(6, 4, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(6);

		// Indices
		ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glBindVertexArray(0);
	}

	public void draw()
	{
		glBindVertexArray(vao);
		glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}
}
