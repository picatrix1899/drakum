package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import java.nio.LongBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VkImage
{
	public long handle;
	public VulkanMemory memory;
	
	public void __release()
	{
		vkDestroyImage(CommonRenderContext.instance().gpu.device, handle, null);
		memory.__release();
	}
	
	public static class Builder
	{
		private int width;
		private int height;
		
		public Builder width(int width)
		{
			this.width = width;
			
			return this;
		}
		
		public Builder height(int height)
		{
			this.height = height;
			
			return this;
		}
		
		public VkImage create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
				imageInfo.sType$Default();
				imageInfo.imageType(VK_IMAGE_TYPE_2D);
				imageInfo.format(VK_FORMAT_B8G8R8A8_SRGB); // oder UNORM für SDR
				imageInfo.extent(VkExtent3D.calloc(stack)
				    .width(width)
				    .height(height)
				    .depth(1));
				imageInfo.mipLevels(1);
				imageInfo.arrayLayers(1);
				imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
				imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
				imageInfo.usage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
				imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
				imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
				
				LongBuffer pImage = stack.mallocLong(1);
				vkCreateImage(CommonRenderContext.instance().gpu.device, imageInfo, null, pImage);
				long image = pImage.get(0);
				
				VkMemoryRequirements memReqs = VkMemoryRequirements.malloc(stack);
				vkGetImageMemoryRequirements(CommonRenderContext.instance().gpu.device, image, memReqs);
				
				VulkanMemory memory = new VulkanMemory.Builder()
					.size(memReqs.size())
					.memoryTypeBits(memReqs.memoryTypeBits())
					.properties(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
					.create();
				
				vkBindImageMemory(CommonRenderContext.instance().gpu.device, image, memory.handle(), 0);
				
				VkImage result = new VkImage();
				result.handle = image;
				result.memory = memory;
				
				return result;
			}
		}
	}
}
