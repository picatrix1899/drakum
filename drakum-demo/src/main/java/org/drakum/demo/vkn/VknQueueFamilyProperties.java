package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties2;

public class VknQueueFamilyProperties
{
	public final int index;
	public final int queueCount;
	public final int queueFlags;
	public final int minImageTransferGranularityWidth;
	public final int minImageTransferGranularityHeight;
	public final int minImageTransferGranularityDepth;
	public final int timestampValidBits;
	
	public VknQueueFamilyProperties(int index, VkQueueFamilyProperties2 properties)
	{
		VkQueueFamilyProperties baseProperties = properties.queueFamilyProperties();
		
		VkExtent3D minImageTransferGranularity = baseProperties.minImageTransferGranularity();
		
		this.index  = index;
		this.queueCount = baseProperties.queueCount();
		this.queueFlags = baseProperties.queueFlags();
		this.minImageTransferGranularityWidth = minImageTransferGranularity.width();
		this.minImageTransferGranularityHeight = minImageTransferGranularity.height();
		this.minImageTransferGranularityDepth = minImageTransferGranularity.depth();
		this.timestampValidBits = baseProperties.timestampValidBits();
	}
}
