package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class VulkanBuffer
{
	public long buffer;
	public long bufferMemory;
	
	public void createBuffer(VkDevice device, VkPhysicalDevice physicalDevice, long size, int usage, int sharingMode, int properties)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
			bufferCreateInfo.sType$Default();
			bufferCreateInfo.size(size);
			bufferCreateInfo.usage(usage);
			bufferCreateInfo.sharingMode(sharingMode);
			
			this.buffer = Utils.createBuffer(device, bufferCreateInfo, stack);
			
			VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
			
			vkGetBufferMemoryRequirements(device, buffer, memoryRequirements);
			
			VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
			memoryAllocateInfo.sType$Default();
			memoryAllocateInfo.allocationSize(memoryRequirements.size());
			memoryAllocateInfo.memoryTypeIndex(findMemoryType(memoryRequirements.memoryTypeBits(), properties, physicalDevice, stack));
			
			this.bufferMemory = Utils.allocateMemory(device, memoryAllocateInfo, stack);
			
			vkBindBufferMemory(device, buffer, bufferMemory, 0);
		}
	}
	
	public int findMemoryType(int typeFilter, int properties, VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc(stack);
		
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
		
		for(int i = 0; i < memoryProperties.memoryTypeCount(); i++)
		{
			if((typeFilter & (1 << i)) != 0 && (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)
			{
				return i;
			}
		}
		
		throw new Error();
	}
	
	public void __release(VkDevice device)
	{
		vkDestroyBuffer(device, buffer, null);
		vkFreeMemory(device, bufferMemory, null);
	}
}
