package org.drakum.demo.vkn;

import org.barghos.util.container.ints.Extent2I;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSurfaceCapabilities2KHR;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;

public class VknSurfaceCapabilities
{
	public final Extent2I currentExtent;
	public final int currentTransform;
	public final int maxImageArrayLayers;
	public final int minImageCount;
	public final int maxImageCount;
	public final Extent2I minImageExtent;
	public final Extent2I maxImageExtent;
	public final int supportedCompositeAlpha;
	public final int supportedTransforms;
	public final int supportedUsageFlags;
	
	public VknSurfaceCapabilities(VkSurfaceCapabilities2KHR capabilities)
	{
		VkSurfaceCapabilitiesKHR baseCapabilities = capabilities.surfaceCapabilities();
		
		VkExtent2D currentExtent = baseCapabilities.currentExtent();
		VkExtent2D minExtent = baseCapabilities.minImageExtent();
		VkExtent2D maxExtent = baseCapabilities.maxImageExtent();
		
		this.currentExtent = new Extent2I(currentExtent.width(), currentExtent.height());
		this.currentTransform = baseCapabilities.currentTransform();
		this.maxImageArrayLayers = baseCapabilities.maxImageArrayLayers();
		this.minImageCount = baseCapabilities.minImageCount();
		this.maxImageCount = baseCapabilities.maxImageCount();
		this.minImageExtent = new Extent2I(minExtent.width(), minExtent.height());
		this.maxImageExtent =  new Extent2I(maxExtent.width(), maxExtent.height());
		this.supportedCompositeAlpha = baseCapabilities.supportedCompositeAlpha();
		this.supportedTransforms = baseCapabilities.supportedTransforms();
		this.supportedUsageFlags = baseCapabilities.supportedUsageFlags();
	}
}
