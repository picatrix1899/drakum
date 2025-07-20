package org.drakum.demo;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;

import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknInternalUtils;
import org.drakum.demo.vkn.VknSimpleDescriptorSetLayout;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class MaterialType
{
	public static long TEXTURE_ALBEDO = 1;
	
	public static MaterialType FLAT_ALBEDO = new MaterialType() {
		
		public void init()
		{
			this.descSetLayout = new VknSimpleDescriptorSetLayout(new VknSimpleDescriptorSetLayout.Settings(CommonRenderContext.context).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT));
		}
		
		public long getId()
		{
			return 1;
		}
		
		private VknSimpleDescriptorSetLayout descSetLayout;
		
		@Override
		public VknSimpleDescriptorSetLayout getLayout()
		{
			return this.descSetLayout;
		}
		
		public void close()
		{
			this.descSetLayout.close();
		}
		
		@Override
		public long getDescSet(long descPool, Material material)
		{
			if(material.descSet > 0) return material.descSet; 
			
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
				descriptorSetAllocateInfo.sType$Default();
				descriptorSetAllocateInfo.descriptorPool(descPool);
				descriptorSetAllocateInfo.pSetLayouts(stack.longs(descSetLayout.handle()));
				
				long textureDescriptorSet = VknInternalUtils.allocateDescriptorSet(CommonRenderContext.context.gpu.handle(), descriptorSetAllocateInfo, stack);
				
				VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
				imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
				imageInfo.imageView(material.textures.get(MaterialType.TEXTURE_ALBEDO).imageView().handle());
				imageInfo.sampler(material.samplers.get(MaterialType.TEXTURE_ALBEDO).handle());
				
				VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(1, stack);
				writeDescriptorSet.get(0).sType$Default();
				writeDescriptorSet.get(0).dstSet(textureDescriptorSet);
				writeDescriptorSet.get(0).dstBinding(0);
				writeDescriptorSet.get(0).dstArrayElement(0);
				writeDescriptorSet.get(0).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
				writeDescriptorSet.get(0).pImageInfo(imageInfo);
				writeDescriptorSet.get(0).descriptorCount(1);

				vkUpdateDescriptorSets(CommonRenderContext.context.gpu.handle(), writeDescriptorSet, null);
				
				material.descSet = textureDescriptorSet;
				
				return textureDescriptorSet;
			}
		}
		
	};
	
	public MaterialType()
	{
		init();
	}
	
	public void init()
	{
		
	}
	
	public long getId()
	{
		return 0;
	}
	
	public VknSimpleDescriptorSetLayout getLayout()
	{
		return null;
	}
	
	public void close()
	{
		
	}
	
	public long getDescSet(long descPool, Material material)
	{
		return 0;
	}
	
	public static void closeAll()
	{
		FLAT_ALBEDO.close();
	}
}
