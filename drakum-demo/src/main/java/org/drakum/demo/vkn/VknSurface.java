package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;

public class VknSurface
{
	private final VknContext context;
	
	private long handle;
	private VknSurfaceCapabilities capabilities;
	private VknSurfaceFormat[] surfaceFormats;
	private VknSurfaceFormat idealFormat;
	
	public VknSurface(Settings settings)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.handle = VknInternalUtils.createWindowSurface(this.context, settings.window.handle(), stack);

			this.capabilities = VknInternalUtils.getPhysicalDeviceSurfaceCapabilities(settings.physicalGPU.handle(), handle, stack);
			
			this.surfaceFormats = VknInternalUtils.getPhysicalDeviceSurfaceFormats(settings.physicalGPU.handle(), handle, stack);
			
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
			
			this.idealFormat = idealFormat;
		}
	}
	
	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public VknSurfaceCapabilities capabilities()
	{
		ensureValid();
		
		return this.capabilities;
	}
	
	public VknSurfaceFormat[] surfaceFormats()
	{
		ensureValid();
		
		return this.surfaceFormats;
	}
	
	public VknSurfaceFormat idealFormat()
	{
		ensureValid();
		
		return this.idealFormat;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroySurfaceKHR(this.context.instance.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}

	public static class Settings
	{
		private final VknContext context;
		
		public VknWindowShell window;
		public VknPhysicalGPU physicalGPU;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
	}
}
