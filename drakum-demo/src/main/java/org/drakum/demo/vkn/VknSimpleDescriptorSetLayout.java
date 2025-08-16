package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;

public class VknSimpleDescriptorSetLayout
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	public VknSimpleDescriptorSetLayout(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBinding = VkDescriptorSetLayoutBinding.calloc(1, stack);
			descriptorSetLayoutBinding.get(0).binding(0);
			descriptorSetLayoutBinding.get(0).descriptorType(settings.type);
			descriptorSetLayoutBinding.get(0).descriptorCount(1);
			descriptorSetLayoutBinding.get(0).stageFlags(settings.stageFlags);
			
			VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
			descriptorSetLayoutCreateInfo.sType$Default();
			descriptorSetLayoutCreateInfo.pBindings(descriptorSetLayoutBinding);
			
			this.handle = VknInternalUtils.createDescriptorSetLayout(CommonRenderContext.context, descriptorSetLayoutCreateInfo, stack);
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
		
		vkDestroyDescriptorSetLayout(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private int type;
		private int stageFlags;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings type(int type)
		{
			this.type = type;
			
			return this;
		}
		
		public int type()
		{
			return this.type;
		}
		
		public Settings stageFlags(int stageFlags)
		{
			this.stageFlags = stageFlags;
			
			return this;
		}
		
		public int stageFlags()
		{
			return this.stageFlags;
		}
	}
}
