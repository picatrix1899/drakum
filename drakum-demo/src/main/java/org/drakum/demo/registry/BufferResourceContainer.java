package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.VknContext;

public class BufferResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	public MemoryResourceContainer memory;
	
	@Override
	public void close()
	{
		vkDestroyBuffer(this.context.gpu.handle(), this.handle.handle(), null);
		
		memory.close();
	}
}
