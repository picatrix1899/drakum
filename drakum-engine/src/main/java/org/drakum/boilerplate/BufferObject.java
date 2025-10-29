package org.drakum.boilerplate;

import static org.lwjgl.opengl.GL46C.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class BufferObject
{
	private int id;
	private MemorySegment mappedMemory;
	private long size;
	
	public BufferObject(long size, int flags)
	{
		this.id = FFMGL.glCreateBuffers();
		this.size = size;
		
		FFMGL.glNamedBufferStorage(this.id, size, flags);
	}
	
	public int id()
	{
		return this.id;
	}
	
	public long size()
	{
		return this.size;
	}
	
	public MemorySegment map(int flags, Arena arena)
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		MemorySegment seg = FFMGL.glMapNamedBufferRange(this.id, 0l, this.size, flags, arena);
		
		this.mappedMemory = seg;
		
		return seg;
	}
	
	public MemorySegment map(long offset, long size, int flags, Arena arena)
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		MemorySegment seg = FFMGL.glMapNamedBufferRange(this.id, offset, size, flags, arena);
		
		this.mappedMemory = seg;
		
		return seg;
	}
	
	public MemorySegment memory()
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		return this.mappedMemory;
	}
	
	public void unmap()
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		FFMGL.glUnmapNamedBuffer(this.id);
		this.mappedMemory = null;
	}
	
	public void copyTo(BufferObject buf)
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		FFMGL.glCopyNamedBufferSubData(this.id, buf.id, 0, 0, size);
	}
	
	public void copyTo(BufferObject buf, long readOffset, long writeOffset, long size)
	{
		if(this.id == 0) throw new AssertionError("Buffer object already freed.");
		
		FFMGL.glCopyNamedBufferSubData(this.id, buf.id, readOffset, writeOffset, size);
	}
	
	public void bindAsSSBO(int index)
	{
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, this.id);
	}
	
	public void releaseResources()
	{
		if(id == 0) return;
		
		FFMGL.glDeleteBuffers(this.id);
		this.id = 0;
	}
}
