package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknContext;

public class PipelineResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	
	@Override
	public void close()
	{
		long vkHandle = handle.getLongHandle();
		
		vkDestroyPipeline(CommonRenderContext.context.gpu.handle(), vkHandle, null);
		
		HandleRegistry.removeLong(handle.handle());
	}

}
