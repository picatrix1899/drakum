package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkMemoryType;

public class VknMemoryType
{
	public final int heapIndex;
	public final int propertyFlags;
	
	public VknMemoryType(VkMemoryType memoryType)
	{
		this.heapIndex = memoryType.heapIndex();
		this.propertyFlags = memoryType.propertyFlags();
	}
}
