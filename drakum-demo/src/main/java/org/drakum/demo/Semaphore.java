package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

public class Semaphore
{
	private long handle;
	
	public long handle()
	{
		return this.handle;
	}
	
	public void __release()
	{
		vkDestroySemaphore(CommonRenderContext.instance().gpu.device, handle, null);
	}
	
	public static class Builder
	{
		public Semaphore create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
				semaphoreCreateInfo.sType$Default();

				long handle = Utils.createSemaphore(CommonRenderContext.instance().gpu.device, semaphoreCreateInfo, stack);
				
				Semaphore result = new Semaphore();
				result.handle = handle;
				
				return result;
			}
		}
	}
}
