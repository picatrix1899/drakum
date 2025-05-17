package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

public class VknSemaphore
{
	private long handle;
	
	public long handle()
	{
		return this.handle;
	}
	
	public void __release()
	{
		vkDestroySemaphore(CommonRenderContext.gpu.handle(), handle, null);
	}
	
	public static VknSemaphore create()
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
			semaphoreCreateInfo.sType$Default();
			
			long handle = VknInternalUtils.createSemaphore(CommonRenderContext.gpu.handle(), semaphoreCreateInfo, stack);
			
			VknSemaphore result = new VknSemaphore();
			result.handle = handle;
			
			return result;
		}
	}
}
