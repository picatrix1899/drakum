package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VulkanBuffer
{
	public long buffer;
	public long bufferMemory;
	
	public void createBuffer(GPU gpu, long size, int usage, int sharingMode, int properties)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
			bufferCreateInfo.sType$Default();
			bufferCreateInfo.size(size);
			bufferCreateInfo.usage(usage);
			bufferCreateInfo.sharingMode(sharingMode);
			
			this.buffer = Utils.createBuffer(gpu.device, bufferCreateInfo, stack);
			
			VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
			
			vkGetBufferMemoryRequirements(gpu.device, buffer, memoryRequirements);
			
			VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
			memoryAllocateInfo.sType$Default();
			memoryAllocateInfo.allocationSize(memoryRequirements.size());
			memoryAllocateInfo.memoryTypeIndex(gpu.findMemoryType(memoryRequirements.memoryTypeBits(), properties, stack));
			
			this.bufferMemory = Utils.allocateMemory(gpu.device, memoryAllocateInfo, stack);
			
			vkBindBufferMemory(gpu.device, buffer, bufferMemory, 0);
		}
	}
	
	public void __release(VkDevice device)
	{
		vkDestroyBuffer(device, buffer, null);
		vkFreeMemory(device, bufferMemory, null);
	}
}
