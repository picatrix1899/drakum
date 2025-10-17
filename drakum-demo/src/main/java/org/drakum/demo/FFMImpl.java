package org.drakum.demo;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import static java.lang.foreign.ValueLayout.*;
import java.lang.invoke.MethodHandle;

public class FFMImpl
{
	private static final Linker LINKER = Linker.nativeLinker();

	private static final String name_glfwGetRequiredInstanceExtensions = "glfwGetRequiredInstanceExtensions";
	private static MethodHandle ffm_glfwGetRequiredInstanceExtensions;
	
	public static void init()
	{
		SymbolLookup glfwLookup = SymbolLookup.libraryLookup("glfw", Arena.global());
		
		ffm_glfwGetRequiredInstanceExtensions = LINKER.downcallHandle(glfwLookup.findOrThrow(name_glfwGetRequiredInstanceExtensions), FunctionDescriptor.of(ADDRESS, ADDRESS));
	}

	public static MemorySegment glfwGetRequiredInstanceExtensions()
	{
		try(Arena arena = Arena.ofConfined())
		{
			MemorySegment fn_result;
			
			MemorySegment fn_refParam_count = arena.allocate(JAVA_INT);

			try
			{
				fn_result = (MemorySegment) ffm_glfwGetRequiredInstanceExtensions.invokeExact(fn_refParam_count);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(name_glfwGetRequiredInstanceExtensions + " failed", e);
			}

			int count = fn_refParam_count.get(JAVA_INT, 0);
			
			return fn_result.reinterpret(count * ADDRESS.byteSize());
		}
	}
}
