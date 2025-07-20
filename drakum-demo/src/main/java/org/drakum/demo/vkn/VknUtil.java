package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.util.ArrayList;
import java.util.List;

import org.barghos.util.container.floats.BoxXF;
import org.barghos.util.container.ints.RectXI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkViewport;

public class VknUtil
{
	public static void cmdSetScissor(VkCommandBuffer cmdBuffer, int posX, int posY, int width, int height, MemoryStack stack)
	{
		VkRect2D.Buffer vkScissor = VkRect2D.calloc(1, stack);

		vkScissor.offset().x(posX).y(posY);
		vkScissor.extent().width(width).height(height);
		
		vkCmdSetScissor(cmdBuffer, 0, vkScissor);
	}
	
	public static CmdSetScissors cmdSetScissors(VkCommandBuffer cmdBuffer, MemoryStack stack)
	{
		return new CmdSetScissors(cmdBuffer, stack);
	}
	
	public static class CmdSetScissors
	{
		private final VkCommandBuffer cmdBuffer;
		private final MemoryStack stack;
		private final List<RectXI> scissors = new ArrayList<>();
		
		public CmdSetScissors(VkCommandBuffer cmdBuffer, MemoryStack stack)
		{
			this.cmdBuffer = cmdBuffer;
			this.stack = stack;
		}
		
		public CmdSetScissors add(int posX, int posY, int width, int height)
		{
			this.scissors.add(new RectXI(posX, posY, width, height));
			
			return this;
		}
		
		public void end()
		{	
			int size = this.scissors.size();
			
			if(size == 0) return;
			
			VkRect2D.Buffer vkScissors = VkRect2D.calloc(size, stack);
			
			for(int i = 0; i < size; i++)
			{
				RectXI scissor = this.scissors.get(0);
				VkRect2D vkScissor = vkScissors.get(0);
				
				vkScissor.offset().x(scissor.x()).y(scissor.y());
				vkScissor.extent().width(scissor.width()).height(scissor.height());
			}
			
			vkCmdSetScissor(cmdBuffer, 0, vkScissors);
		}
	}
	
	public static void cmdSetViewport(VkCommandBuffer cmdBuffer, float x, float y, float width, float height, float minDepth, float maxDepth, MemoryStack stack)
	{
		VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
		viewport.x(x);
		viewport.y(y);
		viewport.width(width);
		viewport.height(height);
		viewport.minDepth(minDepth);
		viewport.maxDepth(maxDepth);

		vkCmdSetViewport(cmdBuffer, 0, viewport);
	}
	
	public static CmdSetViewports cmdSetViewports(VkCommandBuffer cmdBuffer, MemoryStack stack)
	{
		return new CmdSetViewports(cmdBuffer, stack);
	}
	
	public static class CmdSetViewports
	{
		private final VkCommandBuffer cmdBuffer;
		private final MemoryStack stack;
		private final List<BoxXF> viewports = new ArrayList<>();
		
		public CmdSetViewports(VkCommandBuffer cmdBuffer, MemoryStack stack)
		{
			this.cmdBuffer = cmdBuffer;
			this.stack = stack;
		}
		
		public CmdSetViewports add(float posX, float posY, float minDepth, float width, float height, float maxDepth)
		{
			this.viewports.add(new BoxXF(posX, posY, minDepth, width, height, maxDepth));
			
			return this;
		}
		
		public void end()
		{	
			int size = this.viewports.size();
			
			if(size == 0) return;

			VkViewport.Buffer vkViewports = VkViewport.calloc(size, stack);
			
			for(int i = 0; i < size; i++)
			{
				BoxXF viewport = this.viewports.get(0);
				VkViewport vkViewport = vkViewports.get(0);
				
				vkViewport.x(viewport.x());
				vkViewport.y(viewport.y());
				vkViewport.minDepth(viewport.z());
				vkViewport.width(viewport.width());
				vkViewport.height(viewport.height());
				vkViewport.maxDepth(viewport.depth());
			}
			
			vkCmdSetViewport(cmdBuffer, 0, vkViewports);
		}
	}
	
	public static void cmdBlitImage(VkCommandBuffer cmdBuffer, long srcImage, int srcX, int srcY, int srcZ, int srcWidth, int srcHeight, int srcDepth, int srcLayout, long dstImage, int dstX, int dstY, int dstZ, int dstWidth, int dstHeight, int dstDepth, int dstLayout, int filter, MemoryStack stack)
	{
		VkImageBlit.Buffer blitRegion = VkImageBlit.calloc(1, stack);

		blitRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
		blitRegion.srcOffsets(0).set(srcX, srcY, srcZ);
		blitRegion.srcOffsets(1).set(srcWidth, srcHeight, srcDepth);

		blitRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
		blitRegion.dstOffsets(0).set(dstX, dstY, dstZ);
		blitRegion.dstOffsets(1).set(dstWidth, dstHeight, dstDepth);
		
		vkCmdBlitImage(cmdBuffer, srcImage, srcLayout, dstImage, dstLayout, blitRegion, filter);
	}
}
