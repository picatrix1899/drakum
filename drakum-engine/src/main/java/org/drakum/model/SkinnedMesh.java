package org.drakum.model;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.drakum.boilerplate.BufferObject;
import org.drakum.boilerplate.FFMGL;
import org.drakum.boilerplate.Vao;

public class SkinnedMesh
{
	public Vao vao;
	public BufferObject vboPositions;
	public BufferObject vboNormals;
	public BufferObject vboTexCoords;
	public BufferObject vboBoneIds;
	public BufferObject vboBoneWeights;
	public BufferObject ebo;
	
	public int indexCount;

	public SkinnedMesh(float[] positions, float[] normals, float[] texcoords, int[] indices, int[] boneIds, float[] weights, int vertexCount)
	{
		indexCount = indices.length;

		BufferObject vboPositions = new BufferObject(positions.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboPositions.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(positions));
			vboPositions.unmap();
		}
		
		this.vboPositions = vboPositions;
		
		BufferObject vboNormals = new BufferObject(normals.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboNormals.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(normals));
			vboNormals.unmap();
		}
		
		this.vboNormals = vboNormals;
		
		BufferObject vboTexCoords = new BufferObject(texcoords.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboTexCoords.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(texcoords));
			vboTexCoords.unmap();
		}
		
		this.vboTexCoords = vboTexCoords;
		
		BufferObject vboBoneIds = new BufferObject(boneIds.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboBoneIds.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(boneIds));
			vboBoneIds.unmap();
		}
		
		this.vboBoneIds = vboBoneIds;
		
		BufferObject vboBoneWeights = new BufferObject(weights.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboBoneWeights.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(weights));
			vboBoneWeights.unmap();
		}
		
		this.vboBoneWeights = vboBoneWeights;
		
		BufferObject ebo = new BufferObject(indices.length * 4, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_MAP_WRITE_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = ebo.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(indices));
			ebo.unmap();
		}
		
		this.ebo = ebo;

		this.vao = new Vao();
		this.vao.format(0, 3, FFMGL.GL_FLOAT, false, 0);
		this.vao.format(1, 2, FFMGL.GL_FLOAT, false, 0);
		this.vao.format(2, 3, FFMGL.GL_FLOAT, false, 0);
		this.vao.formatI(5, 4, FFMGL.GL_INT, 0);
		this.vao.format(6, 4, FFMGL.GL_FLOAT, false, 0);
		this.vao.binding(0, 0);
		this.vao.binding(1, 1);
		this.vao.binding(2, 2);
		this.vao.binding(5, 5);
		this.vao.binding(6, 6);
		this.vao.attachVertexArray(0, this.vboPositions, 0, 3 * 4);
		this.vao.attachVertexArray(1, this.vboTexCoords, 0, 2 * 4);
		this.vao.attachVertexArray(2, this.vboNormals, 0, 2 * 4);
		this.vao.attachVertexArray(5, this.vboBoneIds, 0, 4 * 4);
		this.vao.attachVertexArray(6, this.vboBoneWeights, 0, 4 * 4);
		this.vao.attachElementArray(this.ebo);
	}

	public void draw()
	{
		this.vao.bind();
		
		FFMGL.glEnableVertexAttribArray(0);
		FFMGL.glEnableVertexAttribArray(1);
		FFMGL.glEnableVertexAttribArray(5);
		FFMGL.glEnableVertexAttribArray(6);
		
		FFMGL.glDrawElements(FFMGL.GL_TRIANGLES, this.indexCount, FFMGL.GL_UNSIGNED_INT, MemorySegment.NULL);
	}
	
	public void releaseResources()
	{
		this.vao.releaseResources();
		this.vboPositions.releaseResources();
		this.vboTexCoords.releaseResources();
		this.vboNormals.releaseResources();
		this.vboBoneIds.releaseResources();
		this.vboBoneWeights.releaseResources();
		this.ebo.releaseResources();
	}
}
