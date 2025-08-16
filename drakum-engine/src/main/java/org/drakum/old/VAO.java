package org.drakum.old;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.barghos.util.nio.buffer.FloatBufferUtils;
import org.barghos.util.nio.buffer.IBufferableRF;
import org.barghos.util.nio.buffer.IntBufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;


public class VAO
{
	
	public static List<VAO> vaos = new ArrayList<>();
	
	private int id;
	
	private HashMap<Integer,Integer> vbos = new HashMap<>();
	
	private int indicesVBO = 0;
	
	public VAO()
	{
		this.id = GL30.glGenVertexArrays();
		
		vaos.add(this);
	}
	
	public int getID()
	{
		return this.id;
	}
	
	public static void clearAll()
	{
		for(VAO vao : vaos)
		{
			vao.clear();
		}
		
		vaos.clear();
	}
	
	
	public void storeData(int attrib, int blocksize, float[] data, int stride, int pointer, int drawFlag)
	{
		BindingUtils.bindVAO(this);
		
		int vboID = 0;

		if(!this.vbos.containsKey(attrib))
		{
			vboID = GL15.glGenBuffers();
			vbos.put(attrib, vboID);
		}
		else
			vboID = this.vbos.get(attrib);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);

		FloatBuffer buffer = FloatBufferUtils.directFromFloat(true, data);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawFlag);
		
		GL20.glVertexAttribPointer(attrib, blocksize, GL11.GL_FLOAT, false, stride, pointer);
	}
	
	public void storeData(int attrib, int blocksize, int[] data, int stride, int pointer, int drawFlag)
	{
		BindingUtils.bindVAO(this);
		
		int vboID = 0;

		if(!this.vbos.containsKey(attrib))
		{
			vboID = GL15.glGenBuffers();
			vbos.put(attrib, vboID);
		}
		else
			vboID = this.vbos.get(attrib);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		
		IntBuffer buffer = IntBufferUtils.directFromInt(data);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawFlag);
		
		GL20.glVertexAttribPointer(attrib, blocksize, GL11.GL_INT, false, stride, pointer);
	}
	
	public void storeDataTuple2(int attrib, IBufferableRF[] data, int stride, int pointer, int drawFlag)
	{
		BindingUtils.bindVAO(this);
		
		int vboID = 0;

		if(!this.vbos.containsKey(attrib))
		{
			vboID = GL15.glGenBuffers();
			vbos.put(attrib, vboID);
		}
		else
			vboID = this.vbos.get(attrib);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		
		FloatBuffer buffer = FloatBufferUtils.directFromTuple2(true, data);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawFlag);
		
		GL20.glVertexAttribPointer(attrib, 2, GL11.GL_FLOAT, false, stride, pointer);
	}
	
	public void storeDataTuple3(int attrib, IBufferableRF[] data, int stride, int pointer, int drawFlag)
	{
		BindingUtils.bindVAO(this);
		
		int vboID = 0;

		if(!this.vbos.containsKey(attrib))
		{
			vboID = GL15.glGenBuffers();
			vbos.put(attrib, vboID);
		}
		else
			vboID = this.vbos.get(attrib);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		
		FloatBuffer buffer = FloatBufferUtils.directFromTuple3(true, data);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawFlag);
		
		GL20.glVertexAttribPointer(attrib, 3, GL11.GL_FLOAT, false, stride, pointer);
	}
	
	public void storeDataTuple4(int attrib, IBufferableRF[] data, int stride, int pointer, int drawFlag)
	{
		BindingUtils.bindVAO(this);
		
		int vboID = 0;

		if(!this.vbos.containsKey(attrib))
		{
			vboID = GL15.glGenBuffers();
			vbos.put(attrib, vboID);
		}
		else
			vboID = this.vbos.get(attrib);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		
		FloatBuffer buffer = FloatBufferUtils.directFromTuple4(true, data);
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, drawFlag);
		
		GL20.glVertexAttribPointer(attrib, 4, GL11.GL_FLOAT, false, stride, pointer);
}
	
	public void storeIndices(int[] indices, int drawflag)
	{
		BindingUtils.bindVAO(this);
		
		if(this.indicesVBO == 0) this.indicesVBO = GL15.glGenBuffers();

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.indicesVBO);
		
		IntBuffer buffer = IntBufferUtils.directFromInt(true, indices);
		
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, drawflag);
	}
	
	public void clearVBOs()
	{
		BindingUtils.bindVAO(this);
		
		for(int i : vbos.values())
		{
			GL15.glDeleteBuffers(i);
		}
		
		if(this.indicesVBO != 0)
		{
			GL15.glDeleteBuffers(this.indicesVBO);	
		}
		
		vbos.clear();
		
		indicesVBO = 0;
		
		GL30.glDeleteVertexArrays(this.id);
		
		vaos.remove(this);
		
		this.id = GL30.glGenVertexArrays();
		
		vaos.add(this);
	}
	
	public void clear()
	{
		BindingUtils.bindVAO(this);
		
		for(int i : vbos.values())
		{
			GL15.glDeleteBuffers(i);
		}
		
		if(this.indicesVBO != 0)
		{
			GL15.glDeleteBuffers(this.indicesVBO);	
		}
		
		GL30.glDeleteVertexArrays(this.id);
	}
}
