package org.drakum.demo;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public class GPU
{
	public VkPhysicalDevice physicalDevice;
	public VkDevice device;
	public QueueFamilyList queueFamilies;
	public VkQueue graphicsQueue;
	public VkQueue presentQueue;
	private VkPhysicalDeviceMemoryProperties memoryProperties;
	
	public GPU()
	{
		memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
	}
	
	public void initDevice(VkInstance vkInstance, long surface, MemoryStack stack)
	{
		VkPhysicalDevice[] physicalDevices = Utils.enumeratePhysicalDevices(vkInstance, stack);

		for (VkPhysicalDevice physDevice : physicalDevices)
		{
			if (isDeviceSuitable(physDevice, stack))
			{
				physicalDevice = physDevice;

				break;
			}
		}

		VkQueueFamilyProperties[] familyPropertiesArray = Utils.getPhysicalDeviceQueueFamilyProperties(physicalDevice, stack);

		queueFamilies = new QueueFamilyList();
		
		for (int i = 0; i < familyPropertiesArray.length; i++)
		{
			VkQueueFamilyProperties familyProperties = familyPropertiesArray[i];

			if ((familyProperties.queueFlags() & (VK_QUEUE_GRAPHICS_BIT)) > 0)
			{
				queueFamilies.graphicsFamily = i;
			}

			if (Utils.getPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, stack))
			{
				queueFamilies.presentFamily = i;
			}

			if (queueFamilies.graphicsFamily > 1 && queueFamilies.presentFamily > 1)
			{
				break;
			}

			i++;
		}

		if (queueFamilies.graphicsFamily == -1)
		{
			throw new Error("Cannot find graphics family");
		}

		if (queueFamilies.presentFamily == -1)
		{
			throw new Error("Cannot find present family");
		}

		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(2, stack);
		queueCreateInfo.get(0).sType$Default();
		queueCreateInfo.get(0).queueFamilyIndex(queueFamilies.graphicsFamily);
		queueCreateInfo.get(0).pQueuePriorities(stack.floats(1.0f));

		queueCreateInfo.get(1).sType$Default();
		queueCreateInfo.get(1).queueFamilyIndex(queueFamilies.presentFamily);
		queueCreateInfo.get(1).pQueuePriorities(stack.floats(1.0f));

		VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

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

		device = Utils.createDevice(deviceCreateInfo, physicalDevice, stack);

		graphicsQueue = Utils.getDeviceQueue(device, queueFamilies.graphicsFamily, 0, stack);
		presentQueue = Utils.getDeviceQueue(device, queueFamilies.presentFamily, 0, stack);
	}
	
	public boolean isDeviceSuitable(VkPhysicalDevice device, MemoryStack stack)
	{
		VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.calloc(stack);
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

		vkGetPhysicalDeviceProperties(device, deviceProperties);
		vkGetPhysicalDeviceFeatures(device, deviceFeatures);

		return deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU && deviceFeatures.geometryShader();
	}
	
	public int findMemoryType(int typeFilter, int properties, MemoryStack stack)
	{
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
		
		for(int i = 0; i < memoryProperties.memoryTypeCount(); i++)
		{
			if((typeFilter & (1 << i)) != 0 && (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)
			{
				return i;
			}
		}
		
		throw new Error();
	}
	
	public void __release()
	{
		vkDestroyDevice(device, null);
		
		memoryProperties.free();
	}
}
