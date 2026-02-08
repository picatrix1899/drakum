package org.drakum.model;

import static org.lwjgl.opengl.GL44C.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

import org.drakum.boilerplate.BufferObject;
import org.drakum.boilerplate.Vao;

public class Quad
{
	public Vao vao;
	public BufferObject ebo;
	public List<BufferObject> vbos = new ArrayList<>();
	public int vertexCount;

	public float x;
	public float y;
	public float width;
	public float height;
	
	public Quad(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		float[] pos = new float[8];
		pos[0] = x;
		pos[1] = y;
		pos[2] = x;
		pos[3] = y + height;
		pos[4] = x + width;
		pos[5] = y + height;
		pos[6] = x + width;
		pos[7] = y;
		
		float[] texCoords = new float[8];
		texCoords[0] = 0.0f;
		texCoords[1] = 0.0f;
		texCoords[2] = 0.0f;
		texCoords[3] = 1.0f;
		texCoords[4] = 1.0f;
		texCoords[5] = 1.0f;
		texCoords[6] = 1.0f;
		texCoords[7] = 0.0f;
		
		int[] indices = new int[6];
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;
		indices[3] = 0;
		indices[4] = 2;
		indices[5] = 3;
		
		vertexCount = indices.length;
		
		BufferObject vboPosition = new BufferObject(pos.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboPosition.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(pos));
			vboPosition.unmap();
		}
		
		this.vbos.add(vboPosition);
		
		BufferObject vboTexCoords = new BufferObject(texCoords.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboTexCoords.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(texCoords));
			vboTexCoords.unmap();
		}
		
		this.vbos.add(vboTexCoords);
		
		BufferObject ebo = new BufferObject(indices.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = ebo.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(indices));
			ebo.unmap();
		}
		
		this.ebo = ebo;
		
		this.vao = new Vao();
		this.vao.format(0, 2, GL_FLOAT, false, 0);
		this.vao.format(1, 2, GL_FLOAT, false, 0);
		this.vao.binding(0, 0);
		this.vao.binding(1, 1);
		
		this.vao.attachVertexArray(0, vboPosition, 0, 2 * 4);
		this.vao.attachVertexArray(1, vboTexCoords, 0, 2 * 4);
		this.vao.attachElementArray(ebo);
	}
	
	public void draw()
	{
		this.vao.bind();
		
		glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);
	}
	
	public void releaseResources()
	{
		this.vao.releaseResources();
		this.ebo.releaseResources();
		for(BufferObject buf : this.vbos)
		{
			buf.releaseResources();
		}
		
		this.vbos.clear();
	}
}
