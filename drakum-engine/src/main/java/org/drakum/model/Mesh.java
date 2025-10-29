package org.drakum.model;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

import org.drakum.OBJFile;
import org.drakum.Vertex;
import org.drakum.boilerplate.BufferObject;
import org.drakum.boilerplate.Vao;
import org.barghos.impl.core.collection.ArrayUtils;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh
{
	public Vao vao;
	public BufferObject ebo;
	public List<BufferObject> vbos = new ArrayList<>();
	public int vertexCount;

	public Mesh(OBJFile obj)
	{
		List<Integer> ind = obj.indices;
		List<Vertex> vertices = obj.vertices;
		int verticesCount = vertices.size();

		this.vertexCount = verticesCount;
		
		float[] pos = new float[verticesCount * 3];
		float[] uvs = new float[verticesCount * 2];
		float[] nrm = new float[verticesCount * 3];
		float[] tng = new float[verticesCount * 3];

		Vertex vertexA;
		Vertex vertexB;
		Vertex vertexC;
		
		for (int i = 0; i < verticesCount; i+=3)
		{
			vertexA = vertices.get(i);
			vertexB = vertices.get(i+1);
			vertexC = vertices.get(i+2);

			pos[i * 3 + 0] = vertexA.pos.v0();
			pos[i * 3 + 1] = vertexA.pos.v1();
			pos[i * 3 + 2] = vertexA.pos.v2();
			pos[i * 3 + 3] = vertexB.pos.v0();
			pos[i * 3 + 4] = vertexB.pos.v1();
			pos[i * 3 + 5] = vertexB.pos.v2();
			pos[i * 3 + 6] = vertexC.pos.v0();
			pos[i * 3 + 7] = vertexC.pos.v1();
			pos[i * 3 + 8] = vertexC.pos.v2();

			uvs[i * 2 + 0] = vertexA.uv.v0();
			uvs[i * 2 + 1] = -vertexA.uv.v1();
			uvs[i * 2 + 2] = vertexB.uv.v0();
			uvs[i * 2 + 3] = -vertexB.uv.v1();
			uvs[i * 2 + 4] = vertexC.uv.v0();
			uvs[i * 2 + 5] = -vertexC.uv.v1();

			nrm[i * 3 + 0] = vertexA.normal.v0();
			nrm[i * 3 + 1] = vertexA.normal.v1();
			nrm[i * 3 + 2] = vertexA.normal.v2();
			nrm[i * 3 + 3] = vertexB.normal.v0();
			nrm[i * 3 + 4] = vertexB.normal.v1();
			nrm[i * 3 + 5] = vertexB.normal.v2();
			nrm[i * 3 + 6] = vertexC.normal.v0();
			nrm[i * 3 + 7] = vertexC.normal.v1();
			nrm[i * 3 + 8] = vertexC.normal.v2();

			tng[i * 3 + 0] = vertexA.tangent.v0();
			tng[i * 3 + 1] = vertexA.tangent.v1();
			tng[i * 3 + 2] = vertexA.tangent.v2();
			tng[i * 3 + 3] = vertexB.tangent.v0();
			tng[i * 3 + 4] = vertexB.tangent.v1();
			tng[i * 3 + 5] = vertexB.tangent.v2();
			tng[i * 3 + 6] = vertexC.tangent.v0();
			tng[i * 3 + 7] = vertexC.tangent.v1();
			tng[i * 3 + 8] = vertexC.tangent.v2();
		}

		int[] indices = new int[ind.size()];
		indices = ArrayUtils.convertToPrimitive(ind.toArray(new Integer[ind.size()]));

		BufferObject vboPosition = new BufferObject(pos.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboPosition.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(pos));
			vboPosition.unmap();
		}
		
		this.vbos.add(vboPosition);
		
		BufferObject vboTexCoords = new BufferObject(uvs.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboTexCoords.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(uvs));
			vboTexCoords.unmap();
		}
		
		this.vbos.add(vboTexCoords);
		
		BufferObject vboNormals = new BufferObject(nrm.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboNormals.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(nrm));
			vboNormals.unmap();
		}
		
		this.vbos.add(vboNormals);
		
		BufferObject vboTangents = new BufferObject(tng.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = vboTangents.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(tng));
			vboTangents.unmap();
		}
		
		this.vbos.add(vboTangents);
		
		BufferObject ebo = new BufferObject(indices.length * 4, GL_MAP_READ_BIT | GL_MAP_WRITE_BIT | GL_CLIENT_STORAGE_BIT);
		
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment mem = ebo.map(GL_MAP_WRITE_BIT, arena);
			mem.copyFrom(MemorySegment.ofArray(indices));
			ebo.unmap();
		}
		
		this.ebo = ebo;
		
		this.vao = new Vao();
		this.vao.format(0, 3, GL_FLOAT, false, 0);
		this.vao.format(1, 2, GL_FLOAT, false, 0);
		this.vao.format(2, 3, GL_FLOAT, false, 0);
		this.vao.format(3, 3, GL_FLOAT, false, 0);
		this.vao.binding(0, 0);
		this.vao.binding(1, 1);
		this.vao.binding(2, 2);
		this.vao.binding(3, 3);
		
		this.vao.attachVertexArray(0, vboPosition, 0, 3 * 4);
		this.vao.attachVertexArray(1, vboTexCoords, 0, 2 * 4);
		this.vao.attachVertexArray(2, vboNormals, 0, 3 * 4);
		this.vao.attachVertexArray(3, vboTangents, 0, 3 * 4);
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
