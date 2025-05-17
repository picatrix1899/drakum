package org.drakum.demo.vkn;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRGetSurfaceCapabilities2.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceSurfaceInfo2KHR;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties2;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilities2KHR;
import org.lwjgl.vulkan.VkSurfaceFormat2KHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class VknInternalUtils
{
	public static VkMemoryRequirements getImageMemoryRequirements(VkDevice device, long image, MemoryStack stack)
	{
		VkMemoryRequirements memReqs = VkMemoryRequirements.malloc(stack);
		vkGetImageMemoryRequirements(CommonRenderContext.gpu.handle(), image, memReqs);
		
		return memReqs;
	}
	
	public static long createImage(VkDevice device, VkImageCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer pImage = stack.mallocLong(1);
		vkCreateImage(CommonRenderContext.gpu.handle(), createInfo, null, pImage);
		return pImage.get(0);
	}
	
	public static int waitForFence(VkDevice device, long fence, boolean waitAll, long timeout)
	{
		return vkWaitForFences(device, fence, waitAll, timeout);
	}
	
	public static int waitForFences(VkDevice device, long[] fences, boolean waitAll, long timeout, MemoryStack stack)
	{
		LongBuffer buf = stack.longs(fences);
		
		return vkWaitForFences(device, buf, waitAll, timeout);
	}
	
	public static VknSurfaceFormat[] getPhysicalDeviceSurfaceFormats(VkPhysicalDevice physicalDevice, long surface, MemoryStack stack)
	{
		VkPhysicalDeviceSurfaceInfo2KHR surfaceInfo = VkPhysicalDeviceSurfaceInfo2KHR.calloc(stack);
		surfaceInfo.sType$Default();
		surfaceInfo.surface(surface);
		
		IntBuffer countBuf = stack.mallocInt(1);
		
		vkGetPhysicalDeviceSurfaceFormats2KHR(physicalDevice, surfaceInfo, countBuf, null);
		
		int count = countBuf.get(0);
		
		VkSurfaceFormat2KHR.Buffer buf = VkSurfaceFormat2KHR.calloc(count, stack);
	
		for (int i = 0; i < count; i++) {
			buf.get(i).sType$Default();
		}
		
		vkGetPhysicalDeviceSurfaceFormats2KHR(physicalDevice, surfaceInfo, countBuf, buf);
		
		VknSurfaceFormat[] out = new VknSurfaceFormat[count];
		
		for(int i = 0; i < count; i++)
		{
			VkSurfaceFormat2KHR format = buf.get(i);
			VknSurfaceFormat formats = new VknSurfaceFormat(format);
			
			out[i] = formats;
		}
			
		return out;
	}
	
	public static VknPhysicalGPUMemoryProperties getPhysicalDeviceMemoryProperties(VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		VkPhysicalDeviceMemoryProperties2 deviceFeatures = VkPhysicalDeviceMemoryProperties2.calloc(stack);
		deviceFeatures.sType$Default();
		
		vkGetPhysicalDeviceMemoryProperties2(physicalDevice, deviceFeatures);
		
		return new VknPhysicalGPUMemoryProperties(deviceFeatures);
	}
	
	public static VknPhysicalGPUFeatures getPhysicalDeviceFeatures(VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		VkPhysicalDeviceFeatures2 deviceFeatures = VkPhysicalDeviceFeatures2.calloc(stack);
		deviceFeatures.sType$Default();
		
		vkGetPhysicalDeviceFeatures2(physicalDevice, deviceFeatures);
		
		return new VknPhysicalGPUFeatures(deviceFeatures);
	}
	
	public static VknPhysicalGPUProperties getPhysicalDeviceProperties(VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		VkPhysicalDeviceProperties2 deviceProperties = VkPhysicalDeviceProperties2.calloc(stack);
		deviceProperties.sType$Default();
		
		vkGetPhysicalDeviceProperties2(physicalDevice, deviceProperties);
		
		return new VknPhysicalGPUProperties(deviceProperties);
	}
	
	public static VkInstance createInstance(VkInstanceCreateInfo createInfo, MemoryStack stack)
	{
		PointerBuffer buf = stack.mallocPointer(1);
		
		if(vkCreateInstance(createInfo, null, buf) != VK_SUCCESS)
		{
			throw new Error("Cannot create vulkan instance");
		}
		
		return new VkInstance(buf.get(0), createInfo);
	}
	
	public static VknPhysicalGPU[] enumeratePhysicalDevices(VkInstance vkInstance, MemoryStack stack)
	{
		IntBuffer countBuf = stack.mallocInt(1);
		
		vkEnumeratePhysicalDevices(vkInstance, countBuf, null);
		
		int count = countBuf.get(0);
		
		PointerBuffer buf = stack.mallocPointer(count);
		
		vkEnumeratePhysicalDevices(vkInstance, countBuf, buf);

		VknPhysicalGPU[] out = new VknPhysicalGPU[count];
		for(int i = 0; i < count; i++)
		{
			out[i] = VknPhysicalGPU.create(new VkPhysicalDevice(buf.get(i), vkInstance));
		}
		
		return out;
	}
	
	public static VknQueueFamilyProperties[] getPhysicalDeviceQueueFamilyProperties(VkPhysicalDevice physicalDevice, MemoryStack stack)
	{
		IntBuffer countBuf = stack.mallocInt(1);
		
		vkGetPhysicalDeviceQueueFamilyProperties2(physicalDevice, countBuf, null);
		
		int count = countBuf.get(0);
		
		VkQueueFamilyProperties2.Buffer buf = VkQueueFamilyProperties2.calloc(count, stack);
	
		for (int i = 0; i < count; i++) {
			buf.get(i).sType$Default();
		}
		
		vkGetPhysicalDeviceQueueFamilyProperties2(physicalDevice, countBuf, buf);
		
		VknQueueFamilyProperties[] out = new VknQueueFamilyProperties[count];
		
		for(int i = 0; i < count; i++)
		{
			VkQueueFamilyProperties2 prop = buf.get(i);
			VknQueueFamilyProperties properties = new VknQueueFamilyProperties(i, prop);
			
			out[i] = properties;
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
	
	public static VknSurfaceCapabilities getPhysicalDeviceSurfaceCapabilities(VkPhysicalDevice physicalDevice, long surface, MemoryStack stack)
	{
		VkPhysicalDeviceSurfaceInfo2KHR surfaceInfo = VkPhysicalDeviceSurfaceInfo2KHR.calloc(stack);
		surfaceInfo.sType$Default();
		surfaceInfo.surface(surface);
		
		VkSurfaceCapabilities2KHR surfaceCapabilities = VkSurfaceCapabilities2KHR.calloc(stack);
		surfaceCapabilities.sType$Default();
		
		vkGetPhysicalDeviceSurfaceCapabilities2KHR(physicalDevice, surfaceInfo, surfaceCapabilities);
		
		return new VknSurfaceCapabilities(surfaceCapabilities);
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
	
	public static IntResult acquireNextImage(VkDevice device, long swapchain, long semaphore, MemoryStack stack)
	{
		IntBuffer buf = stack.callocInt(1);
		
		int code = vkAcquireNextImageKHR(device, swapchain, Long.MAX_VALUE, semaphore, VK_NULL_HANDLE, buf);
		
		IntResult result = new IntResult();
		
		result.result = buf.get(0);
		result.code = code;
		
		return result;
	}
	
	public static class IntResult
	{
		public int result;
		public int code;
	}
	
	public static long createBuffer(VkDevice device, VkBufferCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateBuffer(device, createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long mapMemory(VkDevice device, long memory, long offset, long size, int flags, MemoryStack stack)
	{
		PointerBuffer buf = stack.mallocPointer(1);
		
		vkMapMemory(device, memory, offset, size, flags, buf);
		
		return buf.get(0);
	}
	
	public static long allocateMemory(VkDevice device, VkMemoryAllocateInfo allocateInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.mallocLong(1);
		
		vkAllocateMemory(device, allocateInfo, null, buf);
		
		return buf.get(0);
	}
}
