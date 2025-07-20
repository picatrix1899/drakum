package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

public class VknCommandPool
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	public VknCommandPool(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
			commandPoolCreateInfo.sType$Default();
			commandPoolCreateInfo.flags(settings.flags);
			commandPoolCreateInfo.queueFamilyIndex(settings.queueFamilyIndex);

			this.handle = VknInternalUtils.createCommandPool(this.context.gpu.handle(), commandPoolCreateInfo, stack);
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
		
		vkDestroyCommandPool(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private int flags;
		private int queueFamilyIndex;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings flags(int flags)
		{
			this.flags = flags;
			
			return this;
		}
		
		public int flags()
		{
			return this.flags;
		}
		
		public Settings queueFamilyIndex(int queueFamilyIndex)
		{
			this.queueFamilyIndex = queueFamilyIndex;
			
			return this;
		}
		
		public int queueFamilyIndex()
		{
			return this.queueFamilyIndex;
		}
	}
}
