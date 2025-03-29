package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.barghos.util.nullable.longs.NullableL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VulkanBuffer
{
	private long handle;
	private long memoryHandle;
	private NullableL mappedMemoryHandle = new NullableL();
	private long size;
	
	public long handle()
	{
		return this.handle;
	}
	
	public long memoryHandle()
	{
		return this.memoryHandle;
	}
	
	public long mappedMemoryHandle()
	{
		return this.mappedMemoryHandle.valueL();
	}
	
	public boolean isMapped()
	{
		return this.mappedMemoryHandle.isNotNull();
	}
	
	public void map()
	{
		if(this.isMapped()) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			mappedMemoryHandle.value(Utils.mapMemory(CommonRenderContext.instance().gpu.device, this.memoryHandle, 0, size, 0, stack));
		}
	}
	
	public void unmap()
	{
		if(!this.isMapped()) return;
		
		if(this.mappedMemoryHandle.isNotNull()) vkUnmapMemory(CommonRenderContext.instance().gpu.device, this.memoryHandle);
		this.mappedMemoryHandle.setNull();
	}
	
	public void __release()
	{
		if(this.mappedMemoryHandle.isNotNull()) unmap();
		vkDestroyBuffer(CommonRenderContext.instance().gpu.device, handle, null);
		vkFreeMemory(CommonRenderContext.instance().gpu.device, memoryHandle, null);
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
				
				VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
				memoryAllocateInfo.sType$Default();
				memoryAllocateInfo.allocationSize(memoryRequirements.size());
				memoryAllocateInfo.memoryTypeIndex(CommonRenderContext.instance().gpu.findMemoryType(memoryRequirements.memoryTypeBits(), this.properties, stack));
				
				long bufferMemory = Utils.allocateMemory(CommonRenderContext.instance().gpu.device, memoryAllocateInfo, stack);
				
				vkBindBufferMemory(CommonRenderContext.instance().gpu.device, buffer, bufferMemory, 0);
				
				VulkanBuffer result = new VulkanBuffer();
				result.handle = buffer;
				result.memoryHandle = bufferMemory;
				result.size = size;
				
				return result;
			}
		}
	}
}
