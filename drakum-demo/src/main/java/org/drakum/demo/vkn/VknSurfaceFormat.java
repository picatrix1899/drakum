package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkSurfaceFormat2KHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class VknSurfaceFormat
{
	public final int format;
	public final int colorSpace;
	
	public VknSurfaceFormat(VkSurfaceFormat2KHR format)
	{
		VkSurfaceFormatKHR baseFormat = format.surfaceFormat();
		this.format = baseFormat.format();
		this.colorSpace = baseFormat.colorSpace();
	}
}
