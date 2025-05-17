package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK14.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.ValueLayout;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.barghos.util.math.MathUtils;
import org.drakum.demo.vkn.VknImage;
import org.drakum.demo.Model.Vertex;
import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.GLFWContext;
import org.drakum.demo.vkn.VknRenderPass;
import org.drakum.demo.vkn.VknBuffer;
import org.drakum.demo.vkn.VknCleanerUtils;
import org.drakum.demo.vkn.VknCmdImageMemoryBarrier;
import org.drakum.demo.vkn.VknGPU;
import org.drakum.demo.vkn.VknInstance;
import org.drakum.demo.vkn.VknPhysicalGPU;
import org.drakum.demo.vkn.VknPhysicalGPUList;
import org.drakum.demo.vkn.VknPipeline;
import org.drakum.demo.vkn.VknWindow;
import org.drakum.demo.vkn.VknInternalUtils;
import org.drakum.demo.vkn.VknMemory;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class Engine
{
	private VknPipeline graphicsPipeline;
	
	private VknRenderPass renderPass;
	
	private VknImage sceneImage;
	public long sceneImageView;
	public long sceneFramebuffer;
	
	private long descriptorSetLayout;
	
	private long descriptorPool;
	private long[] descriptorSets;
	private VknBuffer[] uniformBuffers;
	
	private Model model;
	private long lastTime;
	
	private VknWindow window;
	
	private long currentFrameIndex;
	
	private long texture;
	private long textureView;
	private VknMemory textureMemory;
	private long textureSampler;
	
	public void start()
	{
		__init();

		run();

		__release();
	}

	public void __init()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			GLFWContext.__init();
			
			VknInstance.Settings instanceCreateSettings = new VknInstance.Settings();
			instanceCreateSettings.applicationName = "Drakum Demo";
			instanceCreateSettings.engineName = "Drakum";
			instanceCreateSettings.debugMode = true;
			
			CommonRenderContext.vkInstance = new VknInstance(instanceCreateSettings);
			
			VknPhysicalGPUList.CreateSettings physicalGpuListCreateSettings = new VknPhysicalGPUList.CreateSettings();
			physicalGpuListCreateSettings.requirementProcessors.add((gpu) -> {
				boolean state = true;
				
				state = state && gpu.deviceFeatures().geometryShader == true;
				
				return state;
			});
			
			physicalGpuListCreateSettings.ratingProcessors.add((gpu) -> {
				int score = 0;
				
				if(gpu.deviceProperties().deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) score += 1000;
				
				return score;
			});
			
			VknPhysicalGPUList physicalGpuList = VknPhysicalGPUList.create(physicalGpuListCreateSettings);
			VknPhysicalGPU physicalGpu = physicalGpuList.physicalGpus()[0];
			
			VknGPU.CreateSettings gpuCreateSettings = new VknGPU.CreateSettings();
			gpuCreateSettings.physicalGpu = physicalGpu;
			
			CommonRenderContext.gpu = VknGPU.create(gpuCreateSettings);

			VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
			commandPoolCreateInfo.sType$Default();
			commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
			commandPoolCreateInfo.queueFamilyIndex(CommonRenderContext.gpu.queueFamilies().graphicsFamily);

			CommonRenderContext.commandPool = VknInternalUtils.createCommandPool(CommonRenderContext.gpu.handle(), commandPoolCreateInfo, stack);
			
			VknWindow.CreateSettings windowCreateSettings = new VknWindow.CreateSettings();
			windowCreateSettings.physicalGpu = CommonRenderContext.gpu.physicalGpu();
			windowCreateSettings.width = 800;
			windowCreateSettings.height = 600;
			windowCreateSettings.title = "Drakum Demo";
			windowCreateSettings.inFlightFrameCount = 8;
			
			this.window = VknWindow.create(windowCreateSettings);
			
			createRenderPass(stack);
			
			VknImage.Settings sceneImageSettings = new VknImage.Settings();
			sceneImageSettings.width = window.framebufferExtent.width;
			sceneImageSettings.height = window.framebufferExtent.height;
			
			sceneImage = new VknImage(sceneImageSettings);
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
			imageViewCreateInfo.sType$Default();
			imageViewCreateInfo.image(sceneImage.handle());
			imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			imageViewCreateInfo.format(this.window.surface().idealFormat().format);
			imageViewCreateInfo.subresourceRange(VkImageSubresourceRange.calloc(stack) 
					.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
					.baseMipLevel(0)
					.levelCount(1)
					.baseArrayLayer(0)
					.layerCount(1));
			
			sceneImageView = VknInternalUtils.createImageView(CommonRenderContext.gpu.handle(), imageViewCreateInfo, stack);
			
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
			framebufferCreateInfo.sType$Default();
			framebufferCreateInfo.renderPass(renderPass.handle());
			framebufferCreateInfo.attachmentCount(1);
			framebufferCreateInfo.pAttachments(stack.longs(sceneImageView));
			framebufferCreateInfo.width(window.framebufferExtent.width);
			framebufferCreateInfo.height(window.framebufferExtent.height);
			framebufferCreateInfo.layers(1);
			
			sceneFramebuffer = VknInternalUtils.createFramebuffer(CommonRenderContext.gpu.handle(), framebufferCreateInfo, stack);
			
			createDescriptorSetLayout(stack);

			VknPipeline.CreateSettings pipelineCreateSettings = new VknPipeline.CreateSettings();
			pipelineCreateSettings.framebufferExtent = window.framebufferExtent;
			pipelineCreateSettings.descriptorSetLayout = descriptorSetLayout;
			pipelineCreateSettings.renderPass = renderPass.handle();
			pipelineCreateSettings.bindingDescriptions = Model.Vertex.getBindingDecription(stack);
			pipelineCreateSettings.attributeDescriptions = Model.Vertex.getAttributeDescription(stack);
			
			graphicsPipeline = VknPipeline.create(pipelineCreateSettings);

			model = new Model();
			model.createVertexBuffer(stack);
			model.createIndexBuffer(stack);

			initTexture();
			
			initUniformBuffers(stack);
			createDescriptorPool(stack);
			createDescriptorSets(stack);

			this.window.windowShell().show();
		
			lastTime = System.nanoTime();
		}
	}

	private byte[] loadTextureData(BufferedImage image)
	{
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
		
		for(int y = 0; y < image.getHeight(); y++)
		{
			for(int x = 0; x < image.getWidth(); x++)
			{
				int pixelIndex = y * image.getWidth() + x;
				
				int pixel = pixels[pixelIndex];
				
				data[pixelIndex * 4  + 0] = (byte) ((pixel >> 16) & 0xFF); // Red component
				data[pixelIndex * 4  + 1] = (byte) ((pixel >> 8) & 0xFF); // Green component
				data[pixelIndex * 4  + 2] = (byte) (pixel & 0xFF); // Blue component
				data[pixelIndex * 4  + 3] = (byte) ((pixel >> 24) & 0xFF); // Alpha component
			}
		}

		return data;
	}
	
	private void initTexture()
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			try
			{
				URL url = Engine.class.getResource("/texture.png");
				Path path = Paths.get(url.toURI());
				InputStream stream = Files.newInputStream(path, StandardOpenOption.READ);
				
				BufferedImage img = ImageIO.read(stream);
				
				int width = img.getWidth();
				int height = img.getHeight();
				
				byte[] pixeldata = loadTextureData(img);
			
				stream.close();
				
				VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
				imageInfo.sType$Default();
				imageInfo.imageType(VK_IMAGE_TYPE_2D);
				imageInfo.format(VK_FORMAT_R8G8B8A8_UNORM); // oder UNORM für SDR
				imageInfo.extent().width(width).height(height).depth(1);
				imageInfo.mipLevels(1);
				imageInfo.arrayLayers(1);
				imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
				imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
				imageInfo.usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
				imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
				imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
				imageInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
				
				this.texture = VknInternalUtils.createImage(CommonRenderContext.gpu.handle(), imageInfo, stack);
				
				VkMemoryRequirements memReqs = VknInternalUtils.getImageMemoryRequirements(CommonRenderContext.gpu.handle(), this.texture, stack);
				
				VknMemory.Settings memoryCreateSettings = new VknMemory.Settings();
				memoryCreateSettings.size = memReqs.size();
				memoryCreateSettings.memoryTypeBits = memReqs.memoryTypeBits();
				memoryCreateSettings.properties = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
				
				this.textureMemory = new VknMemory(memoryCreateSettings);
				
				vkBindImageMemory(CommonRenderContext.gpu.handle(), this.texture, this.textureMemory.handle(), 0);
				
				VknBuffer.Settings stagingBufferCreateSettings = new VknBuffer.Settings();
				stagingBufferCreateSettings.size = memReqs.size();
				stagingBufferCreateSettings.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
				stagingBufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
				stagingBufferCreateSettings.properties = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
				
				VknBuffer stagingBuffer = new VknBuffer(stagingBufferCreateSettings);
				
				stagingBuffer.map();
				
				ByteBuffer mappedMemory = MemoryUtil.memByteBuffer(stagingBuffer.mappedMemoryHandle(), pixeldata.length);
				mappedMemory.put(pixeldata);
				
				stagingBuffer.unmap();
				
				VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
				commandBufferAllocateInfo.sType$Default();
				commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
				commandBufferAllocateInfo.commandPool(CommonRenderContext.commandPool);
				commandBufferAllocateInfo.commandBufferCount(1);
				
				VkCommandBuffer cmdBuffer = VknInternalUtils.allocateCommandBuffer(CommonRenderContext.gpu.handle(), commandBufferAllocateInfo, stack);
				
				VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
				commandBufferBeginInfo.sType$Default();
				commandBufferBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
				
				vkBeginCommandBuffer(cmdBuffer, commandBufferBeginInfo);
				
				new VknCmdImageMemoryBarrier(cmdBuffer, this.texture)
				.layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.accessMask(0, VK_ACCESS_TRANSFER_WRITE_BIT)
				.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
				.run();
				
				VkBufferImageCopy.Buffer bufferImageCopy = VkBufferImageCopy.calloc(1, stack);
				bufferImageCopy.bufferOffset(0);
				bufferImageCopy.bufferRowLength(0);
				bufferImageCopy.bufferImageHeight(0);
				bufferImageCopy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
				bufferImageCopy.imageOffset().set(0, 0, 0);
				bufferImageCopy.imageExtent().set(width, height, 1);
				
				vkCmdCopyBufferToImage(cmdBuffer, stagingBuffer.handle(), this.texture, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, bufferImageCopy);
				
				new VknCmdImageMemoryBarrier(cmdBuffer, this.texture)
				.layout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				.accessMask(VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_SHADER_READ_BIT)
				.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
				.run();
				
				vkEndCommandBuffer(cmdBuffer);
				
				VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
				submitInfo.sType$Default();
				submitInfo.pCommandBuffers(stack.pointers(cmdBuffer));
				
				vkQueueSubmit(CommonRenderContext.gpu.graphicsQueue(), submitInfo, VK_NULL_HANDLE);
				
				vkQueueWaitIdle(CommonRenderContext.gpu.graphicsQueue());
				
				vkFreeCommandBuffers(CommonRenderContext.gpu.handle(), CommonRenderContext.commandPool, stack.pointers(cmdBuffer));	
				
				stagingBuffer.close();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				throw new Error();
			}

			VkComponentMapping componentMapping = VkComponentMapping.calloc(stack);
			componentMapping.r(VK_COMPONENT_SWIZZLE_IDENTITY);
			componentMapping.g(VK_COMPONENT_SWIZZLE_IDENTITY);
			componentMapping.b(VK_COMPONENT_SWIZZLE_IDENTITY);
			componentMapping.a(VK_COMPONENT_SWIZZLE_IDENTITY);

			VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack);
			subresourceRange.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
			subresourceRange.baseMipLevel(0);
			subresourceRange.levelCount(1);
			subresourceRange.baseArrayLayer(0);
			subresourceRange.layerCount(1);
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
			imageViewCreateInfo.sType$Default();
			imageViewCreateInfo.image(this.texture);
			imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			imageViewCreateInfo.format(VK_FORMAT_R8G8B8A8_UNORM);
			imageViewCreateInfo.components(componentMapping);
			imageViewCreateInfo.subresourceRange(subresourceRange);

			this.textureView = VknInternalUtils.createImageView(CommonRenderContext.gpu.handle(), imageViewCreateInfo, stack);
			
			VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack)
			        .sType$Default()
			        .magFilter(VK_FILTER_LINEAR)
			        .minFilter(VK_FILTER_LINEAR)
			        .addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
			        .addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
			        .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
			        .anisotropyEnable(true)
			        .maxAnisotropy(16.0f)
			        .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
			        .unnormalizedCoordinates(false)
			        .compareEnable(false)
			        .compareOp(VK_COMPARE_OP_ALWAYS)
			        .mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR)
			        .minLod(0.0f)
			        .maxLod(0.0f)
			        .mipLodBias(0.0f);

			    LongBuffer pSampler = stack.mallocLong(1);
			    int err = vkCreateSampler(CommonRenderContext.gpu.handle(), samplerInfo, null, pSampler);
			    if (err != VK_SUCCESS) {
			        throw new RuntimeException("Failed to create texture sampler: " + err);
			    }

			    textureSampler = pSampler.get(0);
		}
	}
	
	private void createDescriptorSets(MemoryStack stack)
	{
		long[] layouts = new long[window.inFlightFrameCount];
		Arrays.fill(layouts, descriptorSetLayout);
		
		VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
		descriptorSetAllocateInfo.sType$Default();
		descriptorSetAllocateInfo.descriptorPool(descriptorPool);
		descriptorSetAllocateInfo.pSetLayouts(stack.longs(layouts));
		
		LongBuffer buf = stack.callocLong(window.inFlightFrameCount);
		
		vkAllocateDescriptorSets(CommonRenderContext.gpu.handle(), descriptorSetAllocateInfo, buf);
		
		descriptorSets = new long[window.inFlightFrameCount];
		
		buf.get(descriptorSets);
		
		for(int i = 0; i < window.inFlightFrameCount; i++)
		{
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
			descriptorBufferInfo.buffer(uniformBuffers[i].handle());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(UBO.byteSize());
			
			VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack)
				    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				    .imageView(this.textureView)
				    .sampler(this.textureSampler);
			
			VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(2, stack);
			writeDescriptorSet.get(0).sType$Default();
			writeDescriptorSet.get(0).dstSet(descriptorSets[i]);
			writeDescriptorSet.get(0).dstBinding(0);
			writeDescriptorSet.get(0).dstArrayElement(0);
			writeDescriptorSet.get(0).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			writeDescriptorSet.get(0).descriptorCount(1);
			writeDescriptorSet.get(0).pBufferInfo(descriptorBufferInfo);
			writeDescriptorSet.get(1).sType$Default();
			writeDescriptorSet.get(1).dstSet(descriptorSets[i]);
			writeDescriptorSet.get(1).dstBinding(1);
			writeDescriptorSet.get(1).dstArrayElement(0);
			writeDescriptorSet.get(1).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
			writeDescriptorSet.get(1).pImageInfo(imageInfo);
			writeDescriptorSet.get(1).descriptorCount(1);

			vkUpdateDescriptorSets(CommonRenderContext.gpu.handle(), writeDescriptorSet, null);
		}
	}
	
	private void createDescriptorPool(MemoryStack stack)
	{
		VkDescriptorPoolSize.Buffer descriptorPoolSize = VkDescriptorPoolSize.calloc(1, stack);
		descriptorPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		descriptorPoolSize.descriptorCount(window.inFlightFrameCount);
		
		VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack);
		descriptorPoolCreateInfo.sType$Default();
		descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
		descriptorPoolCreateInfo.maxSets(window.inFlightFrameCount);
		
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorPool(CommonRenderContext.gpu.handle(), descriptorPoolCreateInfo, null, buf);
		
		descriptorPool = buf.get(0);
	}
	
	private void createDescriptorSetLayout(MemoryStack stack)
	{
		VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBinding = VkDescriptorSetLayoutBinding.calloc(2, stack);
		descriptorSetLayoutBinding.get(0).binding(0);
		descriptorSetLayoutBinding.get(0).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		descriptorSetLayoutBinding.get(0).descriptorCount(1);
		descriptorSetLayoutBinding.get(0).stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
		
		descriptorSetLayoutBinding.get(1).binding(1);
		descriptorSetLayoutBinding.get(1).descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
		descriptorSetLayoutBinding.get(1).descriptorCount(1);
		descriptorSetLayoutBinding.get(1).stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
		
		VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
		descriptorSetLayoutCreateInfo.sType$Default();
		descriptorSetLayoutCreateInfo.pBindings(descriptorSetLayoutBinding);
		
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorSetLayout(CommonRenderContext.gpu.handle(), descriptorSetLayoutCreateInfo, null, buf);
		
		descriptorSetLayout = buf.get(0);
	}
	
	private void createRenderPass(MemoryStack stack)
	{
		VknRenderPass.Attachment renderPassAttachment = new VknRenderPass.Attachment();
		renderPassAttachment.format = window.surface().idealFormat().format;
		renderPassAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
		renderPassAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
		renderPassAttachment.finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
		
		VknRenderPass.SubpassAttachmentRef subpassAttachmentRef = new VknRenderPass.SubpassAttachmentRef();
		subpassAttachmentRef.attachementIndex = 0;
		subpassAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
		
		VknRenderPass.Subpass subpass = new VknRenderPass.Subpass();
		subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
		subpass.colorAttachmentReferences.add(subpassAttachmentRef);
		
		VknRenderPass.SubpassDependency subpassDependency = new VknRenderPass.SubpassDependency();
		subpassDependency.srcSubpass = VK_SUBPASS_EXTERNAL;
		subpassDependency.dstSubpass = 0;
		subpassDependency.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		subpassDependency.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		subpassDependency.srcAccessMask = 0;
		subpassDependency.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
		
		VknRenderPass.CreateSettings renderPassCreateSettings = new VknRenderPass.CreateSettings();
		renderPassCreateSettings.attachments.add(renderPassAttachment);
		renderPassCreateSettings.subpasses.add(subpass);
		renderPassCreateSettings.dependencies.add(subpassDependency);
		
		renderPass = VknRenderPass.create(renderPassCreateSettings);
	}
	
	private void initUniformBuffers(MemoryStack stack)
	{
		uniformBuffers = new VknBuffer[window.inFlightFrameCount];
		
		for (int i = 0; i < window.inFlightFrameCount; i++)
		{
			VknBuffer.Settings bufferCreateSettings = new VknBuffer.Settings();
			bufferCreateSettings.size = UBO.byteSize();
			bufferCreateSettings.usage = VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
			bufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
			bufferCreateSettings.properties = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
			
			VknBuffer buffer = new VknBuffer(bufferCreateSettings);
			
			buffer.map();

			uniformBuffers[i] = buffer;
		}
	}

	public void run()
	{
		while (!this.window.windowShell().shouldClose())
		{
			update();
			render();
			currentFrameIndex++;
		}

		vkDeviceWaitIdle(CommonRenderContext.gpu.handle());
	}

	public void update()
	{
		glfwPollEvents();
		
		//window.swapchain.recreate(window.framebufferExtent);
	}

	public void render()
	{
		window.swapchain.beginFrame(currentFrameIndex);

		updateUniformBuffer();
		
		recordCommandBuffer();

		window.swapchain.endFrame();
	}

	private void updateUniformBuffer()
	{
		long diff = System.nanoTime() - lastTime;
		
		float passedSeconds = (float)(diff / 1000000000.0);
		
		UBO ubo = new UBO();
		ubo.projection = new Matrix4f().perspective(MathUtils.DEG_TO_RADf * 45.0f, window.framebufferExtent.width / window.framebufferExtent.height, 0.1f, 10.0f);
		ubo.view = new Matrix4f().lookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		ubo.model = new Matrix4f().rotate(MathUtils.DEG_TO_RADf * 90.0f * passedSeconds, 0.0f, 0.0f, 1.0f);
		
		ubo.projection.m11(ubo.projection.m11() * -1);
		
		VknBuffer currentBuffer = uniformBuffers[window.swapchain.currentInFlightFrame()];
		
		FloatBuffer buf = MemoryUtil.memFloatBuffer(currentBuffer.mappedMemoryHandle(), UBO.floatSize());
		
		ubo.model.get(buf);
		buf.position(16);
		ubo.view.get(buf);
		buf.position(16 + 16);
		ubo.projection.get(buf);
	}
	
	private void recordCommandBuffer()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
			commandBufferBeginInfo.sType$Default();

			vkBeginCommandBuffer(window.swapchain.currentCmdBuffer(), commandBufferBeginInfo);

			VkOffset2D offset = VkOffset2D.calloc(stack);

			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.offset(offset);
			renderArea.extent().width(window.framebufferExtent.width).height(window.framebufferExtent.height);

			VkClearColorValue clearColorValue = VkClearColorValue.calloc(stack);
			clearColorValue.float32(0, 1.0f);
			clearColorValue.float32(1, 1.0f);
			clearColorValue.float32(2, 0.0f);
			clearColorValue.float32(3, 1.0f);

			VkClearValue.Buffer clearColor = VkClearValue.calloc(1, stack);
			clearColor.color(clearColorValue);

			new VknCmdImageMemoryBarrier(window.swapchain.currentCmdBuffer(), sceneImage.handle())
			.layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
			.accessMask(0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
			.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.run();
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack);
			renderPassBeginInfo.sType$Default();
			renderPassBeginInfo.renderPass(renderPass.handle());
			renderPassBeginInfo.framebuffer(sceneFramebuffer);
			renderPassBeginInfo.renderArea(renderArea);
			renderPassBeginInfo.clearValueCount(1);
			renderPassBeginInfo.pClearValues(clearColor);

			vkCmdBeginRenderPass(window.swapchain.currentCmdBuffer(), renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

			vkCmdBindPipeline(window.swapchain.currentCmdBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.graphicsPipeline);

			VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
			viewport.x(0.0f);
			viewport.y(0.0f);
			viewport.width(window.framebufferExtent.width);
			viewport.height(window.framebufferExtent.height);
			viewport.minDepth(0.0f);
			viewport.maxDepth(1.0f);

			vkCmdSetViewport(window.swapchain.currentCmdBuffer(), 0, viewport);

			VkOffset2D scissorOffset = VkOffset2D.calloc(stack);
			scissorOffset.x(0);
			scissorOffset.y(0);

			VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset(scissorOffset);
			scissor.extent().width(window.framebufferExtent.width).height(window.framebufferExtent.height);

			vkCmdSetScissor(window.swapchain.currentCmdBuffer(), 0, scissor);

			LongBuffer vertexBuffers = stack.longs(model.vertexBuffer.handle());
			LongBuffer offsets = stack.longs(0);
			
			vkCmdBindVertexBuffers(window.swapchain.currentCmdBuffer(), 0, vertexBuffers, offsets);
			
			vkCmdBindIndexBuffer(window.swapchain.currentCmdBuffer(), model.indexBuffer.handle(), 0, VK_INDEX_TYPE_UINT32);
			
			vkCmdBindDescriptorSets(window.swapchain.currentCmdBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.pipelineLayout, 0, stack.longs(descriptorSets[window.swapchain.currentInFlightFrame()]), null);
			
			vkCmdDrawIndexed(window.swapchain.currentCmdBuffer(), model.indices.length, 1, 0, 0, 0);

			vkCmdEndRenderPass(window.swapchain.currentCmdBuffer());
			
			window.swapchain.cmdPresent(sceneImage, stack);
			
			vkEndCommandBuffer(window.swapchain.currentCmdBuffer());
		}
	}

	public void __release()
	{
		vkDestroySampler(CommonRenderContext.gpu.handle(), textureSampler, null);
		vkDestroyImageView(CommonRenderContext.gpu.handle(), this.textureView, null);
		vkDestroyImage(CommonRenderContext.gpu.handle(), this.texture, null);
		this.textureMemory.close();
		
		model.__release();
		renderPass.__release();
		
		graphicsPipeline.__release();

		for (int i = 0; i < uniformBuffers.length; i++)
		{
			uniformBuffers[i].close();
		}
		
		vkDestroyFramebuffer(CommonRenderContext.gpu.handle(), sceneFramebuffer, null);
		
		vkDestroyImageView(CommonRenderContext.gpu.handle(), sceneImageView, null);
		
		sceneImage.close();
		
		vkDestroyDescriptorPool(CommonRenderContext.gpu.handle(), descriptorPool, null);
		
		vkDestroyDescriptorSetLayout(CommonRenderContext.gpu.handle(), descriptorSetLayout, null);

		vkDestroyCommandPool(CommonRenderContext.gpu.handle(), CommonRenderContext.commandPool, null);
		
		this.window.__release();
		
		CommonRenderContext.gpu.__release();
		
		CommonRenderContext.vkInstance.close();

		GLFWContext.__release();
	}
	
	public static class UBO
	{
		public Matrix4f projection;
		public Matrix4f view;
		public Matrix4f model;
		
		public static int byteSize()
		{
			return (int)ValueLayout.JAVA_FLOAT.byteSize() * floatSize();
		}
		
		public static int floatSize()
		{
			return 16 * 16 * 16;
		}
	}
	
	public static class Settings
	{
		
	}
}
