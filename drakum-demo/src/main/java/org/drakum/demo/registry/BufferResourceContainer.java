package org.drakum.demo.registry;

import org.drakum.demo.vkn.VknContext;
import org.drakum.demo.vkn.VknInternalUtils;

public class BufferResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	public MemoryResourceContainer memory;
	
	@Override
	public void close()
	{
		VknInternalUtils.destroyBuffer(context, this.handle);
		
		memory.close();
	}
}
