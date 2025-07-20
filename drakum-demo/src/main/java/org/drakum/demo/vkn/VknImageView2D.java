package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class VknImageView2D
{
	private final VknContext context;
	
	private long handle;
	private int format;
	private int width;
	private int height;
	
	public VknImageView2D(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
			imageViewCreateInfo.sType$Default();
			imageViewCreateInfo.image(settings.image.handle());
			imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			imageViewCreateInfo.format(settings.image.format());
			imageViewCreateInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY).g(VK_COMPONENT_SWIZZLE_IDENTITY).b(VK_COMPONENT_SWIZZLE_IDENTITY).a(VK_COMPONENT_SWIZZLE_IDENTITY);
			imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);

			this.handle = VknInternalUtils.createImageView(this.context.gpu.handle(), imageViewCreateInfo, stack);
		}
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
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroyImageView(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private IVknImage2D image;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings image(IVknImage2D image)
		{
			this.image = image;
			
			return this;
		}
		
		public IVknImage2D image()
		{
			return this.image;
		}
	}
}
