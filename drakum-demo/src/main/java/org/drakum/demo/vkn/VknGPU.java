package org.drakum.demo.vkn;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;

public class VknGPU
{
	private final VknContext context;
	
	private VknPhysicalGPU physicalGpu;
	private VkDevice handle;
	private VknQueueFamilyList queueFamilies;
	private VkQueue graphicsQueue;
	private VkQueue presentQueue;
	
	public VknGPU(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.physicalGpu = settings.physicalGpu;
			
			VknWindowShell.Settings windowCreateSettings = new VknWindowShell.Settings();
			windowCreateSettings.width = 400;
			windowCreateSettings.height = 400;
			windowCreateSettings.title = "";
			
			VknWindowShell windowShell = new VknWindowShell(windowCreateSettings);
			
			VknSurface.Settings surfaceCreateSettings = new VknSurface.Settings(this.context);
			surfaceCreateSettings.window = windowShell;
			surfaceCreateSettings.physicalGPU = settings.physicalGpu;
			
			VknSurface surface = new VknSurface(surfaceCreateSettings);
			
			this.queueFamilies = new VknQueueFamilyList();
			
			for (int i = 0; i < settings.physicalGpu.queueFamilyPropertyListArray().length; i++)
			{
				VknQueueFamilyProperties familyProperties = settings.physicalGpu.queueFamilyPropertyListArray()[i];

				if ((familyProperties.queueFlags & (VK_QUEUE_GRAPHICS_BIT)) > 0)
				{
					this.queueFamilies.graphicsFamily = i;
				}

				if (settings.physicalGpu.supportsSurface(i, surface.handle(), stack))
				{
					this.queueFamilies.presentFamily = i;
				}

				if (this.queueFamilies.graphicsFamily > 1 && this.queueFamilies.presentFamily > 1)
				{
					break;
				}

				i++;
			}

			if (this.queueFamilies.graphicsFamily == -1)
			{
				throw new Error("Cannot find graphics family");
			}

			if (this.queueFamilies.presentFamily == -1)
			{
				throw new Error("Cannot find present family");
			}

			VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(2, stack);
			queueCreateInfo.get(0).sType$Default();
			queueCreateInfo.get(0).queueFamilyIndex(this.queueFamilies.graphicsFamily);
			queueCreateInfo.get(0).pQueuePriorities(stack.floats(1.0f));

			queueCreateInfo.get(1).sType$Default();
			queueCreateInfo.get(1).queueFamilyIndex(this.queueFamilies.presentFamily);
			queueCreateInfo.get(1).pQueuePriorities(stack.floats(1.0f));

			VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
			physicalDeviceFeatures.samplerAnisotropy(true);
			physicalDeviceFeatures.fillModeNonSolid(true);
			physicalDeviceFeatures.geometryShader(true);
			physicalDeviceFeatures.imageCubeArray(true);
			physicalDeviceFeatures.multiViewport(true);
			physicalDeviceFeatures.sparseBinding(true);
			physicalDeviceFeatures.tessellationShader(true);
			physicalDeviceFeatures.wideLines(true);
			
			PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();
			
			PointerBuffer deviceExtensions = stack.mallocPointer(glfwExtensions.remaining() + 1);
			deviceExtensions.put(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
			deviceExtensions.flip();

			PointerBuffer enabledLayers = stack.mallocPointer(1);
			enabledLayers.put(stack.UTF8("VK_LAYER_KHRONOS_validation"));
			enabledLayers.flip();
			
			VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack);
			deviceCreateInfo.sType$Default();
			deviceCreateInfo.pQueueCreateInfos(queueCreateInfo);
			deviceCreateInfo.pEnabledFeatures(physicalDeviceFeatures);
			deviceCreateInfo.ppEnabledLayerNames(enabledLayers);
			deviceCreateInfo.ppEnabledExtensionNames(deviceExtensions);

			this.handle = VknInternalUtils.createDevice(deviceCreateInfo, settings.physicalGpu.handle(), stack);
			
			this.graphicsQueue = VknInternalUtils.getDeviceQueue(this.handle, this.queueFamilies.graphicsFamily, 0, stack);
			this.presentQueue = VknInternalUtils.getDeviceQueue(this.handle, this.queueFamilies.presentFamily, 0, stack);
			
			surface.close();
			windowShell.close();
		}
	}
	
	public VkDevice handle()
	{
		return this.handle;
	}
	
	public VknPhysicalGPU physicalGpu()
	{
		return this.physicalGpu;
	}
	
	public VkPhysicalDevice physicalGpuHandle()
	{
		return this.physicalGpu.handle();
	}
	
	public VknQueueFamilyList queueFamilies()
	{
		return this.queueFamilies;
	}
	
	public VkQueue graphicsQueue()
	{
		return this.graphicsQueue;
	}
	
	public VkQueue presentQueue()
	{
		return this.presentQueue;
	}
	
	public int findMemoryType(int typeFilter, int properties, MemoryStack stack)
	{
		return this.physicalGpu.findMemoryType(typeFilter, properties, stack);
	}
	
	public void close()
	{
		vkDestroyDevice(this.handle, null);
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private VknPhysicalGPU physicalGpu;
	
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings physicalGpu(VknPhysicalGPU physicalGpu)
		{
			this.physicalGpu = physicalGpu;
			
			return this;
		}
		
		public VknPhysicalGPU physicalGpu()
		{
			return this.physicalGpu;
		}
	}
}
