package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VknBuffer
{
	private final VknContext context;
	
	private long handle;
	private VknMemory memory;
	
	public VknBuffer(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
			bufferCreateInfo.sType$Default();
			bufferCreateInfo.size(settings.size);
			bufferCreateInfo.usage(settings.usage);
			bufferCreateInfo.sharingMode(settings.sharingMode);
			
			this.handle = VknInternalUtils.createBuffer(this.context.gpu.handle(), bufferCreateInfo, stack);
			
			VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
			
			vkGetBufferMemoryRequirements(this.context.gpu.handle(), this.handle, memoryRequirements);

			this.memory = new VknMemory(new VknMemory.Settings(this.context).size(memoryRequirements.size()).memoryTypeBits(memoryRequirements.memoryTypeBits()).properties(settings.properties));
			
			vkBindBufferMemory(this.context.gpu.handle(), this.handle, this.memory.handle(), 0);
		}
	}
	
	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public long memoryHandle()
	{
		ensureValid();
		
		return this.memory.handle();
	}
	
	public long mappedMemoryHandle()
	{
		ensureValid();
		
		return this.memory.mappedHandle();
	}
	
	public boolean isMapped()
	{
		ensureValid();
		
		return this.memory.isMapped();
	}
	
	public void map()
	{
		ensureValid();
		
		this.memory.map();
	}
	
	public void unmap()
	{
		ensureValid();
		
		this.memory.unmap();
	}

	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		unmap();
		
		vkDestroyBuffer(this.context.gpu.handle(), this.handle, null);
		this.memory.close();
		
		this.handle = VK_NULL_HANDLE;
	}

	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		private VknWindow window;
		private long size;
		private int usage;
		private int sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		private int properties;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings window(VknWindow window)
		{
			this.window = window;
			
			return this;
		}
		
		public VknWindow window()
		{
			return this.window;
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
		
		public Settings usage(int usage)
		{
			this.usage = usage;
			
			return this;
		}
		
		public int usage()
		{
			return this.usage;
		}
		
		public Settings usageTransferSrc()
		{
			this.usage |= VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
			
			return this;
		}
		
		public Settings usageTransferDst()
		{
			this.usage |= VK_BUFFER_USAGE_TRANSFER_DST_BIT;
			
			return this;
		}
		
		public Settings usageVertexBuffer()
		{
			this.usage |= VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
			
			return this;
		}
		
		public Settings usageIndexBuffer()
		{
			this.usage |= VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
			
			return this;
		}
		
		public Settings sharingMode(int sharingMode)
		{
			this.sharingMode = sharingMode;
			
			return this;
		}
		
		public int sharingMode()
		{
			return this.sharingMode;
		}
		
		public Settings shareExclusive()
		{
			this.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
			
			return this;
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
		
		public Settings propertyDeviceLocal()
		{
			this.properties |= VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
			
			return this;
		}
	}
}
