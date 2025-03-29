package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VulkanBuffer
{
	private long handle;
	private VulkanMemory memory;
	
	public long handle()
	{
		return this.handle;
	}
	
	public long memoryHandle()
	{
		return this.memory.handle();
	}
	
	public long mappedMemoryHandle()
	{
		return this.memory.mappedHandle();
	}
	
	public boolean isMapped()
	{
		return this.memory.isMapped();
	}
	
	public void map()
	{
		this.memory.map();
	}
	
	public void unmap()
	{
		this.memory.unmap();
	}
	
	public void __release()
	{
		vkDestroyBuffer(CommonRenderContext.instance().gpu.device, handle, null);
		this.memory.__release();
	}
	
	public static class Builder
	{
		private long size;
		private int usage;
		private int sharingMode;
		private int properties;
		
		public Builder size(long size)
		{
			this.size = size;
			
			return this;
		}
		
		public Builder usage(int usage)
		{
			this.usage = usage;
			
			return this;
		}
		
		public Builder sharingMode(int sharingMode)
		{
			this.sharingMode = sharingMode;
			
			return this;
		}
		
		public Builder properties(int properties)
		{
			this.properties = properties;
			
			return this;
		}
		
		public VulkanBuffer create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
				bufferCreateInfo.sType$Default();
				bufferCreateInfo.size(this.size);
				bufferCreateInfo.usage(this.usage);
				bufferCreateInfo.sharingMode(this.sharingMode);
				
				long buffer = Utils.createBuffer(CommonRenderContext.instance().gpu.device, bufferCreateInfo, stack);
				
				VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
				
				vkGetBufferMemoryRequirements(CommonRenderContext.instance().gpu.device, buffer, memoryRequirements);
				
				VulkanMemory memory = new VulkanMemory.Builder()
					.size(memoryRequirements.size())
					.memoryTypeBits(memoryRequirements.memoryTypeBits())
					.properties(this.properties).create();
				
				vkBindBufferMemory(CommonRenderContext.instance().gpu.device, buffer, memory.handle(), 0);
				
				VulkanBuffer result = new VulkanBuffer();
				result.handle = buffer;
				result.memory = memory;
				
				return result;
			}
		}
	}
}
