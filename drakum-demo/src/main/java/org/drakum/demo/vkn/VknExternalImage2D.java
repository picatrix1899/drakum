package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.barghos.util.container.ints.Extent2I;

public class VknExternalImage2D implements IVknImage2D
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	private int format;
	private int width;
	private int height;

	public VknExternalImage2D(Settings settings)
	{
		this.context = settings.context;
		
		this.handle = settings.handle;
		this.format = settings.format;
		this.width = settings.width;
		this.height = settings.height;
	}

	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public int format()
	{
		ensureValid();
		
		return this.format;
	}
	
	public int width()
	{
		ensureValid();
		
		return this.width;
	}
	
	public int height()
	{
		ensureValid();
		
		return this.height;
	}
	
	public VknImageView2D createView()
	{
		ensureValid();
		
		return new VknImageView2D(new VknImageView2D.Settings(this.context).image(this));
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private long handle;
		private int width;
		private int height;
		private int format = VK_FORMAT_B8G8R8A8_SRGB;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings handle(long handle)
		{
			this.handle = handle;
			
			return this;
		}
		
		public long handle()
		{
			return this.handle;
		}
		
		public Settings size(int width, int height)
		{
			this.width = width;
			this.height = height;
			
			return this;
		}
		
		public Settings size(Extent2I extent)
		{
			this.width = extent.width();
			this.height = extent.height();
			
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
		
		public Settings format(int format)
		{
			this.format = format;
			
			return this;
		}
		
		public int format()
		{
			return this.format;
		}
	}
}
