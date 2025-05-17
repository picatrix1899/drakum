package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.ref.Cleaner.Cleanable;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class VknFence
{
	private long handle = VK_NULL_HANDLE;
	private final Cleanable cleanable;
	
	public VknFence(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			int flags = 0;
			if(settings.isSignaled) flags |= VK_FENCE_CREATE_SIGNALED_BIT;
			
			VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack);
			fenceCreateInfo.sType$Default();
			fenceCreateInfo.flags(flags);
			
			long handle = VknInternalUtils.createFence(CommonRenderContext.gpu.handle(), fenceCreateInfo, stack);
			this.handle = handle;
			
			this.cleanable = VknCleanerUtils.CLEANER.register(this, () -> {
				if(this.handle != VK_NULL_HANDLE) vkDestroyFence(CommonRenderContext.gpu.handle(), this.handle, null);
				
				this.handle = VK_NULL_HANDLE;
			});
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
		
		VknInternalUtils.waitForFence(CommonRenderContext.gpu.handle(), this.handle, true, Long.MAX_VALUE);
	}
	
	public void waitFor(long timeout)
	{
		ensureValid();
		
		VknInternalUtils.waitForFence(CommonRenderContext.gpu.handle(), this.handle, true, timeout);
	}
	
	public void reset()
	{
		ensureValid();
		
		vkResetFences(CommonRenderContext.gpu.handle(), this.handle);
	}

	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		this.cleanable.clean();
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static void waitForAll(VknFence[] fences)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				fenceHandles[i] = fence.handle;
			}
				
			VknInternalUtils.waitForFences(CommonRenderContext.gpu.handle(), fenceHandles, true, Long.MAX_VALUE, stack);
		}
	}
	
	public static void waitForAll(VknFence[] fences, long timeout)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				fenceHandles[i] = fence.handle;
			}
				
			VknInternalUtils.waitForFences(CommonRenderContext.gpu.handle(), fenceHandles, true, timeout, stack);
		}
	}
	
	public static void waitForOne(VknFence[] fences)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				fenceHandles[i] = fence.handle;
			}
				
			VknInternalUtils.waitForFences(CommonRenderContext.gpu.handle(), fenceHandles, false, Long.MAX_VALUE, stack);
		}
	}
	
	public static void waitForOne(VknFence[] fences, long timeout)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			long[] fenceHandles = new long[fences.length];
			
			for(int i = 0; i < fences.length; i++)
			{
				VknFence fence = fences[i];
				fenceHandles[i] = fence.handle;
			}
				
			VknInternalUtils.waitForFences(CommonRenderContext.gpu.handle(), fenceHandles, false, timeout, stack);
		}
	}
	
	public static class Settings
	{
		public boolean isSignaled;
	}
}
