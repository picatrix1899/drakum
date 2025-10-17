package org.drakum.demo.vkn;

import org.barghos.glfw.window.GlfwWindow;
import org.barghos.util.container.ints.Extent2I;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;

public class VknWindow
{
	private final VknContext context;
	
	private GlfwWindow windowShell;
	private VknSurface surface;
	public VknSwapchain swapchain;
	public int inFlightFrameCount;
	public Extent2I framebufferExtent;
	
	public VknWindow(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			GlfwWindow.Settings windowCreateSettings = new GlfwWindow.Settings();
			windowCreateSettings.windowWidth = settings.width;
			windowCreateSettings.windowHeight = settings.height;
			windowCreateSettings.title = settings.title;
			
			GlfwWindow windowShell = GlfwWindow.create(windowCreateSettings);
			
			VknSurface.Settings surfaceCreateSettings = new VknSurface.Settings(this.context);
			surfaceCreateSettings.window = windowShell;
			surfaceCreateSettings.physicalGPU = settings.physicalGpu;
			
			VknSurface surface = new VknSurface(surfaceCreateSettings);
			
			VknSurfaceCapabilities surfaceCapabilities = surface.capabilities();

			VkExtent2D actualExtent = VknInternalUtils.getFramebufferSize(windowShell.handle(), stack);

			VkExtent2D vkFramebufferExtent = VkExtent2D.calloc();

			vkFramebufferExtent.width(Math.clamp(actualExtent.width(), surfaceCapabilities.minImageExtent.width(), surfaceCapabilities.maxImageExtent.width()));
			vkFramebufferExtent.height(Math.clamp(actualExtent.height(), surfaceCapabilities.minImageExtent.height(), surfaceCapabilities.maxImageExtent.height()));

			Extent2I framebufferExtent = new Extent2I(vkFramebufferExtent.width(), vkFramebufferExtent.height());
			
			int imageCount = surfaceCapabilities.minImageCount + 1;
			if (surfaceCapabilities.maxImageCount > 0)
			{
				imageCount = Math.clamp(imageCount, surfaceCapabilities.minImageCount, surfaceCapabilities.maxImageCount);
			}
			
			int swapchainImageCount = imageCount;
			
			VknSwapchain.Settings swapchainCreateSettings = new VknSwapchain.Settings(CommonRenderContext.context);
			swapchainCreateSettings.swapchainImageCount = swapchainImageCount;
			swapchainCreateSettings.surface = surface;
			swapchainCreateSettings.inFlightFrameCount = settings.inFlightFrameCount;
			swapchainCreateSettings.framebufferExtent = framebufferExtent;
			
			VknSwapchain swapchain = new VknSwapchain(swapchainCreateSettings);
			
			this.windowShell = windowShell;
			this.surface = surface;
			this.swapchain = swapchain;
			this.framebufferExtent = framebufferExtent;
			this.inFlightFrameCount = settings.inFlightFrameCount;
		}
	}
	
	public void close()
	{
		this.swapchain.close();
		this.surface.close();
		this.windowShell.releaseResources();
	}
	
	public GlfwWindow windowShell()
	{
		return this.windowShell;
	}
	
	public VknSurface surface()
	{
		return this.surface;
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private VknPhysicalGPU physicalGpu;
		private int width;
		private int height;
		private String title;
		private int inFlightFrameCount;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings physicalGpu(VknPhysicalGPU physicalGpu)
		{
			this.physicalGpu = physicalGpu;
			
			return this;
		}
		
		public VknPhysicalGPU physicalGpu()
		{
			return this.physicalGpu;
		}
		
		public Settings size(int width, int height)
		{
			this.width = width;
			this.height = height;
			
			return this;
		}
		
		public Settings width(int width)
		{
			this.width = width;
			
			return this;
		}
		
		public int width()
		{
			return this.width;
		}
		
		public Settings height(int height)
		{
			this.height = height;
			
			return this;
		}
		
		public int height()
		{
			return this.height;
		}
		
		public Settings title(String title)
		{
			this.title = title;
			
			return this;
		}
		
		public String title()
		{
			return this.title;
		}
		
		public Settings inFlightFrameCount(int count)
		{
			this.inFlightFrameCount = count;
			
			return this;
		}
		
		public int inFlightFrameCount()
		{
			return this.inFlightFrameCount;
		}
	}
}
