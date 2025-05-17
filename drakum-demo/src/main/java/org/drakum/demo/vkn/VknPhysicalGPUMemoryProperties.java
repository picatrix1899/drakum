package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkMemoryType;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties2;

public class VknPhysicalGPUMemoryProperties
{
	public final VknMemoryType[] memoryTypes;
	
	public VknPhysicalGPUMemoryProperties(VkPhysicalDeviceMemoryProperties2 properties)
	{
		VkPhysicalDeviceMemoryProperties baseProperties = properties.memoryProperties();
		
		VkMemoryType.Buffer memoryTypesBuf = baseProperties.memoryTypes();
		VknMemoryType[] memoryTypes = new VknMemoryType[memoryTypesBuf.remaining()];
		for(int i = 0; i < memoryTypesBuf.remaining(); i++)
		{
			memoryTypes[i] = new VknMemoryType(memoryTypesBuf.get(i));
		}
		
		this.memoryTypes = memoryTypes;
	}
}
