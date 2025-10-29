package org.drakum.model;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import java.util.List;

import org.barghos.impl.core.collection.ArrayUtils;

import static org.barghos.api.core.tuple.floats.IOOpsMem2FAligned.*;
import static org.barghos.api.core.tuple.floats.IOOpsITupMem3FAligned.*;

import org.drakum.OBJFile;
import org.drakum.Vertex;
import org.drakum.boilerplate.BufferObject;
import org.drakum.boilerplate.FFMGL;
import org.drakum.boilerplate.Vao;

public class ConstMesh
{
	public Vao vao;
	public BufferObject ebo;
	public BufferObject vbo;
	public int vertexCount;

	public ConstMesh(OBJFile obj)
	{
		List<Integer> ind = obj.indices;
		List<Vertex> vertices = obj.vertices;
		int verticesCount = vertices.size();

		this.vertexCount = verticesCount;
		
		int vboSize = verticesCount * 64;
		
		BufferObject vbo = new BufferObject(vboSize, FFMGL.GL_MAP_READ_BIT | FFMGL.GL_CLIENT_STORAGE_BIT);
		
		BufferObject staging = new BufferObject(vboSize, FFMGL.GL_MAP_WRITE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = staging.map(FFMGL.GL_MAP_WRITE_BIT, arena);
			
			Vertex vertex;
			
			for (int i = 0; i < verticesCount; i++)
			{
				vertex = vertices.get(i);
		
				int base = i * 64;
				
				tup_writeBlock16T_3fa(vertex.pos, mem, base + 0l);
				tup_writeBlock16T_2fa(vertex.uv.v0(), -vertex.uv.v1(), mem, base + 16l);
				tup_writeBlock16T_3fa(vertex.normal, mem, base + 32l);
				tup_writeBlock16T_3fa(vertex.tangent, mem, base + 48l);
			}

			staging.unmap();
			
			staging.copyTo(vbo);
		}
		
		this.vbo = vbo;
		
		int[] indices = new int[ind.size()];
		indices = ArrayUtils.convertToPrimitive(ind.toArray(new Integer[ind.size()]));

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
		this.vao.format(1, 2, FFMGL.GL_FLOAT, false, 16);
		this.vao.format(2, 3, FFMGL.GL_FLOAT, false, 32);
		this.vao.format(3, 3, FFMGL.GL_FLOAT, false, 48);
		this.vao.binding(0, 0);
		this.vao.binding(1, 0);
		this.vao.binding(2, 0);
		this.vao.binding(3, 0);
		this.vao.attachVertexArray(0, this.vbo, 0, 64);
		this.vao.attachElementArray(ebo);
	}
	
	public void draw()
	{
		this.vao.bind();
		
		FFMGL.glDrawElements(FFMGL.GL_TRIANGLES, this.vertexCount, FFMGL.GL_UNSIGNED_INT);
	}
	
	public void releaseResources()
	{
		this.vao.releaseResources();
		this.ebo.releaseResources();
		this.vbo.releaseResources();
	}
}
