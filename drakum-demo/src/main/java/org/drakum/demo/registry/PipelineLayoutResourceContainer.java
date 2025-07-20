package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.VknContext;

public class PipelineLayoutResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	
	@Override
	public void close()
	{
		long vkHandle = handle.getLongHandle();
		
		vkDestroyPipelineLayout(this.context.gpu.handle(), handle.handle(), null);
	}
}
