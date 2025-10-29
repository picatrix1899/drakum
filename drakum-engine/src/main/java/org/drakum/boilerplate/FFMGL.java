package org.drakum.boilerplate;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.Arena;
import java.lang.invoke.MethodHandle;

public class FFMGL
{
	/** Accepted by the {@code access} parameter of MapBufferRange. */
    public static final int
        GL_MAP_READ_BIT              = 0x1,
        GL_MAP_WRITE_BIT             = 0x2,
        GL_MAP_INVALIDATE_RANGE_BIT  = 0x4,
        GL_MAP_INVALIDATE_BUFFER_BIT = 0x8,
        GL_MAP_FLUSH_EXPLICIT_BIT    = 0x10,
        GL_MAP_UNSYNCHRONIZED_BIT    = 0x20;
	
    /** Accepted in the {@code flags} parameter of {@link #glBufferStorage BufferStorage} and {@link ARBBufferStorage#glNamedBufferStorageEXT NamedBufferStorageEXT}. */
    public static final int
        GL_MAP_PERSISTENT_BIT  = 0x40,
        GL_MAP_COHERENT_BIT    = 0x80,
        GL_DYNAMIC_STORAGE_BIT = 0x100,
        GL_CLIENT_STORAGE_BIT  = 0x200;
    
    /** DataType */
    public static final int
        GL_BYTE           = 0x1400,
        GL_UNSIGNED_BYTE  = 0x1401,
        GL_SHORT          = 0x1402,
        GL_UNSIGNED_SHORT = 0x1403,
        GL_INT            = 0x1404,
        GL_UNSIGNED_INT   = 0x1405,
        GL_FLOAT          = 0x1406,
        GL_DOUBLE         = 0x140A;
    
    /** BeginMode */
    public static final int
        GL_POINTS         = 0x0,
        GL_LINES          = 0x1,
        GL_LINE_LOOP      = 0x2,
        GL_LINE_STRIP     = 0x3,
        GL_TRIANGLES      = 0x4,
        GL_TRIANGLE_STRIP = 0x5,
        GL_TRIANGLE_FAN   = 0x6,
        GL_QUADS          = 0x7;
    
	private static final Linker LINKER = Linker.nativeLinker();
	
	private static final MethodHandle mh_glGetProcAddress;
	private static final MethodHandle mh_glMapNamedBufferRange;
	private static final MethodHandle mh_glUnmapNamedBuffer;
	private static final MethodHandle mh_glNamedBufferStorage;
	private static final MethodHandle mh_glCreateBuffers;
	private static final MethodHandle mh_glDeleteBuffers;
	private static final MethodHandle mh_glCopyNamedBufferSubData;
	private static final MethodHandle mh_glVertexArrayVertexBuffer;
	private static final MethodHandle mh_glVertexArrayAttribFormat;
	private static final MethodHandle mh_glVertexArrayAttribBinding;
	private static final MethodHandle mh_glDeleteVertexArrays;
	private static final MethodHandle mh_glEnableVertexAttribArray;
	private static final MethodHandle mh_glDisableVertexAttribArray;
	private static final MethodHandle mh_glDrawElements;
	
	static
	{
		SymbolLookup lookup = SymbolLookup.libraryLookup("opengl32.dll", Arena.global());
		
		mh_glGetProcAddress = LINKER.downcallHandle(lookup.findOrThrow("wglGetProcAddress"), FunctionDescriptor.of(ADDRESS, ADDRESS));
		
		mh_glMapNamedBufferRange = LINKER.downcallHandle(glGetProcAddress("glMapNamedBufferRange"), FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_LONG, JAVA_LONG, JAVA_INT));
		mh_glUnmapNamedBuffer = LINKER.downcallHandle(glGetProcAddress("glUnmapNamedBuffer"), FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT));
		mh_glNamedBufferStorage = LINKER.downcallHandle(glGetProcAddress("glNamedBufferStorage"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_LONG, ADDRESS, JAVA_INT));
		mh_glCreateBuffers = LINKER.downcallHandle(glGetProcAddress("glCreateBuffers"), FunctionDescriptor.ofVoid(JAVA_INT, ADDRESS));
		mh_glDeleteBuffers = LINKER.downcallHandle(glGetProcAddress("glDeleteBuffers"), FunctionDescriptor.ofVoid(JAVA_INT, ADDRESS));
		mh_glCopyNamedBufferSubData = LINKER.downcallHandle(glGetProcAddress("glCopyNamedBufferSubData"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_LONG, JAVA_LONG, JAVA_LONG));
		mh_glVertexArrayVertexBuffer = LINKER.downcallHandle(glGetProcAddress("glVertexArrayVertexBuffer"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_LONG, JAVA_INT));
		mh_glVertexArrayAttribFormat = LINKER.downcallHandle(glGetProcAddress("glVertexArrayAttribFormat"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_BOOLEAN, JAVA_INT));
		mh_glVertexArrayAttribBinding = LINKER.downcallHandle(glGetProcAddress("glVertexArrayAttribBinding"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT));
		mh_glDeleteVertexArrays = LINKER.downcallHandle(glGetProcAddress("glDeleteVertexArrays"), FunctionDescriptor.ofVoid(JAVA_INT, ADDRESS));
		mh_glEnableVertexAttribArray = LINKER.downcallHandle(glGetProcAddress("glEnableVertexAttribArray"), FunctionDescriptor.ofVoid(JAVA_INT));
		mh_glDisableVertexAttribArray = LINKER.downcallHandle(glGetProcAddress("glDisableVertexAttribArray"), FunctionDescriptor.ofVoid(JAVA_INT));
		mh_glDrawElements = LINKER.downcallHandle(glGetProcAddress("glDrawElements"), FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS));
	}
	
	public static MemorySegment glGetProcAddress(String name)
	{
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment cName = arena.allocateFrom(name);
			MemorySegment address = (MemorySegment) mh_glGetProcAddress.invokeExact(cName);
			
			return address;
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glGetProcAddress failed", t);
		}
	}
	
	/**
     * DSA version of {@link GL30C#glMapBufferRange MapBufferRange}.
     *
     * @param buffer the buffer object name
     * @param offset the starting offset within the buffer of the range to be mapped
     * @param length the length of the range to be mapped
     * @param access a combination of access flags indicating the desired access to the range. One or more of:<br><table><tr><td>{@link GL30#GL_MAP_READ_BIT MAP_READ_BIT}</td><td>{@link GL30#GL_MAP_WRITE_BIT MAP_WRITE_BIT}</td><td>{@link GL30#GL_MAP_INVALIDATE_RANGE_BIT MAP_INVALIDATE_RANGE_BIT}</td><td>{@link GL30#GL_MAP_INVALIDATE_BUFFER_BIT MAP_INVALIDATE_BUFFER_BIT}</td></tr><tr><td>{@link GL30#GL_MAP_FLUSH_EXPLICIT_BIT MAP_FLUSH_EXPLICIT_BIT}</td><td>{@link GL30#GL_MAP_UNSYNCHRONIZED_BIT MAP_UNSYNCHRONIZED_BIT}</td></tr></table>
     * 
     * @see <a href="https://docs.gl/gl4/glMapBufferRange">Reference Page</a>
     */
	public static MemorySegment glMapNamedBufferRange(int buffer, long offset, long length, int access, Arena arena)
	{
		try
		{
			MemorySegment ptr = (MemorySegment) mh_glMapNamedBufferRange.invokeExact(buffer, offset, length, access);
			
			return ptr.reinterpret(length, arena, null);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glMapNamedBufferRange failed", t);
		}
	}
	
	/**
     * DSA version of {@link GL15C#glUnmapBuffer UnmapBuffer}.
     *
     * @param buffer the buffer object name
     * 
     * @see <a href="https://docs.gl/gl4/glUnmapBuffer">Reference Page</a>
     */
	public static boolean glUnmapNamedBuffer(int buffer)
	{
		try
		{
			return (boolean) mh_glUnmapNamedBuffer.invokeExact(buffer);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glUnmapNamedBuffer failed", t);
		}
	}
	
	 /**
     * DSA version of {@link GL44C#glBufferStorage BufferStorage}.
     *
     * @param buffer the buffer object name
     * @param size   the size of the data store in basic machine units
     * @param flags  the bitwise {@code OR} of flags describing the intended usage of the buffer object's data store by the application. Valid flags and their meanings
     *               are as follows:
     *               
     *               <ul>
     *               <li>{@link GL44#GL_DYNAMIC_STORAGE_BIT DYNAMIC_STORAGE_BIT} &ndash; The contents of the data store may be updated after creation through calls to
     *               {@link GL15C#glBufferSubData BufferSubData}. If this bit is not set, the buffer content may not be directly updated by the client. The {@code data}
     *               argument may be used to specify the initial content of the buffer's data store regardless of the presence of the {@link GL44#GL_DYNAMIC_STORAGE_BIT DYNAMIC_STORAGE_BIT}.
     *               Regardless of the presence of this bit, buffers may always be updated with server-side calls such as {@link GL31C#glCopyBufferSubData CopyBufferSubData} and
     *               {@link GL43C#glClearBufferSubData ClearBufferSubData}.</li>
     *               <li>{@link GL30#GL_MAP_READ_BIT MAP_READ_BIT} &ndash; The buffer's data store may be mapped by the client for read access and a pointer in the client's address space
     *               obtained that may be read from.</li>
     *               <li>{@link GL30#GL_MAP_WRITE_BIT MAP_WRITE_BIT} &ndash; The buffer's data store may be mapped by the client for write access and a pointer in the client's address
     *               space obtained that may be written to.</li>
     *               <li>{@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT} &ndash; The client may request that the server read from or write to the buffer while it is mapped. The client's
     *               pointer to the data store remains valid so long as the data store is mapped, even during execution of drawing or dispatch commands.</li>
     *               <li>{@link GL44#GL_MAP_COHERENT_BIT MAP_COHERENT_BIT} &ndash; Shared access to buffers that are simultaneously mapped for client access and are used by the server will be
     *               coherent, so long as that mapping is performed using MapBufferRange. That is, data written to the store by either the client or server will be
     *               immediately visible to the other with no further action taken by the application. In particular:
     *               
     *               <ul>
     *               <li>If {@code MAP_COHERENT_BIT} is not set and the client performs a write followed by a call to the {@link GL42C#glMemoryBarrier MemoryBarrier} command with
     *               the {@link GL44#GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT CLIENT_MAPPED_BUFFER_BARRIER_BIT} set, then in subsequent commands the server will see the writes.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is set and the client performs a write, then in subsequent commands the server will see the writes.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is not set and the server performs a write, the application must call {@link GL42C#glMemoryBarrier MemoryBarrier} with the
     *               {@link GL44#GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT CLIENT_MAPPED_BUFFER_BARRIER_BIT} set and then call {@link GL32C#glFenceSync FenceSync} with {@link GL32#GL_SYNC_GPU_COMMANDS_COMPLETE SYNC_GPU_COMMANDS_COMPLETE} (or
     *               {@link GL11C#glFinish Finish}). Then the CPU will see the writes after the sync is complete.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is set and the server does a write, the app must call {@link GL32C#glFenceSync FenceSync} with
     *               {@link GL32#GL_SYNC_GPU_COMMANDS_COMPLETE SYNC_GPU_COMMANDS_COMPLETE} (or {@link GL11C#glFinish Finish}). Then the CPU will see the writes after the sync is complete.</li>
     *               </ul></li>
     *               <li>{@link GL44#GL_CLIENT_STORAGE_BIT CLIENT_STORAGE_BIT} &ndash; When all other criteria for the buffer storage allocation are met, this bit may be used by an
     *               implementation to determine whether to use storage that is local to the server or to the client to serve as the backing store for the buffer.</li>
     *               </ul>
     *               
     *               <p>If {@code flags} contains {@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT}, it must also contain at least one of {@link GL30#GL_MAP_READ_BIT MAP_READ_BIT} or {@link GL30#GL_MAP_WRITE_BIT MAP_WRITE_BIT}.</p>
     *               
     *               <p>It is an error to specify {@link GL44#GL_MAP_COHERENT_BIT MAP_COHERENT_BIT} without also specifying {@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT}.</p>
     * 
     * @see <a href="https://docs.gl/gl4/glBufferStorage">Reference Page</a>
     */
	public static void glNamedBufferStorage(int buffer, long size, int flags)
	{
		try
		{
			mh_glNamedBufferStorage.invokeExact(buffer, size, MemorySegment.NULL, flags);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glNamedBufferStorage failed", t);
		}
	}
	
	 /**
     * DSA version of {@link GL44C#glBufferStorage BufferStorage}.
     *
     * @param buffer the buffer object name
     * @param size   the size of the data store in basic machine units
     * @param flags  the bitwise {@code OR} of flags describing the intended usage of the buffer object's data store by the application. Valid flags and their meanings
     *               are as follows:
     *               
     *               <ul>
     *               <li>{@link GL44#GL_DYNAMIC_STORAGE_BIT DYNAMIC_STORAGE_BIT} &ndash; The contents of the data store may be updated after creation through calls to
     *               {@link GL15C#glBufferSubData BufferSubData}. If this bit is not set, the buffer content may not be directly updated by the client. The {@code data}
     *               argument may be used to specify the initial content of the buffer's data store regardless of the presence of the {@link GL44#GL_DYNAMIC_STORAGE_BIT DYNAMIC_STORAGE_BIT}.
     *               Regardless of the presence of this bit, buffers may always be updated with server-side calls such as {@link GL31C#glCopyBufferSubData CopyBufferSubData} and
     *               {@link GL43C#glClearBufferSubData ClearBufferSubData}.</li>
     *               <li>{@link GL30#GL_MAP_READ_BIT MAP_READ_BIT} &ndash; The buffer's data store may be mapped by the client for read access and a pointer in the client's address space
     *               obtained that may be read from.</li>
     *               <li>{@link GL30#GL_MAP_WRITE_BIT MAP_WRITE_BIT} &ndash; The buffer's data store may be mapped by the client for write access and a pointer in the client's address
     *               space obtained that may be written to.</li>
     *               <li>{@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT} &ndash; The client may request that the server read from or write to the buffer while it is mapped. The client's
     *               pointer to the data store remains valid so long as the data store is mapped, even during execution of drawing or dispatch commands.</li>
     *               <li>{@link GL44#GL_MAP_COHERENT_BIT MAP_COHERENT_BIT} &ndash; Shared access to buffers that are simultaneously mapped for client access and are used by the server will be
     *               coherent, so long as that mapping is performed using MapBufferRange. That is, data written to the store by either the client or server will be
     *               immediately visible to the other with no further action taken by the application. In particular:
     *               
     *               <ul>
     *               <li>If {@code MAP_COHERENT_BIT} is not set and the client performs a write followed by a call to the {@link GL42C#glMemoryBarrier MemoryBarrier} command with
     *               the {@link GL44#GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT CLIENT_MAPPED_BUFFER_BARRIER_BIT} set, then in subsequent commands the server will see the writes.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is set and the client performs a write, then in subsequent commands the server will see the writes.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is not set and the server performs a write, the application must call {@link GL42C#glMemoryBarrier MemoryBarrier} with the
     *               {@link GL44#GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT CLIENT_MAPPED_BUFFER_BARRIER_BIT} set and then call {@link GL32C#glFenceSync FenceSync} with {@link GL32#GL_SYNC_GPU_COMMANDS_COMPLETE SYNC_GPU_COMMANDS_COMPLETE} (or
     *               {@link GL11C#glFinish Finish}). Then the CPU will see the writes after the sync is complete.</li>
     *               <li>If {@code MAP_COHERENT_BIT} is set and the server does a write, the app must call {@link GL32C#glFenceSync FenceSync} with
     *               {@link GL32#GL_SYNC_GPU_COMMANDS_COMPLETE SYNC_GPU_COMMANDS_COMPLETE} (or {@link GL11C#glFinish Finish}). Then the CPU will see the writes after the sync is complete.</li>
     *               </ul></li>
     *               <li>{@link GL44#GL_CLIENT_STORAGE_BIT CLIENT_STORAGE_BIT} &ndash; When all other criteria for the buffer storage allocation are met, this bit may be used by an
     *               implementation to determine whether to use storage that is local to the server or to the client to serve as the backing store for the buffer.</li>
     *               </ul>
     *               
     *               <p>If {@code flags} contains {@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT}, it must also contain at least one of {@link GL30#GL_MAP_READ_BIT MAP_READ_BIT} or {@link GL30#GL_MAP_WRITE_BIT MAP_WRITE_BIT}.</p>
     *               
     *               <p>It is an error to specify {@link GL44#GL_MAP_COHERENT_BIT MAP_COHERENT_BIT} without also specifying {@link GL44#GL_MAP_PERSISTENT_BIT MAP_PERSISTENT_BIT}.</p>
     * 
     * @see <a href="https://docs.gl/gl4/glBufferStorage">Reference Page</a>
     */
	public static void glNamedBufferStorage(int buffer, long size, MemorySegment data, int flags)
	{
		try
		{
			mh_glNamedBufferStorage.invokeExact(buffer, size, data, flags);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glNamedBufferStorage failed", t);
		}
	}
	
	/**
     * Returns {@code n} previously unused buffer names in {@code buffers}, each representing a new buffer object initialized as if it had been bound to an
     * unspecified target.
     * 
     * @see <a href="https://docs.gl/gl4/glCreateBuffers">Reference Page</a>
     */
	public static int glCreateBuffers()
	{
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment ptr = arena.allocate(JAVA_INT, 1);
			
			mh_glCreateBuffers.invokeExact(1, ptr);
			
			return ptr.get(JAVA_INT, 0);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glCreateBuffers failed", t);
		}
	}
	
	/**
     * Returns {@code n} previously unused buffer names in {@code buffers}, each representing a new buffer object initialized as if it had been bound to an
     * unspecified target.
     * 
     * @see <a href="https://docs.gl/gl4/glCreateBuffers">Reference Page</a>
     */
	public static void glCreateBuffers(int count, MemorySegment ptr)
	{
		try
		{
			mh_glCreateBuffers.invokeExact(count, ptr);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glCreateBuffers failed", t);
		}
	}
	
	/**
     * Deletes named buffer objects.
     * 
     * @see <a href="https://docs.gl/gl4/glDeleteBuffers">Reference Page</a>
     */
	public static void glDeleteBuffers(int buffer)
	{
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment ptr = arena.allocate(JAVA_INT, 1);
			ptr.set(JAVA_INT, 0, buffer);
			
			mh_glDeleteBuffers.invokeExact(1, ptr);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDeleteBuffers failed", t);
		}
	}
	
	/**
     * Deletes named buffer objects.
     * 
     * @see <a href="https://docs.gl/gl4/glDeleteBuffers">Reference Page</a>
     */
	public static void glDeleteBuffers(int count, MemorySegment ptr)
	{
		try
		{
			mh_glDeleteBuffers.invokeExact(count, ptr);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDeleteBuffers failed", t);
		}
	}
	
	/**
     * DSA version of {@link GL31C#glCopyBufferSubData CopyBufferSubData}.
     *
     * @param readBuffer  the source buffer object name
     * @param writeBuffer the destination buffer object name
     * @param readOffset  the source buffer object offset, in bytes
     * @param writeOffset the destination buffer object offset, in bytes
     * @param size        the number of bytes to copy
     * 
     * @see <a href="https://docs.gl/gl4/glCopyBufferSubData">Reference Page</a>
     */
	public static void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size)
	{
		try
		{
			mh_glCopyNamedBufferSubData.invokeExact(readBuffer, writeBuffer, readOffset, writeOffset, size);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glCopyNamedBufferSubData failed", t);
		}
	}
	
	/**
     * DSA version of {@link GL43C#glBindVertexBuffer BindVertexBuffer}.
     *
     * @param vaobj        the vertex array object name
     * @param bindingindex the index of the vertex buffer binding point to which to bind the buffer
     * @param buffer       the name of an existing buffer to bind to the vertex buffer binding point
     * @param offset       the offset of the first element of the buffer
     * @param stride       the distance between elements within the buffer
     * 
     * @see <a href="https://docs.gl/gl4/glVertexArrayVertexBuffer">Reference Page</a>
     */
    public static void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride)
    {
    	try
		{
    		mh_glVertexArrayVertexBuffer.invokeExact(vaobj, bindingindex, buffer, offset, stride);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glVertexArrayVertexBuffer failed", t);
		}
    }
    
    /**
     * DSA version of {@link GL43C#glVertexAttribFormat VertexAttribFormat}.
     *
     * @param vaobj          the vertex array object name
     * @param attribindex    the generic vertex attribute array being described
     * @param size           the number of values per vertex that are stored in the array. One of:<br><table><tr><td>1</td><td>2</td><td>3</td><td>4</td><td>{@link GL12#GL_BGRA BGRA}</td></tr></table>
     * @param type           the type of the data stored in the array
     * @param normalized     if true then integer data is normalized to the range [-1, 1] or [0, 1] if it is signed or unsigned, respectively. If false then integer data is
     *                       directly converted to floating point.
     * @param relativeoffset the offset, measured in basic machine units of the first element relative to the start of the vertex buffer binding this attribute fetches from
     * 
     * @see <a href="https://docs.gl/gl4/glVertexArrayAttribFormat">Reference Page</a>
     */
    public static void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset)
    {
    	try
		{
    		mh_glVertexArrayAttribFormat.invokeExact(vaobj, attribindex, size, type, normalized, relativeoffset);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glVertexArrayAttribFormat failed", t);
		}
    }
    
    /**
     * DSA version of {@link GL43C#glVertexAttribBinding VertexAttribBinding}.
     *
     * @param vaobj        the vertex array object name
     * @param attribindex  the index of the attribute to associate with a vertex buffer binding
     * @param bindingindex the index of the vertex buffer binding with which to associate the generic vertex attribute
     * 
     * @see <a href="https://docs.gl/gl4/glVertexArrayAttribBinding">Reference Page</a>
     */
    public static void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex)
    {
    	try
		{
    		mh_glVertexArrayAttribBinding.invokeExact(vaobj, attribindex, bindingindex);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glVertexArrayAttribBinding failed", t);
		}
    }
    
    /**
     * Deletes vertex array objects.
     * 
     * @see <a href="https://docs.gl/gl4/glDeleteVertexArrays">Reference Page</a>
     */
    public static void glDeleteVertexArrays(int count, MemorySegment ptr)
    {
    	try
		{
			mh_glDeleteVertexArrays.invokeExact(1, ptr);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDeleteVertexArrays failed", t);
		}
    }
    
    /**
     * Deletes vertex array objects.
     * 
     * @see <a href="https://docs.gl/gl4/glDeleteVertexArrays">Reference Page</a>
     */
    public static void glDeleteVertexArrays(int array)
    {
    	try(Arena arena = Arena.ofConfined())
		{
			MemorySegment ptr = arena.allocate(JAVA_INT, 1);
			ptr.set(JAVA_INT, 0, array);
			
			mh_glDeleteVertexArrays.invokeExact(1, ptr);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDeleteVertexArrays failed", t);
		}
    }
    
    /**
     * Enables a generic vertex attribute array.
     *
     * @param index the index of the generic vertex attribute to be enabled
     * 
     * @see <a href="https://docs.gl/gl4/glEnableVertexAttribArray">Reference Page</a>
     */
    public static void glEnableVertexAttribArray(int index)
    {
    	try
		{
			mh_glEnableVertexAttribArray.invokeExact(index);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glEnableVertexAttribArray failed", t);
		}
    }

    /**
     * Disables a generic vertex attribute array.
     *
     * @param index the index of the generic vertex attribute to be disabled
     * 
     * @see <a href="https://docs.gl/gl4/glDisableVertexAttribArray">Reference Page</a>
     */
    public static void glDisableVertexAttribArray(int index)
    {
    	try
		{
			mh_glDisableVertexAttribArray.invokeExact(index);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDisableVertexAttribArray failed", t);
		}
    }
    
    /**
     * Constructs a sequence of geometric primitives by successively transferring elements for {@code count} vertices to the GL.
     * The i<sup>th</sup> element transferred by {@code DrawElements} will be taken from element {@code indices[i]} (if no element array buffer is bound), or
     * from the element whose index is stored in the currently bound element array buffer at offset {@code indices + i}.
     *
     * @param mode    the kind of primitives being constructed. One of:<br><table><tr><td>{@link #GL_POINTS POINTS}</td><td>{@link #GL_LINE_STRIP LINE_STRIP}</td><td>{@link #GL_LINE_LOOP LINE_LOOP}</td><td>{@link #GL_LINES LINES}</td><td>{@link #GL_TRIANGLE_STRIP TRIANGLE_STRIP}</td><td>{@link #GL_TRIANGLE_FAN TRIANGLE_FAN}</td></tr><tr><td>{@link #GL_TRIANGLES TRIANGLES}</td><td>{@link GL32#GL_LINES_ADJACENCY LINES_ADJACENCY}</td><td>{@link GL32#GL_LINE_STRIP_ADJACENCY LINE_STRIP_ADJACENCY}</td><td>{@link GL32#GL_TRIANGLES_ADJACENCY TRIANGLES_ADJACENCY}</td><td>{@link GL32#GL_TRIANGLE_STRIP_ADJACENCY TRIANGLE_STRIP_ADJACENCY}</td><td>{@link GL40#GL_PATCHES PATCHES}</td></tr></table>
     * @param count   the number of vertices to transfer to the GL
     * @param type    indicates the type of index values in {@code indices}. One of:<br><table><tr><td>{@link #GL_UNSIGNED_BYTE UNSIGNED_BYTE}</td><td>{@link #GL_UNSIGNED_SHORT UNSIGNED_SHORT}</td><td>{@link #GL_UNSIGNED_INT UNSIGNED_INT}</td></tr></table>
     * @param indices the index values
     * 
     * @see <a href="https://docs.gl/gl4/glDrawElements">Reference Page</a>
     */
    public static void glDrawElements(int mode, int count, int type, MemorySegment indices)
    {
    	try
		{
			mh_glDrawElements.invokeExact(mode, count, type, indices);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDrawElements failed", t);
		}
    }
    
    /**
     * Constructs a sequence of geometric primitives by successively transferring elements for {@code count} vertices to the GL.
     * The i<sup>th</sup> element transferred by {@code DrawElements} will be taken from element {@code indices[i]} (if no element array buffer is bound), or
     * from the element whose index is stored in the currently bound element array buffer at offset {@code indices + i}.
     *
     * @param mode    the kind of primitives being constructed. One of:<br><table><tr><td>{@link #GL_POINTS POINTS}</td><td>{@link #GL_LINE_STRIP LINE_STRIP}</td><td>{@link #GL_LINE_LOOP LINE_LOOP}</td><td>{@link #GL_LINES LINES}</td><td>{@link #GL_TRIANGLE_STRIP TRIANGLE_STRIP}</td><td>{@link #GL_TRIANGLE_FAN TRIANGLE_FAN}</td></tr><tr><td>{@link #GL_TRIANGLES TRIANGLES}</td><td>{@link GL32#GL_LINES_ADJACENCY LINES_ADJACENCY}</td><td>{@link GL32#GL_LINE_STRIP_ADJACENCY LINE_STRIP_ADJACENCY}</td><td>{@link GL32#GL_TRIANGLES_ADJACENCY TRIANGLES_ADJACENCY}</td><td>{@link GL32#GL_TRIANGLE_STRIP_ADJACENCY TRIANGLE_STRIP_ADJACENCY}</td><td>{@link GL40#GL_PATCHES PATCHES}</td></tr></table>
     * @param count   the number of vertices to transfer to the GL
     * @param type    indicates the type of index values in {@code indices}. One of:<br><table><tr><td>{@link #GL_UNSIGNED_BYTE UNSIGNED_BYTE}</td><td>{@link #GL_UNSIGNED_SHORT UNSIGNED_SHORT}</td><td>{@link #GL_UNSIGNED_INT UNSIGNED_INT}</td></tr></table>
     * @param indices the index values
     * 
     * @see <a href="https://docs.gl/gl4/glDrawElements">Reference Page</a>
     */
    public static void glDrawElements(int mode, int count, int type)
    {
    	try
		{
			mh_glDrawElements.invokeExact(mode, count, type, MemorySegment.NULL);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("glDrawElements failed", t);
		}
    }
}
