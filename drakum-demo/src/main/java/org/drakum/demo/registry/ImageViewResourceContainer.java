package org.drakum.demo.registry;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.vkn.VknContext;

public class ImageViewResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	
	@Override
	public void close()
	{
		vkDestroyImageView(this.context.gpu.handle(), this.handle.handle(), null);
	}
}
