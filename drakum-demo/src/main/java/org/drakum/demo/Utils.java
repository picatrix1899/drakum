package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class Utils
{
	
	public static VkInstance createInstance(VkInstanceCreateInfo createInfo, MemoryStack stack)
	{
		PointerBuffer buf = stack.mallocPointer(1);
		
		if(vkCreateInstance(createInfo, null, buf) != VK_SUCCESS)
		{
			throw new Error("Cannot create vulkan instance");
		}
		
		return new VkInstance(buf.get(0), createInfo);
	}
	
	public static VkPhysicalDevice[] enumeratePhysicalDevices(VkInstance vkInstance, MemoryStack stack)
	{
		IntBuffer count = stack.mallocInt(1);
		
		vkEnumeratePhysicalDevices(vkInstance, count, null);
		
		PointerBuffer buf = stack.mallocPointer(count.get(0));
		
		vkEnumeratePhysicalDevices(vkInstance, count, buf);

		VkPhysicalDevice[] out = new VkPhysicalDevice[count.get(0)];
		
		for(int i = 0; i < count.get(0); i++)
		{
			out[i] = new VkPhysicalDevice(buf.get(i), vkInstance);
		}
		
		return out;
	}
	
	public static VkQueueFamilyProperties[] getPhysicalDeviceQueueFamilyProperties(VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		IntBuffer count = stack.mallocInt(1);
		
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, count, null);
		
		VkQueueFamilyProperties.Buffer buf = VkQueueFamilyProperties.calloc(count.get(0), stack);
	
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, count, buf);
		
		VkQueueFamilyProperties[] out = new VkQueueFamilyProperties[count.get(0)];
		
		for(int i = 0; i < count.get(0); i++)
		{
			out[i] = buf.get(i);
		}
		
		return out;
	}
	
	public static VkDevice createDevice(VkDeviceCreateInfo createInfo, VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		PointerBuffer buf = stack.callocPointer(1);
		
		if(vkCreateDevice(physicalDevice, createInfo, null, buf) != VK_SUCCESS)
		{
			throw new Error("cannot create Device");
		}
		
		return new VkDevice(buf.get(0), physicalDevice, createInfo);
	}
	
	public static boolean getPhysicalDeviceSurfaceSupportKHR(VkPhysicalDevice physicalDevice, int queueFamilyIndex, long surface, MemoryStack stack)
	{
		IntBuffer buf = stack.mallocInt(1);
		
		vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, queueFamilyIndex, surface, buf);
		
		return buf.get(0) == VK_TRUE;
	}
	
	public static long createWindowSurface(VkInstance vkInstance, long window, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		if(glfwCreateWindowSurface(vkInstance, window, null, buf) != VK_SUCCESS)
		{
			throw new Error("Cannot create Surface");
		}
		
		return buf.get(0);
	}
	
	public static VkQueue getDeviceQueue(VkDevice device, int queueFamily, int queueIndex, MemoryStack stack)
	{
		PointerBuffer buf = stack.callocPointer(1);
		
		vkGetDeviceQueue(device, queueFamily, queueIndex, buf);
		
		return new VkQueue(buf.get(0), device);
	}
	
	public static long createSwapchain(VkDevice device, VkSwapchainCreateInfoKHR createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateSwapchainKHR(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static VkSurfaceCapabilitiesKHR getPhysicalDeviceSurfaceCapabilities(VkPhysicalDevice physicalDevice, long surface, MemoryStack stack)
	{
		VkSurfaceCapabilitiesKHR surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc(stack);
		
		vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfaceCapabilities);
		
		return surfaceCapabilities;
	}
	
	public static VkExtent2D getFramebufferSize(long window, MemoryStack stack)
	{
		IntBuffer framebufferSizeBuffer = stack.mallocInt(2);
		
		glfwGetFramebufferSize(window, framebufferSizeBuffer.slice(0, 1), framebufferSizeBuffer.slice(1, 1));
		
		VkExtent2D extent = VkExtent2D.malloc(stack);
		extent.width(framebufferSizeBuffer.get(0));
		extent.height(framebufferSizeBuffer.get(1));
		
		return extent;
	}
	
	public static long[] getSwapchainImages(VkDevice device, long swapchain, MemoryStack stack)
	{
		IntBuffer count = stack.callocInt(1);
		
		vkGetSwapchainImagesKHR(device, swapchain, count, null);
		
		LongBuffer buf = stack.callocLong(count.get(0));
		
		vkGetSwapchainImagesKHR(device, swapchain, count, buf);
		
		long[] out = new long[count.get(0)];
		
		for(int i = 0; i < count.get(0); i++)
		{
			out[i] = buf.get(i);
		}
		
		return out;
	}
	
	public static long createImageView(VkDevice device, VkImageViewCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateImageView(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createShaderModule(VkDevice device, VkShaderModuleCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateShaderModule(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createPipelineLayout(VkDevice device, VkPipelineLayoutCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreatePipelineLayout(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createRenderPass(VkDevice device, VkRenderPassCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateRenderPass(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createGraphicsPipeline(VkDevice device, VkGraphicsPipelineCreateInfo.Buffer createInfos, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, createInfos, null, buf);
		
		return buf.get(0);
	}
	
	public static long createFramebuffer(VkDevice device, VkFramebufferCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateFramebuffer(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createCommandPool(VkDevice device, VkCommandPoolCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateCommandPool(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static VkCommandBuffer allocateCommandBuffer(VkDevice device, VkCommandBufferAllocateInfo allocateInfo, MemoryStack stack)
	{
		PointerBuffer buf = stack.callocPointer(1);
		
		vkAllocateCommandBuffers(device, allocateInfo, buf);
		
		return new VkCommandBuffer(buf.get(0), device);
	}
	
	public static long createSemaphore(VkDevice device, VkSemaphoreCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateSemaphore(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createFence(VkDevice device, VkFenceCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateFence(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static int acquireNextImage(VkDevice device, long swapchain, long semaphore, MemoryStack stack)
	{
		IntBuffer buf = stack.callocInt(1);
		
		vkAcquireNextImageKHR(device, swapchain, Long.MAX_VALUE, semaphore, VK_NULL_HANDLE, buf);
		
		return buf.get(0);
	}
}
