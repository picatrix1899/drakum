package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkExtent2D;

public class VknExtent2D
{
	public final int width;
	public final int height;
	
	public VknExtent2D(VkExtent2D extent)
	{
		this.width = extent.width();
		this.height = extent.height();
	}
}
