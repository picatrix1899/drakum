package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

public class VknSampler
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	public VknSampler(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
			samplerInfo.sType$Default();
			samplerInfo.magFilter(settings.magFilter);
	        samplerInfo.minFilter(settings.minFilter);
	        samplerInfo.addressModeU(settings.mode);
	        samplerInfo.addressModeV(settings.mode);
	        samplerInfo.addressModeW(settings.mode);
	        samplerInfo.anisotropyEnable(settings.anisotropy > 0);
	        samplerInfo.maxAnisotropy(settings.anisotropy);
	        samplerInfo.unnormalizedCoordinates(false);
	        samplerInfo.compareEnable(settings.enableCompare);
	        samplerInfo.compareOp(settings.compareOp);
	        samplerInfo.mipmapMode(settings.mipmapMode);
	        samplerInfo.minLod(settings.minLod);
	        samplerInfo.maxLod(settings.maxLod);
	        samplerInfo.mipLodBias(settings.mipLodBias);

			this.handle = VknInternalUtils.createSampler(this.context.gpu.handle(), samplerInfo, stack);
		}
	}
	
	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroySampler(this.context.gpu.handle(), this.handle, null);

		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Image object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		public int magFilter = VK_FILTER_LINEAR;
		public int minFilter = VK_FILTER_LINEAR;
		public int mode = VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
		public float anisotropy = 0.0f;
		public boolean enableCompare = false;
		public int compareOp = VK_COMPARE_OP_ALWAYS;
		public int mipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;
		public float minLod = 0.0f;
		public float maxLod = 0.0f;
		public float mipLodBias = 0.0f;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
	}
}
