package org.drakum.demo.vkn;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;

public class VknPhysicalGPUProperties
{
	public final VknVersion apiVersion;
	public final VknVersion driverVersion;
	public final int vendorId;
	public final int deviceId;
	public final String deviceName;
	public final int deviceType;
	public final byte[] pipelineCacheUUIDs;
	
	public VknPhysicalGPUProperties(VkPhysicalDeviceProperties2 properties)
	{
		VkPhysicalDeviceProperties baseProperties = properties.properties();
		
		ByteBuffer pipelineCacheUUIDBuf = baseProperties.pipelineCacheUUID();
		byte[] pipelineCacheUUIDs = new byte[pipelineCacheUUIDBuf.remaining()];
		
		for(int i = 0; i < pipelineCacheUUIDBuf.remaining(); i++)
		{
			pipelineCacheUUIDs[i] = pipelineCacheUUIDBuf.get(i);
		}
		
		this.apiVersion = VknVersion.fromVulkanApi(baseProperties.apiVersion());
		this.driverVersion = VknVersion.fromVulkan(baseProperties.driverVersion());
		this.vendorId = baseProperties.vendorID();
		this.deviceId = baseProperties.deviceID();
		this.deviceName = baseProperties.deviceNameString();
		this.deviceType = baseProperties.deviceType();
		this.pipelineCacheUUIDs = pipelineCacheUUIDs;
	}
}
