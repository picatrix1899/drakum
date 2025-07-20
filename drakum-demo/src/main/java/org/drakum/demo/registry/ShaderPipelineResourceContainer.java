package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.VknContext;

public class ShaderPipelineResourceContainer implements IResourceContainer
{
	public LongHandle handle = new LongHandle();
	public VknContext context;
	
	
	@Override
	public void close()
	{
		vkDestroyPipeline(this.context.gpu.handle(), handle.handle, null);
	}

}
