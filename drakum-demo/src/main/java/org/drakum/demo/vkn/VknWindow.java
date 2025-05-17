package org.drakum.demo.vkn;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;

public class VknWindow
{
	private VknWindowShell windowShell;
	private VknSurface surface;
	public VknSwapchain swapchain;
	public int inFlightFrameCount;
	public VknExtent2D framebufferExtent;
	
	public void __release()
	{
		this.swapchain.__release();
		this.surface.__release();
		this.windowShell.__release();
	}
	
	public VknWindowShell windowShell()
	{
		return this.windowShell;
	}
	
	public VknSurface surface()
	{
		return this.surface;
	}
	
	public static VknWindow create(CreateSettings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknWindowShell.CreateSettings windowCreateSettings = new VknWindowShell.CreateSettings();
			windowCreateSettings.width = settings.width;
			windowCreateSettings.height = settings.height;
			windowCreateSettings.title = settings.title;
			
			VknWindowShell windowShell = VknWindowShell.create(windowCreateSettings);
			
			VknSurface.CreateSettings surfaceCreateSettings = new VknSurface.CreateSettings();
			surfaceCreateSettings.window = windowShell;
			surfaceCreateSettings.physicalGPU = settings.physicalGpu;
			
			VknSurface surface = VknSurface.create(surfaceCreateSettings);
			
			VknSurfaceCapabilities surfaceCapabilities = surface.capabilities();

			VkExtent2D actualExtent = VknInternalUtils.getFramebufferSize(windowShell.handle(), stack);

			VkExtent2D vkFramebufferExtent = VkExtent2D.calloc();

			vkFramebufferExtent.width(Math.clamp(actualExtent.width(), surfaceCapabilities.minImageExtent.width, surfaceCapabilities.maxImageExtent.width));
			vkFramebufferExtent.height(Math.clamp(actualExtent.height(), surfaceCapabilities.minImageExtent.height, surfaceCapabilities.maxImageExtent.height));

			VknExtent2D framebufferExtent = new VknExtent2D(vkFramebufferExtent);
			
			int imageCount = surfaceCapabilities.minImageCount + 1;
			if (surfaceCapabilities.maxImageCount > 0)
			{
				imageCount = Math.clamp(imageCount, surfaceCapabilities.minImageCount, surfaceCapabilities.maxImageCount);
			}
			
			int swapchainImageCount = imageCount;
			
			VknSwapchain.CreateSettings swapchainCreateSettings = new VknSwapchain.CreateSettings();
			swapchainCreateSettings.swapchainImageCount = swapchainImageCount;
			swapchainCreateSettings.surface = surface;
			swapchainCreateSettings.inFlightFrameCount = settings.inFlightFrameCount;
			swapchainCreateSettings.framebufferExtent = framebufferExtent;
			
			VknSwapchain swapchain = VknSwapchain.create(swapchainCreateSettings);
			
			VknWindow result = new VknWindow();
			result.windowShell = windowShell;
			result.surface = surface;
			result.swapchain = swapchain;
			result.framebufferExtent = framebufferExtent;
			result.inFlightFrameCount = settings.inFlightFrameCount;
			
			return result;
		}
	}
	
	public static class CreateSettings
	{
		public VknPhysicalGPU physicalGpu;
		public int width;
		public int height;
		public String title;
		public int inFlightFrameCount;
	}
}
