package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.barghos.util.container.ints.Extent2I;
import org.drakum.demo.registry.LongId;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VknImage2D implements IVknImage2D
{
	private final VknContext context;
	
	private LongId handle;
	
	private MemoryRequirements memoryRequirements;
	private int format;
	private int width;
	private int height;
	
	public VknImage2D(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.format = settings.format;
			this.width = settings.width;
			this.height = settings.height;
			
			VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
			imageInfo.sType$Default();
			imageInfo.imageType(settings.type);
			imageInfo.format(this.format);
			imageInfo.extent().width(settings.width).height(settings.height).depth(settings.depth);
			imageInfo.mipLevels(settings.mipLevels);
			imageInfo.arrayLayers(settings.arrayLayers);
			imageInfo.samples(settings.samples);
			imageInfo.tiling(settings.tiling);
			imageInfo.usage(settings.usage);
			imageInfo.sharingMode(settings.sharingMode);
			imageInfo.initialLayout(settings.initialLayout);
			
			this.handle = VknInternalUtils.createImage(this.context.gpu.handle(), imageInfo, stack);

			VkMemoryRequirements memoryRequirements = VknInternalUtils.getImageMemoryRequirements(this.context.gpu.handle(), this.handle, stack);
			
			this.memoryRequirements = new MemoryRequirements();
			this.memoryRequirements.size = memoryRequirements.size();
			this.memoryRequirements.alignment = memoryRequirements.alignment();
			this.memoryRequirements.memoryTypeBits = memoryRequirements.memoryTypeBits();
		}
	}

	public LongId handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public int format()
	{
		ensureValid();
		
		return this.format;
	}
	
	public MemoryRequirements memoryRequirements()
	{
		ensureValid();
		
		return this.memoryRequirements;
	}
	
	public VknMemory allocateMemory(int properties)
	{
		ensureValid();
		
		return new VknMemory(new VknMemory.Settings(this.context).size(this.memoryRequirements.size).memoryTypeBits(this.memoryRequirements.memoryTypeBits).properties(properties));
	}
	
	public void bindMemory(VknMemory memory, long offset)
	{
		ensureValid();
		
		vkBindImageMemory(this.context.gpu.handle(), this.handle.handle(), memory.handle().handle(), offset);
	}
	
	public VknMemory allocateAndBindMemory(int properties)
	{
		ensureValid();
		
		VknMemory memory = allocateMemory(properties);
		bindMemory(memory, 0);
		
		return memory;
	}
	
	public VknImageView2D createView()
	{
		ensureValid();
		
		return new VknImageView2D(new VknImageView2D.Settings(this.context).image(this));
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
		return this.handle.isValid();
	}
	
	public void close()
	{
		vkDestroyImage(this.context.gpu.handle(), this.handle.handle(), null);
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public static class MemoryRequirements
	{
		public long size;
		public long alignment;
		public int memoryTypeBits;
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private int type = VK_IMAGE_TYPE_2D;
		private int width;
		private int height;
		private int depth = 1;
		private int format = VK_FORMAT_B8G8R8A8_SRGB;
		private int mipLevels = 1;
		private int arrayLayers = 1;
		private int samples = VK_SAMPLE_COUNT_1_BIT;
		private int tiling = VK_IMAGE_TILING_OPTIMAL;
		private int usage;
		private int sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		private int initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings type(int type)
		{
			this.type = type;
			
			return this;
		}
		
		public int type()
		{
			return this.type;
		}
		
		public Settings size(int width, int height)
		{
			this.width = width;
			this.height = height;
			this.depth = 1;
			
			return this;
		}
		
		public Settings size(Extent2I extent)
		{
			this.width = extent.width();
			this.height = extent.height();
			this.depth = 1;
			
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
		
		public Settings depth(int depth)
		{
			this.depth = depth;
			
			return this;
		}
		
		public int depth()
		{
			return this.depth;
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
		
		public Settings mipLevels(int mipLevels)
		{
			this.mipLevels = mipLevels;
			
			return this;
		}
		
		public int mipLevels()
		{
			return this.mipLevels;
		}
		
		public Settings arrayLayers(int arrayLayers)
		{
			this.arrayLayers = arrayLayers;
			
			return this;
		}
		
		public int arrayLayers()
		{
			return this.arrayLayers;
		}
		
		public Settings samples(int samples)
		{
			this.samples = samples;
			
			return this;
		}
		
		public int samples()
		{
			return this.samples;
		}
		
		public Settings tiling(int tiling)
		{
			this.tiling = tiling;
			
			return this;
		}
		
		public int tiling()
		{
			return this.tiling;
		}
		
		public Settings sharingMode(int sharingMode)
		{
			this.sharingMode = sharingMode;
			
			return this;
		}
		
		public int sharingMode()
		{
			return this.sharingMode;
		}
		
		public Settings initialLayout(int initialLayout)
		{
			this.initialLayout = initialLayout;
			
			return this;
		}
		
		public int initialLayout()
		{
			return this.initialLayout;
		}
		
		public Settings usage(int usage)
		{
			this.usage = usage;
			
			return this;
		}
		
		public int usage()
		{
			return this.usage;
		}
		
		public Settings usageColorAttachment()
		{
			this.usage |= VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
			
			return this;
		}
		
		public Settings usageTransferSrc()
		{
			this.usage |= VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
			
			return this;
		}
		
		public Settings usageTransferDst()
		{
			this.usage |= VK_IMAGE_USAGE_TRANSFER_DST_BIT;
			
			return this;
		}
		
		public Settings usageSampled()
		{
			this.usage |= VK_IMAGE_USAGE_SAMPLED_BIT;
			
			return this;
		}
	}
}
