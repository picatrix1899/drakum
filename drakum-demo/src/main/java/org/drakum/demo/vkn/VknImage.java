package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.ref.Cleaner.Cleanable;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VknImage
{
	private long handle = VK_NULL_HANDLE;
	private VknMemory memory;
	
	private final Cleanable cleanable;
	
	public VknImage(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
			imageInfo.sType$Default();
			imageInfo.imageType(VK_IMAGE_TYPE_2D);
			imageInfo.format(settings.format); // oder UNORM für SDR
			imageInfo.extent().width(settings.width).height(settings.height).depth(1);
			imageInfo.mipLevels(1);
			imageInfo.arrayLayers(1);
			imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
			imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
			imageInfo.usage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
			imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
			
			this.handle = VknInternalUtils.createImage(CommonRenderContext.gpu.handle(), imageInfo, stack);
			
			VkMemoryRequirements memReqs = VknInternalUtils.getImageMemoryRequirements(CommonRenderContext.gpu.handle(), this.handle, stack);
			
			VknMemory.Settings memoryCreateSettings = new VknMemory.Settings();
			memoryCreateSettings.size = memReqs.size();
			memoryCreateSettings.memoryTypeBits = memReqs.memoryTypeBits();
			memoryCreateSettings.properties = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
			
			this.memory = new VknMemory(memoryCreateSettings);
			
			vkBindImageMemory(CommonRenderContext.gpu.handle(), this.handle, this.memory.handle(), 0);
			
			this.cleanable = VknCleanerUtils.CLEANER.register(this, () -> {
				if(this.handle == VK_NULL_HANDLE) return;
					
				vkDestroyImage(CommonRenderContext.gpu.handle(), this.handle, null);
				
				this.memory.close();
				
				this.handle = VK_NULL_HANDLE;
				this.memory = null;
			});
		}
	}

	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		this.cleanable.clean();
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		public int width;
		public int height;
		public int format = VK_FORMAT_B8G8R8A8_SRGB;
	}
}
