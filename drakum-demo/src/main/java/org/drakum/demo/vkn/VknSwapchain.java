package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;

import org.barghos.util.container.ints.Extent2I;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class VknSwapchain
{
	private final VknContext context;
	
	private long handle;
	
	private VknExternalImage2D[] swapchainImages;
	private VknImageView2D[] swapchainImageViews;
	private VknFramebuffer2D[] swapchainFramebuffers;

	private VknRenderPass presentRenderPass;
	
	private VknSemaphore[] imageAvailableSemaphore;
	private VknSemaphore[] renderFinishedSemaphore;
	private VknFence[] inFlightFence;
	private long[] commandPools;
	private VkCommandBuffer[] commandBuffers;
	
	private Extent2I framebufferExtent;
	
	private int inFlightFrameCount;
	private int swapchainImageCount;
	
	private int currentInFlightFrame;
	private int currentImageIndex;
	
	private VknSurface surface;

	public VknSwapchain(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.inFlightFrameCount = settings.inFlightFrameCount;
			this.framebufferExtent = settings.framebufferExtent;
			this.surface = settings.surface;
			this.swapchainImageCount = settings.swapchainImageCount;
			
			createPresentRenderPass();
			createInflightControls(stack);
			
			recreate(this.framebufferExtent);
		}
	}
	
	private void createPresentRenderPass()
	{
		VknRenderPass.Attachment renderpassAttachment = new VknRenderPass.Attachment();
		renderpassAttachment.format = this.surface.idealFormat().format;
		renderpassAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
		renderpassAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
		
		VknRenderPass.Subpass renderpassSubpass = new VknRenderPass.Subpass();
		renderpassSubpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
		renderpassSubpass.addColorAttachmentReference(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		
		VknRenderPass.Settings renderpassCreateSettings = new VknRenderPass.Settings(this.context);
		renderpassCreateSettings.attachments.add(renderpassAttachment);
		renderpassCreateSettings.subpasses.add(renderpassSubpass);

		this.presentRenderPass = new VknRenderPass(renderpassCreateSettings);
	}
	
	private void createInflightControls(MemoryStack stack)
	{
		VknSemaphore[] imageAvailableSemaphores = new VknSemaphore[this.inFlightFrameCount];
		VknSemaphore[] renderFinishedSemaphores = new VknSemaphore[this.inFlightFrameCount];
		VknFence[] inFlightFences = new VknFence[this.inFlightFrameCount];
		
		long[] commandPools = new long[this.inFlightFrameCount];
		VkCommandBuffer[] commandBuffers = new VkCommandBuffer[this.inFlightFrameCount];
		
		for(int i = 0; i < this.inFlightFrameCount; i++)
		{
			imageAvailableSemaphores[i] = new VknSemaphore(new VknSemaphore.Settings(this.context));
			
			renderFinishedSemaphores[i] = new VknSemaphore(new VknSemaphore.Settings(this.context));

			inFlightFences[i] = new VknFence(new VknFence.Settings(this.context).signaled());
			
			VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
			commandPoolCreateInfo.sType$Default();
			commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			commandPoolCreateInfo.queueFamilyIndex(this.context.gpu.queueFamilies().graphicsFamily);

			commandPools[i] = VknInternalUtils.createCommandPool(this.context.gpu.handle(), commandPoolCreateInfo, stack);

			VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
			commandBufferAllocateInfo.sType$Default();
			commandBufferAllocateInfo.commandPool(commandPools[i]);
			commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			commandBufferAllocateInfo.commandBufferCount(1);

			commandBuffers[i] = VknInternalUtils.allocateCommandBuffer(this.context.gpu.handle(), commandBufferAllocateInfo, stack);
		}
		
		this.imageAvailableSemaphore = imageAvailableSemaphores;
		this.renderFinishedSemaphore = renderFinishedSemaphores;
		this.inFlightFence = inFlightFences;
		
		this.commandPools = commandPools;
		this.commandBuffers = commandBuffers;
	}
	
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
		return this.commandBuffers[this.currentInFlightFrame];
	}
	
	public int acquireNextImage(VknSemaphore imageAvailableSemaphore)
	{	
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknInternalUtils.IntResult result = VknInternalUtils.acquireNextImage(this.context.gpu.handle(), this.handle, imageAvailableSemaphore.handle(), stack);
			
			return result.result;
		}
	}
	
	public void beginFrame(long frameIndex)
	{
		this.currentInFlightFrame = (int)(frameIndex % this.inFlightFrameCount);
		
		this.inFlightFence[this.currentInFlightFrame].waitFor();
		this.inFlightFence[this.currentInFlightFrame].reset();
		
		this.currentImageIndex = acquireNextImage(this.imageAvailableSemaphore[this.currentInFlightFrame]);
		
		vkResetCommandBuffer(this.commandBuffers[this.currentInFlightFrame], 0);
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
			submitInfo.pCommandBuffers(stack.pointers(this.commandBuffers[this.currentInFlightFrame]));
			submitInfo.pSignalSemaphores(stack.longs(this.renderFinishedSemaphore[this.currentInFlightFrame].handle()));

			vkQueueSubmit(this.context.gpu.graphicsQueue(), submitInfo, this.inFlightFence[this.currentInFlightFrame].handle());

			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType$Default();
			presentInfo.pWaitSemaphores(stack.longs(this.renderFinishedSemaphore[this.currentInFlightFrame].handle()));
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(this.handle));
			presentInfo.pImageIndices(stack.ints(this.currentImageIndex));
			
			vkQueuePresentKHR(this.context.gpu.presentQueue(), presentInfo);
		}
	}
	
	public void cmdPresent(VknImage2D sceneImage, MemoryStack stack)
	{
		new VknCmdImageMemoryBarrier(this.commandBuffers[this.currentInFlightFrame], sceneImage.handle())
		.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL)
		.accessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT, VK_ACCESS_TRANSFER_READ_BIT)
		.stageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
		.run();

		new VknCmdImageMemoryBarrier(this.commandBuffers[this.currentInFlightFrame], this.swapchainImages[this.currentImageIndex])
		.layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
		.accessMask(0, VK_ACCESS_TRANSFER_WRITE_BIT)
		.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
		.run();

		VknUtil.cmdBlitImage(
				this.commandBuffers[this.currentInFlightFrame], sceneImage.handle(), 0, 0, 0, this.framebufferExtent.width(), this.framebufferExtent.height(), 1, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
				this.swapchainImages[this.currentImageIndex].handle(), 0, 0, 0, this.framebufferExtent.width(), this.framebufferExtent.height(), 1, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_FILTER_LINEAR, stack);
		
		new VknCmdImageMemoryBarrier(this.commandBuffers[this.currentInFlightFrame], this.swapchainImages[this.currentImageIndex])
		.layout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
		.accessMask(VK_ACCESS_TRANSFER_WRITE_BIT, 0)
		.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
		.run();

		new VknCmdImageMemoryBarrier(this.commandBuffers[this.currentInFlightFrame], sceneImage.handle())
		.layout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
		.accessMask(VK_ACCESS_TRANSFER_READ_BIT, VK_ACCESS_COLOR_ATTACHMENT_READ_BIT)
		.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
		.run();
	}
	
	public void recreate(Extent2I framebufferExtent)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.framebufferExtent = framebufferExtent;
			
			long oldHandle = this.handle;
			
			if(oldHandle != VK_NULL_HANDLE) vkDeviceWaitIdle(this.context.gpu.handle());
			
			if(this.swapchainFramebuffers != null)
			{
				for(VknFramebuffer2D framebuffer : this.swapchainFramebuffers)
				{
					framebuffer.close();
				}
			}
			
			if(this.swapchainImageViews != null)
			{
				for(VknImageView2D imageView : this.swapchainImageViews)
				{
					imageView.close();
				}
			}

			int sharingMode = 0;
			IntBuffer familyIndices = null;
			if(this.context.gpu.queueFamilies().graphicsFamily != this.context.gpu.queueFamilies().presentFamily)
			{
				sharingMode = VK_SHARING_MODE_CONCURRENT;
				familyIndices = stack.ints(this.context.gpu.queueFamilies().graphicsFamily, this.context.gpu.queueFamilies().presentFamily);
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
			swapchainCreateInfo.imageExtent().width(framebufferExtent.width()).height(framebufferExtent.height());
			swapchainCreateInfo.imageArrayLayers(1);
			swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT);
			swapchainCreateInfo.imageSharingMode(sharingMode);
			swapchainCreateInfo.pQueueFamilyIndices(familyIndices);
			swapchainCreateInfo.preTransform(this.surface.capabilities().currentTransform);
			swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			//swapchainCreateInfo.presentMode(VK_PRESENT_MODE_MAILBOX_KHR);
			swapchainCreateInfo.presentMode(VK_PRESENT_MODE_IMMEDIATE_KHR);
			swapchainCreateInfo.clipped(true);
			if(oldHandle != VK_NULL_HANDLE) swapchainCreateInfo.oldSwapchain(oldHandle);
			
			this.handle = VknInternalUtils.createSwapchain(this.context.gpu.handle(), swapchainCreateInfo, stack);

			long[] rawSwapchainImages = VknInternalUtils.getSwapchainImages(this.context.gpu.handle(), handle, stack);
			
			VknExternalImage2D[] swapchainImages = new VknExternalImage2D[rawSwapchainImages.length];
			VknImageView2D[] swapchainImageViews = new VknImageView2D[rawSwapchainImages.length];
			VknFramebuffer2D[] swapchainFramebuffers = new VknFramebuffer2D[rawSwapchainImages.length];
			
			for(int i = 0; i < rawSwapchainImages.length; i++)
			{
				VknExternalImage2D image = new VknExternalImage2D(new VknExternalImage2D.Settings(this.context).handle(rawSwapchainImages[i]).format(this.surface.idealFormat().format).size(this.framebufferExtent));
				swapchainImages[i] = image;
				
				VknImageView2D view = image.createView();
				swapchainImageViews[i] = view;
				
				VknFramebuffer2D.Settings framebufferSettings = new VknFramebuffer2D.Settings(this.context);
				framebufferSettings.size(this.framebufferExtent);
				framebufferSettings.renderPass(presentRenderPass);
				framebufferSettings.addAttachment(view);
				
				swapchainFramebuffers[i] = new VknFramebuffer2D(framebufferSettings);
			}
			
			this.swapchainImages = swapchainImages;
			this.swapchainImageViews = swapchainImageViews;
			this.swapchainFramebuffers = swapchainFramebuffers;
		
			if(oldHandle != VK_NULL_HANDLE) vkDestroySwapchainKHR(this.context.gpu.handle(), oldHandle, null);
		}
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		for(VknSemaphore semaphore : this.imageAvailableSemaphore)
		{
			semaphore.close();
		}
		
		this.imageAvailableSemaphore = null;
		
		for(VknSemaphore semaphore : this.renderFinishedSemaphore)
		{
			semaphore.close();
		}
		
		this.renderFinishedSemaphore = null;
		
		for(VknFence fence : this.inFlightFence)
		{
			fence.close();
		}
		
		this.inFlightFence = null;
		
		for(long commandPool : this.commandPools)
		{
			vkDestroyCommandPool(this.context.gpu.handle(), commandPool, null);
		}
		
		this.commandPools = null;
		
		for(VknFramebuffer2D framebuffer : this.swapchainFramebuffers)
		{
			framebuffer.close();
		}
		
		this.swapchainFramebuffers = null;
		
		for(VknImageView2D imageView : this.swapchainImageViews)
		{
			imageView.close();
		}
		
		this.presentRenderPass.close();
		
		vkDestroySwapchainKHR(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}

	public static class Settings
	{
		private final VknContext context;
		
		public int swapchainImageCount;
		public VknSurface surface;
		public int inFlightFrameCount;
		public Extent2I framebufferExtent;
		
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
