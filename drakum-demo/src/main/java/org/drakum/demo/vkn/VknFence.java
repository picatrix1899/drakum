package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class VknFence
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	public VknFence(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			int flags = 0;
			if(settings.isSignaled) flags |= VK_FENCE_CREATE_SIGNALED_BIT;
			
			VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack);
			fenceCreateInfo.sType$Default();
			fenceCreateInfo.flags(flags);
			
			this.handle = VknInternalUtils.createFence(this.context, fenceCreateInfo, stack);
		}
	}

	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public void waitFor()
	{
		ensureValid();
		
		VknInternalUtils.waitForFence(this.context, this.handle, true, Long.MAX_VALUE);
	}
	
	public void waitFor(long timeout)
	{
		ensureValid();
		
		VknInternalUtils.waitForFence(this.context, this.handle, true, timeout);
	}
	
	public void reset()
	{
		ensureValid();
		
		vkResetFences(this.context.gpu.handle(), this.handle);
	}

	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroyFence(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static void waitForAll(VknFence[] fences)
	{
		if(fences.length == 0) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{		
			VknContext context = null;
			
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				
				if(context != null && fence.isValid()) context = fence.context;
				
				fenceHandles[i] = fence.handle();
			}
				
			VknInternalUtils.waitForFences(context, fenceHandles, true, Long.MAX_VALUE, stack);
		}
	}
	
	public static void waitForAll(VknFence[] fences, long timeout)
	{
		if(fences.length == 0) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknContext context = null;
			
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				
				if(context != null && fence.isValid()) context = fence.context;
				
				fenceHandles[i] = fence.handle();
			}
				
			VknInternalUtils.waitForFences(context, fenceHandles, true, timeout, stack);
		}
	}
	
	public static void waitForOne(VknFence[] fences)
	{
		if(fences.length == 0) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknContext context = null;
			
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				
				if(context != null && fence.isValid()) context = fence.context;
				
				fenceHandles[i] = fence.handle();
			}
				
			VknInternalUtils.waitForFences(context, fenceHandles, false, Long.MAX_VALUE, stack);
		}
	}
	
	public static void waitForOne(VknFence[] fences, long timeout)
	{
		if(fences.length == 0) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknContext context = null;
			
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				
				if(context != null && fence.isValid()) context = fence.context;
				
				fenceHandles[i] = fence.handle();
			}
				
			VknInternalUtils.waitForFences(context, fenceHandles, false, timeout, stack);
		}
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private boolean isSignaled;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings isSignaled(boolean isSignaled)
		{
			this.isSignaled = isSignaled;
			
			return this;
		}
		
		public boolean isSignaled()
		{
			return this.isSignaled;
		}
		
		public Settings signaled()
		{
			this.isSignaled = true;
			
			return this;
		}
	}
}
