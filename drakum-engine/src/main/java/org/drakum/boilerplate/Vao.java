package org.drakum.boilerplate;

import static org.lwjgl.opengl.GL46C.*;

public class Vao
{
	private int id;
	
	public Vao()
	{
		this.id = glCreateVertexArrays();
	}

	public void attachVertexArray(int bindingIndex, BufferObject vbo)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		FFMGL.glVertexArrayVertexBuffer(this.id, bindingIndex, vbo.id(), 0l, (int)vbo.size());
	}
	
	public void attachVertexArray(int bindingIndex, BufferObject vbo, long offset, int stride)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		FFMGL.glVertexArrayVertexBuffer(this.id, bindingIndex, vbo.id(), offset, stride);
	}
	
	public void attachVertexArray(int bindingIndex, int vbo, long offset, int stride)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		FFMGL.glVertexArrayVertexBuffer(this.id, bindingIndex, vbo, offset, stride);
	}

	public void attachElementArray(BufferObject ebo)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		glVertexArrayElementBuffer(this.id, ebo.id());
	}
	
	public void attachElementArray(int ebo)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		glVertexArrayElementBuffer(this.id, ebo);
	}
	
	public void format(int attribIndex, int size, int type, boolean normalized, int relativeOffset)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		FFMGL.glVertexArrayAttribFormat(this.id, attribIndex, size, type, normalized, relativeOffset);
	}
	
	public void formatI(int attribIndex, int size, int type, int relativeOffset)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		glVertexArrayAttribIFormat(this.id, attribIndex, size, type, relativeOffset);
	}
	
	public void formatL(int attribIndex, int size, int type, int relativeOffset)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		glVertexArrayAttribLFormat(this.id, attribIndex, size, type, relativeOffset);
	}
	
	public void binding(int attribIndex, int bindingIndex)
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		FFMGL.glVertexArrayAttribBinding(this.id, attribIndex, bindingIndex);
	}
	
	public void bind()
	{
		if(this.id == 0) throw new AssertionError("Vertex array object already freed.");
		
		glBindVertexArray(this.id);
	}
	
	public void releaseResources()
	{
		if(this.id == 0) return;
		
		FFMGL.glDeleteVertexArrays(this.id);
		this.id = 0;
	}
}
