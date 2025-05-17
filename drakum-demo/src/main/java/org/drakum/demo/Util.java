package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;

public class Util
{
	public static void imageMemoryBarrier(VkCommandBuffer cmdBuffer, int oldLayout, int newLayout, int srcAccessMask, int dstAccessMask, int srcQueueFamilyIndex, int dstQueueFamilyIndex, long image, )
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1, stack);
			imageMemoryBarrier.sType$Default();
			imageMemoryBarrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
			imageMemoryBarrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
			imageMemoryBarrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
			imageMemoryBarrier.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT);
			imageMemoryBarrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			imageMemoryBarrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			imageMemoryBarrier.image(sceneImage.handle());
			imageMemoryBarrier.subresourceRange(VkImageSubresourceRange.calloc(stack) 
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1));
			
			vkCmdPipelineBarrier(this.commandBuffer[this.currentInFlightFrame], VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, null, null, imageMemoryBarrier4);
		}
	}
}
