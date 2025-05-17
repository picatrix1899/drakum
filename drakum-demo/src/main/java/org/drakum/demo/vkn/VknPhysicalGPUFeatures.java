package org.drakum.demo.vkn;

import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;

public class VknPhysicalGPUFeatures
{
	public final boolean geometryShader;
	public final boolean tesselationShader;
	public final boolean sparseBinding;
	
	public VknPhysicalGPUFeatures(VkPhysicalDeviceFeatures2 properties)
	{
		VkPhysicalDeviceFeatures baseProperties = properties.features();
		
		this.geometryShader = baseProperties.geometryShader();
		this.tesselationShader = baseProperties.tessellationShader();
		this.sparseBinding = baseProperties.sparseBinding();
	}
}
