package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.ref.Cleaner.Cleanable;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;

public class VknMemory
{
	private long handle;
	private long mappedHandle = VK_NULL_HANDLE;
	private final long size;
	private final Cleanable cleanable;
	
	public VknMemory(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
			memoryAllocateInfo.sType$Default();
			memoryAllocateInfo.allocationSize(settings.size);
			memoryAllocateInfo.memoryTypeIndex(CommonRenderContext.gpu.findMemoryType(settings.memoryTypeBits, settings.properties, stack));

			this.handle = VknInternalUtils.allocateMemory(CommonRenderContext.gpu.handle(), memoryAllocateInfo, stack);
			this.size = settings.size;
			
			this.cleanable = VknCleanerUtils.CLEANER.register(this, () -> {
				if(this.mappedHandle != VK_NULL_HANDLE) unmap();
				vkFreeMemory(CommonRenderContext.gpu.handle(), handle, null);
				
				this.handle = VK_NULL_HANDLE;
			});
		}
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public long mappedHandle()
	{
		return this.mappedHandle;
	}
	
	public boolean isMapped()
	{
		return this.mappedHandle != VK_NULL_HANDLE;
	}
	
	public void map()
	{
		if(this.isMapped()) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.mappedHandle = VknInternalUtils.mapMemory(CommonRenderContext.gpu.handle(), this.handle, 0, size, 0, stack);
		}
	}
	
	public void unmap()
	{
		if(!this.isMapped()) return;
		
		if(this.mappedHandle != VK_NULL_HANDLE) vkUnmapMemory(CommonRenderContext.gpu.handle(), this.handle);
		this.mappedHandle = VK_NULL_HANDLE;
	}
	
	public void close()
	{
		this.cleanable.clean();
	}
	
	public static class Settings
	{
		public long size;
		public int memoryTypeBits;
		public int properties;
	}
}
