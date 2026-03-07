package org.drakum.model;

import static org.lwjgl.opengl.GL44C.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.barghos.math.mesh.MeshData;
import org.barghos.math.mesh.MeshGenerator2d;
import org.barghos.math.mesh.MeshVertexData;
import org.drakum.boilerplate.BufferObject;
import org.drakum.boilerplate.Vao;

public class Quad
{
	public Vao vao;
	public BufferObject vbo;
	public int indexCount;

	public float x;
	public float y;
	public float width;
	public float height;
	
	public Quad(float x, float y, float width, float height)
	{
		try(Arena vertexDataArena = Arena.ofConfined())
		{
			//MeshVertexData vertexData = MeshGenerator2d.generateRect2D(MeshGenerator2d.UV0, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f);
			//MeshVertexData vertexData = MeshGenerator2d.generateRoundedRect2D(MeshGenerator2d.UV0, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, 40, 4);
			MeshVertexData vertexData = MeshGenerator2d.generateCircle(MeshGenerator2d.UV0, x, y, 0.0f, 0.0f, 1.0f, 1.0f, width / 2, 32);
			MeshData meshData = MeshGenerator2d.packInterleaved(vertexData, vertexDataArena);
			
			this.indexCount = meshData.indexCount;
			
			BufferObject vboData = new BufferObject(meshData.indexBlockByteSize + meshData.vertexDataBlockByteSize, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);

			MemorySegment mem = vboData.map(GL_MAP_WRITE_BIT, vertexDataArena);
			mem.copyFrom(meshData.data);
			vboData.unmap();
			
			this.vbo = vboData;
			
			this.vao = new Vao();
			this.vao.format(0, 2, GL_FLOAT, false, 0);
			this.vao.format(1, 2, GL_FLOAT, false, 2 * 4);
			this.vao.binding(0, 0);
			this.vao.binding(1, 0);
			
			this.vao.attachVertexArray(0, vboData, meshData.indexBlockByteSize, 4 * 4);
			this.vao.attachElementArray(vboData);
		}
	}
	
	public void draw()
	{
		this.vao.bind();
		
		glDrawElements(GL_TRIANGLES, this.indexCount, GL_UNSIGNED_INT, 0);
	}
	
	public void releaseResources()
	{
		this.vao.releaseResources();

		this.vbo.releaseResources();
	}
}
