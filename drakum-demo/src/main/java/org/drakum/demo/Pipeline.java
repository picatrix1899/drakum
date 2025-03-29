package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

public class Pipeline
{
	public long pipelineLayout;
	public ShaderModule vertexShaderModule;
	public ShaderModule fragmentShaderModule;
	public long graphicsPipeline;
	
	public Pipeline()
	{
		
	}
	
	public void __release()
	{
		vkDestroyPipeline(CommonRenderContext.instance().gpu.device, graphicsPipeline, null);
		vkDestroyPipelineLayout(CommonRenderContext.instance().gpu.device, pipelineLayout, null);
		
		vertexShaderModule.__release();
		fragmentShaderModule.__release();
	}
	
	public static class Builder
	{
		public Pipeline create(VkExtent2D framebufferExtent, long descriptorSetLayout, long renderPass, VkVertexInputBindingDescription.Buffer bindingDescriptions, VkVertexInputAttributeDescription.Buffer attributeDescriptions)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				ShaderModule vertexShaderModule = new ShaderModule.Builder().create("/vert.spv");
				ShaderModule fragmentShaderModule = new ShaderModule.Builder().create("/frag.spv");

				VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
				shaderStages.get(0).sType$Default();
				shaderStages.get(0).stage(VK_SHADER_STAGE_VERTEX_BIT);
				shaderStages.get(0).module(vertexShaderModule.handle);
				shaderStages.get(0).pName(stack.UTF8("main"));
				shaderStages.get(1).sType$Default();
				shaderStages.get(1).stage(VK_SHADER_STAGE_FRAGMENT_BIT);
				shaderStages.get(1).module(fragmentShaderModule.handle);
				shaderStages.get(1).pName(stack.UTF8("main"));

				VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack);
				dynamicStateCreateInfo.sType$Default();
				dynamicStateCreateInfo.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));
				
				VkPipelineVertexInputStateCreateInfo pipelineVertexInputCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
				pipelineVertexInputCreateInfo.sType$Default();
				pipelineVertexInputCreateInfo.pVertexBindingDescriptions(bindingDescriptions);
				pipelineVertexInputCreateInfo.pVertexAttributeDescriptions(attributeDescriptions);
				
				VkPipelineInputAssemblyStateCreateInfo pipelineInputAssemblyStateCreateInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
				pipelineInputAssemblyStateCreateInfo.sType$Default();
				pipelineInputAssemblyStateCreateInfo.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
				pipelineInputAssemblyStateCreateInfo.primitiveRestartEnable(false);

				VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
				viewport.x(0.0f);
				viewport.y(0.0f);
				viewport.width(framebufferExtent.width());
				viewport.height(framebufferExtent.height());
				viewport.minDepth(0.0f);
				viewport.maxDepth(1.0f);

				VkOffset2D scissorOffset = VkOffset2D.calloc(stack);
				scissorOffset.x(0);
				scissorOffset.y(0);

				VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
				scissor.offset(scissorOffset);
				scissor.extent(framebufferExtent);

				VkPipelineViewportStateCreateInfo pipelineViewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc(stack);
				pipelineViewportStateCreateInfo.sType$Default();
				pipelineViewportStateCreateInfo.viewportCount(1);
				pipelineViewportStateCreateInfo.scissorCount(1);
				pipelineViewportStateCreateInfo.pViewports(viewport);
				pipelineViewportStateCreateInfo.pScissors(scissor);

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
				pipelineColoreBlendAttachmentState.blendEnable(true);
				pipelineColoreBlendAttachmentState.srcColorBlendFactor(VK_BLEND_FACTOR_ONE);
				pipelineColoreBlendAttachmentState.dstColorBlendFactor(VK_BLEND_FACTOR_ZERO);
				pipelineColoreBlendAttachmentState.colorBlendOp(VK_BLEND_OP_ADD);
				pipelineColoreBlendAttachmentState.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE);
				pipelineColoreBlendAttachmentState.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
				pipelineColoreBlendAttachmentState.alphaBlendOp(VK_BLEND_OP_ADD);

				VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack);
				pipelineColorBlendStateCreateInfo.sType$Default();
				pipelineColorBlendStateCreateInfo.logicOpEnable(false);
				pipelineColorBlendStateCreateInfo.attachmentCount(1);
				pipelineColorBlendStateCreateInfo.pAttachments(pipelineColoreBlendAttachmentState);

				VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack);
				pipelineLayoutCreateInfo.sType$Default();
				pipelineLayoutCreateInfo.pSetLayouts(stack.longs(descriptorSetLayout));
				pipelineLayoutCreateInfo.setLayoutCount(1);
				
				long pipelineLayout = Utils.createPipelineLayout(CommonRenderContext.instance().gpu.device, pipelineLayoutCreateInfo, stack);

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
				graphicsPipelineCreateInfo.layout(pipelineLayout);
				graphicsPipelineCreateInfo.renderPass(renderPass);
				graphicsPipelineCreateInfo.subpass(0);

				long graphicsPipeline = Utils.createGraphicsPipeline(CommonRenderContext.instance().gpu.device, graphicsPipelineCreateInfo, stack);
				
				Pipeline result = new Pipeline();
				result.vertexShaderModule = vertexShaderModule;
				result.fragmentShaderModule =fragmentShaderModule;
				result.pipelineLayout = pipelineLayout;
				result.graphicsPipeline = graphicsPipeline;
				
				return result;
			}
		}
	}
}
