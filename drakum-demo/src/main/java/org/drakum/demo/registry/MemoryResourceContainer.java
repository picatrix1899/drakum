package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.VknContext;
import org.drakum.demo.vkn.VknMemory;

public class MemoryResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	public VknMemory object;
	
	@Override
	public void close()
	{
		if(object.isMapped()) vkUnmapMemory(this.context.gpu.handle(), this.handle.handle());
		
		vkFreeMemory(this.context.gpu.handle(), this.handle.handle(), null);
	}
}
