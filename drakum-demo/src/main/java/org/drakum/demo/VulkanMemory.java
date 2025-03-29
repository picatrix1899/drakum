package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.barghos.util.nullable.longs.NullableL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;

public class VulkanMemory
{
	private long handle;
	private NullableL mappedHandle = new NullableL();
	private long size;
	
	public long handle()
	{
		return this.handle;
	}
	
	public long mappedHandle()
	{
		return this.mappedHandle.valueL();
	}
	
	public boolean isMapped()
	{
		return this.mappedHandle.isNotNull();
	}
	
	public void map()
	{
		if(this.isMapped()) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			mappedHandle.value(Utils.mapMemory(CommonRenderContext.instance().gpu.device, this.handle, 0, size, 0, stack));
		}
	}
	
	public void unmap()
	{
		if(!this.isMapped()) return;
		
		if(this.mappedHandle.isNotNull()) vkUnmapMemory(CommonRenderContext.instance().gpu.device, this.handle);
		this.mappedHandle.setNull();
	}
	
	public void __release()
	{
		if(this.mappedHandle.isNotNull()) unmap();
		vkFreeMemory(CommonRenderContext.instance().gpu.device, handle, null);
	}
	
	public static class Builder
	{
		private long size;
		private int memoryTypeBits;
		private int properties;
		
		public Builder size(long size)
		{
			this.size = size;
			
			return this;
		}
		
		public Builder memoryTypeBits(int memoryTypeBits)
		{
			this.memoryTypeBits = memoryTypeBits;
			
			return this;
		}
		
		public Builder properties(int properties)
		{
			this.properties = properties;
			
			return this;
		}
		
		public VulkanMemory create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
				memoryAllocateInfo.sType$Default();
				memoryAllocateInfo.allocationSize(size);
				memoryAllocateInfo.memoryTypeIndex(CommonRenderContext.instance().gpu.findMemoryType(memoryTypeBits, this.properties, stack));
				
				long handle = Utils.allocateMemory(CommonRenderContext.instance().gpu.device, memoryAllocateInfo, stack);
				
				VulkanMemory result = new VulkanMemory();
				result.handle = handle;
				result.size = size;
				
				return result;
			}
		}
	}
}
