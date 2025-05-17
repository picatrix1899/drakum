package org.drakum.demo.vkn;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;

import static org.lwjgl.vulkan.VK14.*;

public class VknCmdImageMemoryBarrier
{
	private VkCommandBuffer cmdBuffer;
	private long image;
	
	private int srcLayout;
	private int dstLayout;
	
	private int srcAccessMask;
	private int dstAccessMask;
	
	private int srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
	private int dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
	
	private int srcStageMask;
	private int dstStageMask;
	
	private int aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
	private int baseMipLevel = 0;
	private int levelCount = 1;
	private int baseArrayLayer = 0;
	private int layerCount = 1;
	
	public VknCmdImageMemoryBarrier(VkCommandBuffer cmdBuffer, long image)
	{
		this.cmdBuffer = cmdBuffer;
		this.image = image;
	}
	
	public VknCmdImageMemoryBarrier layout(int src, int dst)
	{
		this.srcLayout = src;
		this.dstLayout = dst;
		
		return this;
	}
	
	public VknCmdImageMemoryBarrier accessMask(int src, int dst)
	{
		this.srcAccessMask = src;
		this.dstAccessMask = dst;
		
		return this;
	}
	
	public VknCmdImageMemoryBarrier queueFamilyIndex(int src, int dst)
	{
		this.srcQueueFamilyIndex = src;
		this.dstQueueFamilyIndex = dst;
		
		return this;
	}
	
	public VknCmdImageMemoryBarrier stageMask(int src, int dst)
	{
		this.srcStageMask = src;
		this.dstStageMask = dst;
		
		return this;
	}
	
	public VknCmdImageMemoryBarrier subresourceRange(int aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer, int layerCount)
	{
		this.aspectMask = aspectMask;
		this.baseMipLevel = baseMipLevel;
		this.levelCount = levelCount;
		this.baseArrayLayer = baseArrayLayer;
		this.layerCount = layerCount;
		
		return this;
	}
	
	public void run()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1, stack);
			imageMemoryBarrier.sType$Default();
			imageMemoryBarrier.oldLayout(this.srcLayout);
			imageMemoryBarrier.newLayout(this.dstLayout);
			imageMemoryBarrier.srcAccessMask(this.srcAccessMask);
			imageMemoryBarrier.dstAccessMask(this.dstAccessMask);
			imageMemoryBarrier.srcQueueFamilyIndex(this.srcQueueFamilyIndex);
			imageMemoryBarrier.dstQueueFamilyIndex(this.dstQueueFamilyIndex);
			imageMemoryBarrier.image(this.image);
			imageMemoryBarrier.subresourceRange()
				.aspectMask(this.aspectMask)
				.baseMipLevel(this.baseMipLevel)
				.levelCount(this.levelCount)
				.baseArrayLayer(this.baseArrayLayer)
				.layerCount(this.layerCount);
			
			vkCmdPipelineBarrier(this.cmdBuffer, this.srcStageMask, this.dstStageMask, 0, null, null, imageMemoryBarrier);
		}
	}
}
