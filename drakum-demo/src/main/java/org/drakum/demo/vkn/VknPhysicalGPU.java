package org.drakum.demo.vkn;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class VknPhysicalGPU
{
	private VkPhysicalDevice handle;
	private VknPhysicalGPUProperties deviceProperties;
	private VknPhysicalGPUFeatures deviceFeatures;
	private VknPhysicalGPUMemoryProperties memoryProperties;
	private VknQueueFamilyProperties[] queueFamilyPropertyListArray;
	
	public VknPhysicalGPU(VkPhysicalDevice physicalDevice)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.handle = physicalDevice;
			this.deviceProperties = VknInternalUtils.getPhysicalDeviceProperties(this.handle, stack);
			this.deviceFeatures = VknInternalUtils.getPhysicalDeviceFeatures(this.handle, stack);
			this.memoryProperties = VknInternalUtils.getPhysicalDeviceMemoryProperties(this.handle, stack);
			this.queueFamilyPropertyListArray = VknInternalUtils.getPhysicalDeviceQueueFamilyProperties(this.handle, stack);
		}
	}
	
	public VkPhysicalDevice handle()
	{
		return this.handle;
	}

	public VknPhysicalGPUProperties deviceProperties()
	{
		return this.deviceProperties;
	}
	
	public VknPhysicalGPUFeatures deviceFeatures()
	{
		return this.deviceFeatures;
	}

	public VknPhysicalGPUMemoryProperties memoryProperties()
	{
		return this.memoryProperties;
	}

	public VknQueueFamilyProperties[] queueFamilyPropertyListArray()
	{
		return this.queueFamilyPropertyListArray;
	}
	
	public boolean supportsSurface(int queueFamilyIndex, long surface, MemoryStack stack)
	{
		return VknInternalUtils.getPhysicalDeviceSurfaceSupportKHR(this.handle, queueFamilyIndex, surface, stack);
	}

	public int findMemoryType(int typeFilter, int properties, MemoryStack stack)
	{
		for(int i = 0; i < this.memoryProperties.memoryTypes.length; i++)
		{
			if((typeFilter & (1 << i)) != 0 && (this.memoryProperties.memoryTypes[i].propertyFlags & properties) == properties)
			{
				return i;
			}
		}
		
		throw new Error();
	}
}
