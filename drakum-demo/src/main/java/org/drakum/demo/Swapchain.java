package org.drakum.demo;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class Swapchain
{
	public long swapchain;
	public long[] swapchainImageViews;
	public long[] swapchainFramebuffers;
	
	public int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
	public int swapchainColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
	
	public void create(VkSurfaceCapabilitiesKHR surfaceCapabilities, int width, int height, VkDevice device, long surface, QueueFamilyList queueFamilies, long renderPass, int imageCount)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkExtent2D imageExtent = VkExtent2D.calloc(stack);
			imageExtent.width(width);
			imageExtent.height(height);
			
			int sharingMode = 0;
			IntBuffer familyIndices = null;
			if(queueFamilies.graphicsFamily != queueFamilies.presentFamily)
			{
				sharingMode = VK_SHARING_MODE_CONCURRENT;
				familyIndices = stack.ints(queueFamilies.graphicsFamily, queueFamilies.presentFamily);
			}
			else
			{
				sharingMode = VK_SHARING_MODE_EXCLUSIVE;
			}
			
			VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
			swapchainCreateInfo.sType$Default();
			swapchainCreateInfo.surface(surface);
			swapchainCreateInfo.minImageCount(imageCount);
			swapchainCreateInfo.imageFormat(swapchainFormat);
			swapchainCreateInfo.imageColorSpace(swapchainColorSpace);
			swapchainCreateInfo.imageExtent(imageExtent);
			swapchainCreateInfo.imageArrayLayers(1);
			swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			swapchainCreateInfo.imageSharingMode(sharingMode);
			swapchainCreateInfo.pQueueFamilyIndices(familyIndices);
			swapchainCreateInfo.preTransform(surfaceCapabilities.currentTransform());
			swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			swapchainCreateInfo.presentMode(VK_PRESENT_MODE_MAILBOX_KHR);
			swapchainCreateInfo.clipped(true);
			
			swapchain = Utils.createSwapchain(device, swapchainCreateInfo, stack);

			long[] swapchainImages = Utils.getSwapchainImages(device, swapchain, stack);
			swapchainImageViews = new long[swapchainImages.length];

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
				imageViewCreateInfo.format(swapchainFormat);
				imageViewCreateInfo.components(componentMapping);
				imageViewCreateInfo.subresourceRange(subresourceRange);

				swapchainImageViews[i] = Utils.createImageView(device, imageViewCreateInfo, stack);
			}
			
			swapchainFramebuffers = new long[swapchainImageViews.length];

			for(int i = 0; i < swapchainImageViews.length; i++)
			{
				VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
				framebufferCreateInfo.sType$Default();
				framebufferCreateInfo.renderPass(renderPass);
				framebufferCreateInfo.attachmentCount(1);
				framebufferCreateInfo.pAttachments(stack.longs(swapchainImageViews[i]));
				framebufferCreateInfo.width(width);
				framebufferCreateInfo.height(height);
				framebufferCreateInfo.layers(1);

				swapchainFramebuffers[i] = Utils.createFramebuffer(device, framebufferCreateInfo, stack);
			}
		}
	}
	
	public void __release(VkDevice device)
	{
		for(long framebuffer : swapchainFramebuffers)
		{
			vkDestroyFramebuffer(device, framebuffer, null);
		}
		
		for(long imageView : swapchainImageViews)
		{
			vkDestroyImageView(device, imageView, null);
		}
		
		vkDestroySwapchainKHR(device, swapchain, null);
	}
}
