package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkExtent3D;

public class VknExtent3D
{
	public final int width;
	public final int height;
	public final int depth;
	
	public VknExtent3D(VkExtent3D extent)
	{
		this.width = extent.width();
		this.height = extent.height();
		this.depth = extent.depth();
	}
}
