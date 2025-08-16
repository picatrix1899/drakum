package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreTypeCreateInfo;

public class VknSemaphore
{
	private final VknContext context;
	
	private long handle;
	
	public VknSemaphore(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkSemaphoreTypeCreateInfo semaphoreTypeCreateInfo = null;
			if(settings.useTypeCreate)
			{
				semaphoreTypeCreateInfo = VkSemaphoreTypeCreateInfo.calloc(stack);
				semaphoreTypeCreateInfo.sType$Default();
				semaphoreTypeCreateInfo.initialValue(settings.initialValue);
				semaphoreTypeCreateInfo.semaphoreType(settings.type);
			}
			
			VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreCreateInfo.sType$Default();
			if(semaphoreTypeCreateInfo != null) semaphoreCreateInfo.pNext(semaphoreTypeCreateInfo);
			
			this.handle = VknInternalUtils.createSemaphore(this.context, semaphoreCreateInfo, stack);
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
		
		vkDestroySemaphore(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}

	public static class Settings
	{
		private final VknContext context;
		
		private boolean useTypeCreate;
		private long initialValue;
		private int type;

		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public int type()
		{
			return this.type;
		}
		
		public Settings type(int type)
		{
			this.type = type;
			this.useTypeCreate = true;
			
			return this;
		}
		
		public Settings timeline()
		{
			this.type = VK_SEMAPHORE_TYPE_TIMELINE;
			this.useTypeCreate = true;
			
			return this;
		}
		
		public Settings binary()
		{
			this.type = VK_SEMAPHORE_TYPE_BINARY;
			this.useTypeCreate = true;
			
			return this;
		}
		
		public long initialValue()
		{
			return this.initialValue;
		}
		
		public Settings initialValue(long value)
		{
			this.initialValue = value;
			this.useTypeCreate = true;
			
			return this;
		}
	}
	
	public static enum Type
	{
		BINARY,
		TIMELINE
		;
	}
}
