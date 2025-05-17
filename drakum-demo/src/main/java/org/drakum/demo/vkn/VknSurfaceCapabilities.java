package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkSurfaceCapabilities2KHR;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;

public class VknSurfaceCapabilities
{
	public final VknExtent2D currentExtent;
	public final int currentTransform;
	public final int maxImageArrayLayers;
	public final int minImageCount;
	public final int maxImageCount;
	public final VknExtent2D minImageExtent;
	public final VknExtent2D maxImageExtent;
	public final int supportedCompositeAlpha;
	public final int supportedTransforms;
	public final int supportedUsageFlags;
	
	public VknSurfaceCapabilities(VkSurfaceCapabilities2KHR capabilities)
	{
		VkSurfaceCapabilitiesKHR baseCapabilities = capabilities.surfaceCapabilities();
		
		this.currentExtent = new VknExtent2D(baseCapabilities.currentExtent());
		this.currentTransform = baseCapabilities.currentTransform();
		this.maxImageArrayLayers = baseCapabilities.maxImageArrayLayers();
		this.minImageCount = baseCapabilities.minImageCount();
		this.maxImageCount = baseCapabilities.maxImageCount();
		this.minImageExtent = new VknExtent2D(baseCapabilities.minImageExtent());
		this.maxImageExtent = new VknExtent2D(baseCapabilities.maxImageExtent());
		this.supportedCompositeAlpha = baseCapabilities.supportedCompositeAlpha();
		this.supportedTransforms = baseCapabilities.supportedTransforms();
		this.supportedUsageFlags = baseCapabilities.supportedUsageFlags();
	}
}
