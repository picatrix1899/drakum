package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;

public class VknSimpleDescriptorPool
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	public VknSimpleDescriptorPool(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkDescriptorPoolSize.Buffer descriptorPoolSize = VkDescriptorPoolSize.calloc(1, stack);
			descriptorPoolSize.get(0).type(settings.type);
			descriptorPoolSize.get(0).descriptorCount(settings.descriptorCount * settings.setCount);
			
			VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack);
			descriptorPoolCreateInfo.sType$Default();
			descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
			descriptorPoolCreateInfo.maxSets(settings.setCount);
			
			this.handle = VknInternalUtils.createDescriptorPool(CommonRenderContext.context.gpu.handle(), descriptorPoolCreateInfo, stack);
		}
	}
	
	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroyDescriptorPool(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private int type;
		private int descriptorCount = 1;
		private int setCount = 1;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings type(int type)
		{
			this.type = type;
			
			return this;
		}
		
		public int type()
		{
			return this.type;
		}
		
		public Settings descriptorCount(int count)
		{
			this.descriptorCount = count;
			
			return this;
		}
		
		public int descriptorCount()
		{
			return this.descriptorCount;
		}
		
		public Settings setCount(int count)
		{
			this.setCount = count;
			
			return this;
		}
		
		public int setCount()
		{
			return this.setCount;
		}
	}
}
