package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class VknSwapchain
{
	public long handle;
	public long[] swapchainImages;
	public long[] swapchainImageViews;
	public long[] swapchainFramebuffers;

	public VknRenderPass presentRenderPass;
	
	public VknSemaphore[] imageAvailableSemaphore;
	public VknSemaphore[] renderFinishedSemaphore;
	public VknFence[] inFlightFence;
	public long[] commandPool;
	public VkCommandBuffer[] commandBuffer;
	public VknExtent2D framebufferExtent;
	
	public int inFlightFrameCount;
	public int swapchainImageCount;
	
	public int currentInFlightFrame;
	public int currentImageIndex;
	
	public VknSurface surface;
	
	public long handle()
	{
		return this.handle;
	}
	
	public int currentInFlightFrame()
	{
		return this.currentInFlightFrame;
	}
	
	public VkCommandBuffer currentCmdBuffer()
	{
		return this.commandBuffer[this.currentInFlightFrame];
	}
	
	public int acquireNextImage(VknSemaphore imageAvailableSemaphore)
	{	
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknInternalUtils.IntResult result = VknInternalUtils.acquireNextImage(CommonRenderContext.gpu.handle(), this.handle, imageAvailableSemaphore.handle(), stack);
			
			return result.result;
		}
	}
	
	public void beginFrame(long frameIndex)
	{
		this.currentInFlightFrame = (int)(frameIndex % this.inFlightFrameCount);
		
		this.inFlightFence[this.currentInFlightFrame].waitFor();
		this.inFlightFence[this.currentInFlightFrame].reset();
		
		this.currentImageIndex = acquireNextImage(this.imageAvailableSemaphore[this.currentInFlightFrame]);
		
		vkResetCommandBuffer(this.commandBuffer[this.currentInFlightFrame], 0);
	}
	
	public void endFrame()
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType$Default();
			submitInfo.pWaitSemaphores(stack.longs(this.imageAvailableSemaphore[this.currentInFlightFrame].handle()));
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(stack.pointers(this.commandBuffer[this.currentInFlightFrame]));
			submitInfo.pSignalSemaphores(stack.longs(this.renderFinishedSemaphore[this.currentInFlightFrame].handle()));

			vkQueueSubmit(CommonRenderContext.gpu.graphicsQueue(), submitInfo, this.inFlightFence[this.currentInFlightFrame].handle());

			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType$Default();
			presentInfo.pWaitSemaphores(stack.longs(this.renderFinishedSemaphore[this.currentInFlightFrame].handle()));
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(this.handle));
			presentInfo.pImageIndices(stack.ints(this.currentImageIndex));
			
			vkQueuePresentKHR(CommonRenderContext.gpu.presentQueue(), presentInfo);
		}
	}
	
	public void cmdPresent(VknImage sceneImage, MemoryStack stack)
	{
		new VknCmdImageMemoryBarrier(this.commandBuffer[this.currentInFlightFrame], sceneImage.handle())
		.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		.accessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT)
		.stageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
		.run();

		new VknCmdImageMemoryBarrier(this.commandBuffer[this.currentInFlightFrame], this.swapchainImages[this.currentImageIndex])
		.layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
		.accessMask(0, VK_ACCESS_TRANSFER_WRITE_BIT)
		.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
		.run();
		
		VkImageBlit.Buffer blitRegion = VkImageBlit.calloc(1, stack);

		// Source: full scene image
		blitRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
		blitRegion.srcSubresource().mipLevel(0);
		blitRegion.srcSubresource().baseArrayLayer(0);
		blitRegion.srcSubresource().layerCount(1);
		blitRegion.srcOffsets(0).set(0, 0, 0);
		blitRegion.srcOffsets(1).set(this.framebufferExtent.width, this.framebufferExtent.height, 1);

		// Destination: full swapchain image
		blitRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
		blitRegion.dstSubresource().mipLevel(0);
		blitRegion.dstSubresource().baseArrayLayer(0);
		blitRegion.dstSubresource().layerCount(1);
		blitRegion.dstOffsets(0).set(0, 0, 0);
		blitRegion.dstOffsets(1).set(this.framebufferExtent.width, this.framebufferExtent.height, 1);
		
		vkCmdBlitImage(this.commandBuffer[this.currentInFlightFrame], sceneImage.handle(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, this.swapchainImages[this.currentImageIndex], VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blitRegion, VK_FILTER_LINEAR );
		
		new VknCmdImageMemoryBarrier(this.commandBuffer[this.currentInFlightFrame], this.swapchainImages[this.currentImageIndex])
		.layout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
		.accessMask(VK_ACCESS_TRANSFER_WRITE_BIT, 0)
		.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
		.run();

		new VknCmdImageMemoryBarrier(this.commandBuffer[this.currentInFlightFrame], sceneImage.handle())
		.layout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
		.accessMask(VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_COLOR_ATTACHMENT_READ_BIT)
		.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
		.run();
	}
	
	public void recreate(VknExtent2D framebufferExtent)
	{
		vkDeviceWaitIdle(CommonRenderContext.gpu.handle());
		
		for(long framebuffer : this.swapchainFramebuffers)
		{
			vkDestroyFramebuffer(CommonRenderContext.gpu.handle(), framebuffer, null);
		}
		
		for(long imageView : this.swapchainImageViews)
		{
			vkDestroyImageView(CommonRenderContext.gpu.handle(), imageView, null);
		}

		try(MemoryStack stack = MemoryStack.stackPush())
		{
			
			VkExtent2D imageExtent = VkExtent2D.calloc(stack);
			imageExtent.width(framebufferExtent.width);
			imageExtent.height(framebufferExtent.height);
			
			int sharingMode = 0;
			IntBuffer familyIndices = null;
			if(CommonRenderContext.gpu.queueFamilies().graphicsFamily != CommonRenderContext.gpu.queueFamilies().presentFamily)
			{
				sharingMode = VK_SHARING_MODE_CONCURRENT;
				familyIndices = stack.ints(CommonRenderContext.gpu.queueFamilies().graphicsFamily, CommonRenderContext.gpu.queueFamilies().presentFamily);
			}
			else
			{
				sharingMode = VK_SHARING_MODE_EXCLUSIVE;
			}
			
			VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			swapchainCreateInfo.sType$Default();
			swapchainCreateInfo.surface(this.surface.handle());
			swapchainCreateInfo.minImageCount(this.swapchainImageCount);
			swapchainCreateInfo.imageFormat(this.surface.idealFormat().format);
			swapchainCreateInfo.imageColorSpace(this.surface.idealFormat().colorSpace);
			swapchainCreateInfo.imageExtent(imageExtent);
			swapchainCreateInfo.imageArrayLayers(1);
			swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT);
			swapchainCreateInfo.imageSharingMode(sharingMode);
			swapchainCreateInfo.pQueueFamilyIndices(familyIndices);
			swapchainCreateInfo.preTransform(this.surface.capabilities().currentTransform);
			swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			swapchainCreateInfo.presentMode(VK_PRESENT_MODE_MAILBOX_KHR);
			swapchainCreateInfo.clipped(true);
			swapchainCreateInfo.oldSwapchain(this.handle);
			
			long handle = VknInternalUtils.createSwapchain(CommonRenderContext.gpu.handle(), swapchainCreateInfo, stack);

			long[] swapchainImages = VknInternalUtils.getSwapchainImages(CommonRenderContext.gpu.handle(), handle, stack);
			long[] swapchainImageViews = new long[swapchainImages.length];

			for(int i = 0; i < swapchainImages.length; i++)
			{
				VkComponentMapping componentMapping = VkComponentMapping.calloc(stack);
				componentMapping.r(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.g(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.b(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.a(VK_COMPONENT_SWIZZLE_IDENTITY);

				VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
				subresourceRange.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				subresourceRange.baseMipLevel(0);
				subresourceRange.levelCount(1);
				subresourceRange.baseArrayLayer(0);
				subresourceRange.layerCount(1);

				VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
				imageViewCreateInfo.sType$Default();
				imageViewCreateInfo.image(swapchainImages[i]);
				imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
				imageViewCreateInfo.format(this.surface.idealFormat().format);
				imageViewCreateInfo.components(componentMapping);
				imageViewCreateInfo.subresourceRange(subresourceRange);

				swapchainImageViews[i] = VknInternalUtils.createImageView(CommonRenderContext.gpu.handle(), imageViewCreateInfo, stack);
			}
			
			long[] swapchainFramebuffers = new long[swapchainImageViews.length];

			for(int i = 0; i < swapchainImageViews.length; i++)
			{
				VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
				framebufferCreateInfo.sType$Default();
				framebufferCreateInfo.renderPass(presentRenderPass.handle());
				framebufferCreateInfo.attachmentCount(1);
				framebufferCreateInfo.pAttachments(stack.longs(swapchainImageViews[i]));
				framebufferCreateInfo.width(framebufferExtent.width);
				framebufferCreateInfo.height(framebufferExtent.height);
				framebufferCreateInfo.layers(1);
				
				swapchainFramebuffers[i] = VknInternalUtils.createFramebuffer(CommonRenderContext.gpu.handle(), framebufferCreateInfo, stack);
			}
		
			vkDestroySwapchainKHR(CommonRenderContext.gpu.handle(), this.handle, null);
			
			this.handle = handle;
			this.swapchainImageViews = swapchainImageViews;
			this.swapchainFramebuffers = swapchainFramebuffers;
			this.swapchainImages = swapchainImages;
			this.framebufferExtent = framebufferExtent;
		}
	}
	
	public void __release()
	{
		for(VknSemaphore semaphore : this.imageAvailableSemaphore)
		{
			semaphore.__release();
		}
		
		for(VknSemaphore semaphore : this.renderFinishedSemaphore)
		{
			semaphore.__release();
		}
		
		for(VknFence fence : this.inFlightFence)
		{
			fence.close();
		}
		
		for(long commandPool : this.commandPool)
		{
			vkDestroyCommandPool(CommonRenderContext.gpu.handle(), commandPool, null);
		}
		
		for(long framebuffer : this.swapchainFramebuffers)
		{
			vkDestroyFramebuffer(CommonRenderContext.gpu.handle(), framebuffer, null);
		}
		
		for(long imageView : this.swapchainImageViews)
		{
			vkDestroyImageView(CommonRenderContext.gpu.handle(), imageView, null);
		}
		
		this.presentRenderPass.__release();
		
		vkDestroySwapchainKHR(CommonRenderContext.gpu.handle(), this.handle, null);
	}
	
	public static VknSwapchain create(CreateSettings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknRenderPass.Attachment renderpassAttachment = new VknRenderPass.Attachment();
			renderpassAttachment.format = settings.surface.idealFormat().format;
			renderpassAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
			renderpassAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

			VknRenderPass.SubpassAttachmentRef renderpassSubpassRef = new VknRenderPass.SubpassAttachmentRef();
			renderpassSubpassRef.attachementIndex = 0;
			renderpassSubpassRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
			
			VknRenderPass.Subpass renderpassSubpass = new VknRenderPass.Subpass();
			renderpassSubpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
			renderpassSubpass.colorAttachmentReferences.add(renderpassSubpassRef);

			VknRenderPass.CreateSettings renderpassCreateSettings = new VknRenderPass.CreateSettings();
			renderpassCreateSettings.attachments.add(renderpassAttachment);
			renderpassCreateSettings.subpasses.add(renderpassSubpass);

			VknRenderPass presentRenderPass = VknRenderPass.create(renderpassCreateSettings);
			
			VkExtent2D imageExtent = VkExtent2D.calloc(stack);
			imageExtent.width(settings.framebufferExtent.width);
			imageExtent.height(settings.framebufferExtent.height);
			
			int sharingMode = 0;
			IntBuffer familyIndices = null;
			if(CommonRenderContext.gpu.queueFamilies().graphicsFamily != CommonRenderContext.gpu.queueFamilies().presentFamily)
			{
				sharingMode = VK_SHARING_MODE_CONCURRENT;
				familyIndices = stack.ints(CommonRenderContext.gpu.queueFamilies().graphicsFamily, CommonRenderContext.gpu.queueFamilies().presentFamily);
			}
			else
			{
				sharingMode = VK_SHARING_MODE_EXCLUSIVE;
			}
			
			VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			swapchainCreateInfo.sType$Default();
			swapchainCreateInfo.surface(settings.surface.handle());
			swapchainCreateInfo.minImageCount(settings.swapchainImageCount);
			swapchainCreateInfo.imageFormat(settings.surface.idealFormat().format);
			swapchainCreateInfo.imageColorSpace(settings.surface.idealFormat().colorSpace);
			swapchainCreateInfo.imageExtent(imageExtent);
			swapchainCreateInfo.imageArrayLayers(1);
			swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT);
			swapchainCreateInfo.imageSharingMode(sharingMode);
			swapchainCreateInfo.pQueueFamilyIndices(familyIndices);
			swapchainCreateInfo.preTransform(settings.surface.capabilities().currentTransform);
			swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			swapchainCreateInfo.presentMode(VK_PRESENT_MODE_MAILBOX_KHR);
			swapchainCreateInfo.clipped(true);
			
			long handle = VknInternalUtils.createSwapchain(CommonRenderContext.gpu.handle(), swapchainCreateInfo, stack);

			long[] swapchainImages = VknInternalUtils.getSwapchainImages(CommonRenderContext.gpu.handle(), handle, stack);
			long[] swapchainImageViews = new long[swapchainImages.length];

			for(int i = 0; i < swapchainImages.length; i++)
			{
				VkComponentMapping componentMapping = VkComponentMapping.calloc(stack);
				componentMapping.r(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.g(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.b(VK_COMPONENT_SWIZZLE_IDENTITY);
				componentMapping.a(VK_COMPONENT_SWIZZLE_IDENTITY);

				VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
				subresourceRange.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
				subresourceRange.baseMipLevel(0);
				subresourceRange.levelCount(1);
				subresourceRange.baseArrayLayer(0);
				subresourceRange.layerCount(1);

				VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
				imageViewCreateInfo.sType$Default();
				imageViewCreateInfo.image(swapchainImages[i]);
				imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
				imageViewCreateInfo.format(settings.surface.idealFormat().format);
				imageViewCreateInfo.components(componentMapping);
				imageViewCreateInfo.subresourceRange(subresourceRange);

				swapchainImageViews[i] = VknInternalUtils.createImageView(CommonRenderContext.gpu.handle(), imageViewCreateInfo, stack);
			}
			
			long[] swapchainFramebuffers = new long[swapchainImageViews.length];

			for(int i = 0; i < swapchainImageViews.length; i++)
			{
				VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
				framebufferCreateInfo.sType$Default();
				framebufferCreateInfo.renderPass(presentRenderPass.handle());
				framebufferCreateInfo.attachmentCount(1);
				framebufferCreateInfo.pAttachments(stack.longs(swapchainImageViews[i]));
				framebufferCreateInfo.width(settings.framebufferExtent.width);
				framebufferCreateInfo.height(settings.framebufferExtent.height);
				framebufferCreateInfo.layers(1);

				swapchainFramebuffers[i] = VknInternalUtils.createFramebuffer(CommonRenderContext.gpu.handle(), framebufferCreateInfo, stack);
			}
			
			VknSemaphore[] imageAvailableSemaphores = new VknSemaphore[settings.inFlightFrameCount];
			VknSemaphore[] renderFinishedSemaphores = new VknSemaphore[settings.inFlightFrameCount];
			VknFence[] inFlightFences = new VknFence[settings.inFlightFrameCount];
			
			long[] commandPools = new long[settings.inFlightFrameCount];
			VkCommandBuffer[] commandBuffers = new VkCommandBuffer[settings.inFlightFrameCount];
			
			for(int i = 0; i < settings.inFlightFrameCount; i++)
			{
				imageAvailableSemaphores[i] = VknSemaphore.create();
				
				renderFinishedSemaphores[i] = VknSemaphore.create();

				VknFence.Settings fenceCreateSettings = new VknFence.Settings();
				fenceCreateSettings.isSignaled = true;
				
				inFlightFences[i] = new VknFence(fenceCreateSettings);
				
				VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
				commandPoolCreateInfo.sType$Default();
				commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
				commandPoolCreateInfo.queueFamilyIndex(CommonRenderContext.gpu.queueFamilies().graphicsFamily);

				commandPools[i] = VknInternalUtils.createCommandPool(CommonRenderContext.gpu.handle(), commandPoolCreateInfo, stack);

				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
				commandBufferAllocateInfo.sType$Default();
				commandBufferAllocateInfo.commandPool(commandPools[i]);
				commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
				commandBufferAllocateInfo.commandBufferCount(1);

				commandBuffers[i] = VknInternalUtils.allocateCommandBuffer(CommonRenderContext.gpu.handle(), commandBufferAllocateInfo, stack);
			}
			
			VknSwapchain result = new VknSwapchain();
			result.handle = handle;
			result.swapchainImageViews = swapchainImageViews;
			result.swapchainFramebuffers = swapchainFramebuffers;
			result.presentRenderPass = presentRenderPass;
			result.imageAvailableSemaphore = imageAvailableSemaphores;
			result.renderFinishedSemaphore = renderFinishedSemaphores;
			result.inFlightFence = inFlightFences;
			result.commandPool = commandPools;
			result.commandBuffer = commandBuffers;
			result.inFlightFrameCount = settings.inFlightFrameCount;
			result.swapchainImages = swapchainImages;
			result.framebufferExtent = settings.framebufferExtent;
			result.surface = settings.surface;
			result.swapchainImageCount = settings.swapchainImageCount;
			
			return result;
		}
	}

	public static class CreateSettings
	{
		public int swapchainImageCount;
		public VknSurface surface;
		public int inFlightFrameCount;
		public VknExtent2D framebufferExtent;
	}
}
