package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;

public class VknSurface
{
	private long handle;
	private VknSurfaceCapabilities capabilities;
	private VknSurfaceFormat[] surfaceFormats;
	private VknSurfaceFormat idealFormat;
	
	public long handle()
	{
		return this.handle;
	}
	
	public VknSurfaceCapabilities capabilities()
	{
		return this.capabilities;
	}
	
	public VknSurfaceFormat[] surfaceFormats()
	{
		return this.surfaceFormats;
	}
	
	public VknSurfaceFormat idealFormat()
	{
		return this.idealFormat;
	}
	
	public void __release()
	{
		vkDestroySurfaceKHR(CommonRenderContext.vkInstance.handle(), this.handle, null);
	}
	
	public static VknSurface create(CreateSettings settings)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			long handle = VknInternalUtils.createWindowSurface(CommonRenderContext.vkInstance.handle(), settings.window.handle(), stack);

			VknSurfaceCapabilities capabilities = VknInternalUtils.getPhysicalDeviceSurfaceCapabilities(settings.physicalGPU.handle(), handle, stack);
			
			VknSurfaceFormat[] surfaceFormats = VknInternalUtils.getPhysicalDeviceSurfaceFormats(settings.physicalGPU.handle(), handle, stack);
			
			VknSurfaceFormat idealFormat = surfaceFormats[0];
			for(int i = 0; i < surfaceFormats.length; i++)
			{
				VknSurfaceFormat format = surfaceFormats[i];
				
				if(format.format == VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				{
					idealFormat = format;
					break;
				}
			}
			
			VknSurface result = new VknSurface();
			result.handle = handle;
			result.capabilities = capabilities;
			result.surfaceFormats = surfaceFormats;
			result.idealFormat = idealFormat;
			
			return result;
		}
	}
	
	public static class CreateSettings
	{
		public VknWindowShell window;
		public VknPhysicalGPU physicalGPU;
	}
}
