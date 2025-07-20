package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

public class VknPipeline
{
	private final VknContext context;
	
	private final long pipelineLayout;
	
	private long handle;
	
	public VknPipeline(Settings settings)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.pipelineLayout = settings.pipelineLayout;
			
			VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(settings.shaders.size(), stack);
			for(int i = 0; i < settings.shaders.size(); i++)
			{
				ShaderSettings ss = settings.shaders.get(i);
				
				shaderStages.get(i).sType$Default();
				shaderStages.get(i).stage(ss.stage);
				shaderStages.get(i).module(ss.shaderModule.handle);
				shaderStages.get(i).pName(stack.UTF8(ss.entryPoint));
			}

			VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack);
			dynamicStateCreateInfo.sType$Default();
			dynamicStateCreateInfo.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));
			
			VkPipelineVertexInputStateCreateInfo pipelineVertexInputCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
			pipelineVertexInputCreateInfo.sType$Default();
			pipelineVertexInputCreateInfo.pVertexBindingDescriptions(settings.bindingDescriptions);
			pipelineVertexInputCreateInfo.pVertexAttributeDescriptions(settings.attributeDescriptions);
			
			VkPipelineInputAssemblyStateCreateInfo pipelineInputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
			pipelineInputAssemblyStateCreateInfo.sType$Default();
			pipelineInputAssemblyStateCreateInfo.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
			pipelineInputAssemblyStateCreateInfo.primitiveRestartEnable(false);

			VkPipelineViewportStateCreateInfo pipelineViewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc(stack);
			pipelineViewportStateCreateInfo.sType$Default();
			pipelineViewportStateCreateInfo.viewportCount(1);
			pipelineViewportStateCreateInfo.scissorCount(1);
			pipelineViewportStateCreateInfo.pViewports(VkViewport.calloc(1, stack));
			pipelineViewportStateCreateInfo.pScissors(VkRect2D.calloc(1, stack));

			VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateCreateInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack);
			pipelineRasterizationStateCreateInfo.sType$Default();
			pipelineRasterizationStateCreateInfo.depthClampEnable(false);
			pipelineRasterizationStateCreateInfo.rasterizerDiscardEnable(false);
			pipelineRasterizationStateCreateInfo.polygonMode(VK_POLYGON_MODE_FILL);
			pipelineRasterizationStateCreateInfo.lineWidth(1.0f);
			pipelineRasterizationStateCreateInfo.cullMode(VK_CULL_MODE_BACK_BIT);
			pipelineRasterizationStateCreateInfo.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
			pipelineRasterizationStateCreateInfo.depthBiasEnable(false);
			pipelineRasterizationStateCreateInfo.depthBiasConstantFactor(0.0f);
			pipelineRasterizationStateCreateInfo.depthBiasClamp(0.0f);
			pipelineRasterizationStateCreateInfo.depthBiasSlopeFactor(0.0f);
			
			VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack);
			pipelineMultisampleStateCreateInfo.sType$Default();
			pipelineMultisampleStateCreateInfo.sampleShadingEnable(false);
			pipelineMultisampleStateCreateInfo.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
			pipelineMultisampleStateCreateInfo.minSampleShading(1.0f);
			pipelineMultisampleStateCreateInfo.alphaToCoverageEnable(false);
			pipelineMultisampleStateCreateInfo.alphaToOneEnable(false);

			VkPipelineColorBlendAttachmentState.Buffer pipelineColoreBlendAttachmentState = VkPipelineColorBlendAttachmentState.calloc(1, stack);
			pipelineColoreBlendAttachmentState.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
			pipelineColoreBlendAttachmentState.blendEnable(false);
//			pipelineColoreBlendAttachmentState.srcColorBlendFactor(VK_BLEND_FACTOR_ONE);
//			pipelineColoreBlendAttachmentState.dstColorBlendFactor(VK_BLEND_FACTOR_ZERO);
//			pipelineColoreBlendAttachmentState.colorBlendOp(VK_BLEND_OP_ADD);
//			pipelineColoreBlendAttachmentState.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE);
//			pipelineColoreBlendAttachmentState.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
//			pipelineColoreBlendAttachmentState.alphaBlendOp(VK_BLEND_OP_ADD);
			
			VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack);
			pipelineColorBlendStateCreateInfo.sType$Default();
			pipelineColorBlendStateCreateInfo.logicOpEnable(false);
			pipelineColorBlendStateCreateInfo.attachmentCount(1);
			pipelineColorBlendStateCreateInfo.pAttachments(pipelineColoreBlendAttachmentState);

			VkGraphicsPipelineCreateInfo.Buffer graphicsPipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
			graphicsPipelineCreateInfo.sType$Default();
			graphicsPipelineCreateInfo.stageCount(2);
			graphicsPipelineCreateInfo.pStages(shaderStages);
			graphicsPipelineCreateInfo.pVertexInputState(pipelineVertexInputCreateInfo);
			graphicsPipelineCreateInfo.pInputAssemblyState(pipelineInputAssemblyStateCreateInfo);
			graphicsPipelineCreateInfo.pViewportState(pipelineViewportStateCreateInfo);
			graphicsPipelineCreateInfo.pRasterizationState(pipelineRasterizationStateCreateInfo);
			graphicsPipelineCreateInfo.pMultisampleState(pipelineMultisampleStateCreateInfo);
			graphicsPipelineCreateInfo.pColorBlendState(pipelineColorBlendStateCreateInfo);
			graphicsPipelineCreateInfo.pDynamicState(dynamicStateCreateInfo);
			graphicsPipelineCreateInfo.layout(settings.pipelineLayout);
			graphicsPipelineCreateInfo.renderPass(settings.renderPass);
			graphicsPipelineCreateInfo.subpass(0);
			
			this.handle = VknInternalUtils.createGraphicsPipeline(this.context.gpu.handle(), graphicsPipelineCreateInfo, stack);
		}
	}
	
	public long handle()
	{
		ensureValid();
		
		return this.handle;
	}

	public long layoutHandle()
	{
		ensureValid();
		
		return this.pipelineLayout;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroyPipeline(this.context.gpu.handle(), handle, null);
		
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		public long[] descriptorSetLayouts;
		public long renderPass;
		public VkVertexInputBindingDescription.Buffer bindingDescriptions;
		public VkVertexInputAttributeDescription.Buffer attributeDescriptions;
		public List<ShaderSettings> shaders = new ArrayList<>();
		public long pipelineLayout;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings addShader(int stage, VknShaderModule shaderModule)
		{
			this.shaders.add(new ShaderSettings(stage, shaderModule));
			
			return this;
		}
		
		public Settings addShader(int stage, VknShaderModule shaderModule, String entryPoint)
		{
			this.shaders.add(new ShaderSettings(stage, shaderModule, entryPoint));
			
			return this;
		}
	}
	
	public static class ShaderSettings
	{
		public VknShaderModule shaderModule;
		public int stage;
		public String entryPoint = "main";
		
		public ShaderSettings(int stage, VknShaderModule shaderModule)
		{
			this.stage = stage;
			this.shaderModule = shaderModule;
		}
		
		public ShaderSettings(int stage, VknShaderModule shaderModule, String entryPoint)
		{
			this.stage = stage;
			this.shaderModule = shaderModule;
			this.entryPoint = entryPoint;
		}
	}
}
