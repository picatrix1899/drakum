package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class Fence
{
	private long handle;
	
	public long handle()
	{
		return this.handle;
	}
	
	public void waitFor()
	{
		vkWaitForFences(CommonRenderContext.instance().gpu.device, this.handle, true, Long.MAX_VALUE);
	}
	
	public void reset()
	{
		vkResetFences(CommonRenderContext.instance().gpu.device, this.handle);
	}
	
	public void __release()
	{
		vkDestroyFence(CommonRenderContext.instance().gpu.device, this.handle, null);
	}
	
	public static class Builder
	{
		private boolean isSignaled;
		
		public Builder signaled()
		{
			this.isSignaled = true;
			
			return this;
		}
		
		public Fence create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				int flags = 0;
				if(this.isSignaled) flags |= VK_FENCE_CREATE_SIGNALED_BIT;
				
				VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack);
				fenceCreateInfo.sType$Default();
				fenceCreateInfo.flags(flags);

				long handle = Utils.createFence(CommonRenderContext.instance().gpu.device, fenceCreateInfo, stack);
				
				Fence result = new Fence();
				result.handle = handle;
				
				return result;
			}
		}
	}
}
