package org.drakum.vk;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRGetSurfaceCapabilities2.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.drakum.demo.registry.HandleRegistry;
import org.drakum.demo.registry.LongId;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
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
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilities2KHR;
import org.lwjgl.vulkan.VkSurfaceFormat2KHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class VknInternalUtils
{
	public static long[] allocateDescriptorSets(VknContext context, VkDescriptorSetAllocateInfo allocateInfo, int count, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(count);
		
		vkAllocateDescriptorSets(context.gpu.handle(), allocateInfo, buf);
		
		long[] result = new long[count];
		
		buf.get(result);
		
		return result;
	}
	
	public static long allocateDescriptorSet(VknContext context, VkDescriptorSetAllocateInfo allocateInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkAllocateDescriptorSets(context.gpu.handle(), allocateInfo, buf);
		
		return buf.get(0);
	}
	
	public static long createDescriptorSetLayout(VknContext context, VkDescriptorSetLayoutCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorSetLayout(context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createDescriptorPool(VknContext context, VkDescriptorPoolCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorPool(CommonRenderContext.context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static LongId createSampler(VknContext context, VkSamplerCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.mallocLong(1);
		
		if (vkCreateSampler(context.gpu.handle(), createInfo, null, buf) != VK_SUCCESS)
		{
			throw new Error("cannot create Sampler");
		}
		
		long handle = buf.get(0);
		
		LongId handleObj = new LongId(handle);
		
		return handleObj;
	}
	
	public static VkMemoryRequirements getImageMemoryRequirements(VknContext context, LongId image, MemoryStack stack)
	{
		VkMemoryRequirements memReqs = VkMemoryRequirements.malloc(stack);
		vkGetImageMemoryRequirements(context.gpu.handle(), image.handle(), memReqs);
		
		return memReqs;
	}
	
	public static VkMemoryRequirements getImageMemoryRequirements(VknContext context, LongId image)
	{
		VkMemoryRequirements memReqs = VkMemoryRequirements.malloc();
		vkGetImageMemoryRequirements(context.gpu.handle(), image.handle(), memReqs);
		
		return memReqs;
	}
	
	public static LongId createImage(VknContext context, VkImageCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer pImage = stack.mallocLong(1);
		vkCreateImage(context.gpu.handle(), createInfo, null, pImage);
		
		long handle = pImage.get(0);
		
		LongId handleObj = new LongId(handle);
		
		return handleObj;
	}
	
	public static int waitForFence(VknContext context, long fence, boolean waitAll, long timeout)
	{
		return vkWaitForFences(context.gpu.handle(), fence, waitAll, timeout);
	}
	
	public static int waitForFences(VknContext context, long[] fences, boolean waitAll, long timeout, MemoryStack stack)
	{
		LongBuffer buf = stack.longs(fences);
		
		return vkWaitForFences(context.gpu.handle(), buf, waitAll, timeout);
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
	
	public static VknPhysicalGPU[] enumeratePhysicalDevices(VknContext context, MemoryStack stack)
	{
		IntBuffer countBuf = stack.mallocInt(1);
		
		vkEnumeratePhysicalDevices(context.instance.handle(), countBuf, null);
		
		int count = countBuf.get(0);
		
		PointerBuffer buf = stack.mallocPointer(count);
		
		vkEnumeratePhysicalDevices(context.instance.handle(), countBuf, buf);

		VknPhysicalGPU[] out = new VknPhysicalGPU[count];
		for(int i = 0; i < count; i++)
		{
			out[i] = new VknPhysicalGPU(new VkPhysicalDevice(buf.get(i), context.instance.handle()));
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
	
	public static long createWindowSurface(VknContext context, long window, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		if(glfwCreateWindowSurface(context.instance.handle(), window, null, buf) != VK_SUCCESS)
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
	
	public static long createSwapchain(VknContext context, VkSwapchainCreateInfoKHR createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateSwapchainKHR(context.gpu.handle(), createInfo, null, buf);
		
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
	
	public static long[] getSwapchainImages(VknContext context, long swapchain, MemoryStack stack)
	{
		IntBuffer count = stack.callocInt(1);
		
		vkGetSwapchainImagesKHR(context.gpu.handle(), swapchain, count, null);
		
		LongBuffer buf = stack.callocLong(count.get(0));
		
		vkGetSwapchainImagesKHR(context.gpu.handle(), swapchain, count, buf);
		
		long[] out = new long[count.get(0)];
		
		for(int i = 0; i < count.get(0); i++)
		{
			out[i] = buf.get(i);
		}
		
		return out;
	}
	
	public static LongId createImageView(VknContext context, VkImageViewCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateImageView(context.gpu.handle(), createInfo, null, buf);
		
		long handle = buf.get(0);
		
		LongId handleObj = new LongId(handle);
		
		return handleObj;
	}
	
	public static LongId createShaderModule(VknContext context, VkShaderModuleCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateShaderModule(context.gpu.handle(), createInfo, null, buf);
		
		long handle = buf.get(0);
		
		LongId handleObj = new LongId(handle);
		
		return handleObj;
	}
	
	public static LongId createPipelineLayout(VknContext context, VkPipelineLayoutCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreatePipelineLayout(context.gpu.handle(), createInfo, null, buf);
		
		long handle = buf.get(0);
		
		return HandleRegistry.PIPELINE_LAYOUT.register(handle);
	}
	
	public static LongId createRenderPass(VknContext context, VkRenderPassCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateRenderPass(context.gpu.handle(), createInfo, null, buf);
		
		return HandleRegistry.RENDERPASS.register(buf.get(0));
	}
	
	public static LongId createGraphicsPipeline(VknContext context, VkGraphicsPipelineCreateInfo.Buffer createInfos, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateGraphicsPipelines(context.gpu.handle(), VK_NULL_HANDLE, createInfos, null, buf);
		
		long handle = buf.get(0);
		
		return HandleRegistry.PIPELINE.register(handle);
	}
	
	public static long createFramebuffer(VknContext context, VkFramebufferCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateFramebuffer(context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createCommandPool(VknContext context, VkCommandPoolCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateCommandPool(context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static VkCommandBuffer allocateCommandBuffer(VknContext context, VkCommandBufferAllocateInfo allocateInfo, MemoryStack stack)
	{
		PointerBuffer buf = stack.callocPointer(1);
		
		vkAllocateCommandBuffers(context.gpu.handle(), allocateInfo, buf);
		
		return new VkCommandBuffer(buf.get(0), context.gpu.handle());
	}
	
	public static long createSemaphore(VknContext context, VkSemaphoreCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateSemaphore(context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static long createFence(VknContext context, VkFenceCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateFence(context.gpu.handle(), createInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static IntResult acquireNextImage(VknContext context, long swapchain, long semaphore, MemoryStack stack)
	{
		IntBuffer buf = stack.callocInt(1);
		
		int code = vkAcquireNextImageKHR(context.gpu.handle(), swapchain, Long.MAX_VALUE, semaphore, VK_NULL_HANDLE, buf);
		
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
	
	public static LongId createBuffer(VknContext context, VkBufferCreateInfo createInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateBuffer(context.gpu.handle(), createInfo, null, buf);
		
		return HandleRegistry.BUFFER.register(buf.get(0));
	}
	
	public static long mapMemory(VknContext context, long memory, long offset, long size, int flags, MemoryStack stack)
	{
		PointerBuffer buf = stack.mallocPointer(1);
		
		vkMapMemory(context.gpu.handle(), memory, offset, size, flags, buf);
		
		return buf.get(0);
	}
	
	public static long allocateMemory(VknContext context, VkMemoryAllocateInfo allocateInfo, MemoryStack stack)
	{
		LongBuffer buf = stack.mallocLong(1);
		
		vkAllocateMemory(context.gpu.handle(), allocateInfo, null, buf);
		
		return buf.get(0);
	}
	
	public static void destroyPipelineLayout(VknContext context, LongId id)
	{
		long handle = HandleRegistry.PIPELINE_LAYOUT.get(id);
		
		vkDestroyPipelineLayout(context.gpu.handle(), handle, null);
		
		HandleRegistry.PIPELINE_LAYOUT.remove(id);
	}
	
	public static void destroyPipeline(VknContext context, LongId id)
	{
		long handle = HandleRegistry.PIPELINE.get(id);
		
		vkDestroyPipeline(context.gpu.handle(), handle, null);
		
		HandleRegistry.PIPELINE.remove(id);
	}
	
	public static void destroyRenderPass(VknContext context, LongId id)
	{
		long handle = HandleRegistry.RENDERPASS.get(id);
		
		vkDestroyRenderPass(context.gpu.handle(), handle, null);
		
		HandleRegistry.RENDERPASS.remove(id);
	}
	
	public static void destroyBuffer(VknContext context, LongId id)
	{
		long handle = HandleRegistry.BUFFER.get(id);
		
		vkDestroyBuffer(context.gpu.handle(), handle, null);
		
		HandleRegistry.BUFFER.remove(id);
	}
}
