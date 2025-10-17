package org.drakum;

import static org.lwjgl.opengl.GL46C.*;

import org.barghos.util.tuple.floats.ITup2RF;
import org.barghos.util.tuple.floats.ITup3RF;
import org.barghos.util.tuple.floats.ITup4RF;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class Vao
{
	private int vao;
	private Int2IntMap vbos = new Int2IntOpenHashMap();
	private int ebo;
	
	public Vao()
	{
		this.vao = glCreateVertexArrays();
		this.ebo = glCreateBuffers();
	}
	
	public void storeFloatData(int attrib, int blocksize, float[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		glBufferData(GL_ARRAY_BUFFER, data, drawFlag);
		
		glVertexAttribPointer(attrib, blocksize, GL_FLOAT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeIntData(int attrib, int blocksize, int[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		glBufferData(GL_ARRAY_BUFFER, data, drawFlag);
		
		glVertexAttribPointer(attrib, blocksize, GL_INT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeUIntData(int attrib, int blocksize, int[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		glBufferData(GL_ARRAY_BUFFER, data, drawFlag);
		
		glVertexAttribPointer(attrib, blocksize, GL_UNSIGNED_INT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeData(int attrib, ITup2RF[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		float[] d = new float[data.length * 2];
		for(int i = 0; i < data.length; i++)
		{
			d[i * 2 + 0] = data[i].v0();
			d[i * 2 + 1] = data[i].v1();
		}

		glBufferData(GL_ARRAY_BUFFER, d, drawFlag);
		
		glVertexAttribPointer(attrib, 2, GL_FLOAT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeData(int attrib, ITup3RF[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		float[] d = new float[data.length * 3];
		for(int i = 0; i < data.length; i++)
		{
			d[i * 3 + 0] = data[i].v0();
			d[i * 3 + 1] = data[i].v1();
			d[i * 3 + 2] = data[i].v2();
		}

		glBufferData(GL_ARRAY_BUFFER, d, drawFlag);
		
		glVertexAttribPointer(attrib, 3, GL_FLOAT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeData(int attrib, ITup4RF[] data, int stride, int pointer, int drawFlag)
	{
		if(!this.vbos.containsKey(attrib))
		{
			this.vbos.put(attrib, glCreateBuffers());
		}
		
		int vbo = this.vbos.get(attrib);
		
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		float[] d = new float[data.length * 4];
		for(int i = 0; i < data.length; i++)
		{
			d[i * 4 + 0] = data[i].v0();
			d[i * 4 + 1] = data[i].v1();
			d[i * 4 + 2] = data[i].v2();
			d[i * 4 + 3] = data[i].v3();
		}

		glBufferData(GL_ARRAY_BUFFER, d, drawFlag);
		
		glVertexAttribPointer(attrib, 4, GL_FLOAT, false, stride, pointer);
		
		glBindVertexArray(0);
	}
	
	public void storeIndices(int[] indices, int drawflag)
	{
		glBindVertexArray(this.vao);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
		
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, drawflag);
		
		glBindVertexArray(0);
	}
	
	public void bind()
	{
		glBindVertexArray(this.vao);
	}
	
	public void releaseResources()
	{
		glDeleteVertexArrays(this.vao);
		
		for(int vbo : this.vbos.values())
		{
			glDeleteBuffers(vbo);
		}
		
		glDeleteBuffers(this.ebo);
	}
}
