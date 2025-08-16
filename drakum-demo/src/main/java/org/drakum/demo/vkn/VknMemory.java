package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.drakum.demo.registry.LongId;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;

public class VknMemory
{
	private final VknContext context;
	
	private LongId handle;
	private long mappedHandle = VK_NULL_HANDLE;
	
	private final long size;
	
	public VknMemory(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			this.size = settings.size;
			
			VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
			memoryAllocateInfo.sType$Default();
			memoryAllocateInfo.allocationSize(this.size);
			memoryAllocateInfo.memoryTypeIndex(this.context.gpu.findMemoryType(settings.memoryTypeBits, settings.properties, stack));

			this.handle = new LongId(VknInternalUtils.allocateMemory(this.context, memoryAllocateInfo, stack));
		}
	}
	
	public LongId handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public long mappedHandle()
	{
		ensureValid();
		
		return this.mappedHandle;
	}
	
	public boolean isMapped()
	{
		ensureValid();
		
		return this.mappedHandle != VK_NULL_HANDLE;
	}
	
	public void map()
	{
		ensureValid();
		
		if(this.mappedHandle != VK_NULL_HANDLE) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.mappedHandle = VknInternalUtils.mapMemory(this.context, this.handle.handle(), 0, size, 0, stack);
		}
	}
	
	public void unmap()
	{
		ensureValid();
		
		if(this.mappedHandle == VK_NULL_HANDLE) return;
		
		vkUnmapMemory(this.context.gpu.handle(), this.handle.handle());
		
		this.mappedHandle = VK_NULL_HANDLE;
	}
	
	public boolean isValid()
	{
		return this.handle.isValid();
	}

	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public void close()
	{
		unmap();
		
		vkFreeMemory(this.context.gpu.handle(), this.handle.handle(), null);
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private long size;
		private int memoryTypeBits;
		private int properties;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings size(long size)
		{
			this.size = size;
			
			return this;
		}
		
		public long size()
		{
			return this.size;
		}
		
		public Settings memoryTypeBits(int memoryTypeBits)
		{
			this.memoryTypeBits = memoryTypeBits;
			
			return this;
		}
		
		public int memoryTypeBits()
		{
			return this.memoryTypeBits;
		}
		
		public Settings properties(int properties)
		{
			this.properties = properties;
			
			return this;
		}
		
		public int properties()
		{
			return this.properties;
		}
		
		public Settings propertyHostVisible()
		{
			this.properties |= VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
			
			return this;
		}
		
		public Settings propertyHostCoherent()
		{
			this.properties |= VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
			
			return this;
		}
	}
}