package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK14.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;

import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import org.barghos.util.math.MathUtils;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class Engine
{
	private WindowRenderContext windowRenderContext;

	private Pipeline graphicsPipeline;
	
	private long renderPass;

	private VkCommandBuffer commandBuffer;

	private Semaphore imageAvailableSemaphore;
	private Semaphore renderFinishedSemaphore;
	private Fence inFlightFence;
	
	private long descriptorSetLayout;
	
	private long descriptorPool;
	private long[] descriptorSets;
	private VulkanBuffer[] uniformBuffers;
	
	private Model model;
	private long lastTime;

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

			CommonRenderContext.instance().vkInstance = new VulkanInstance.Builder()
				.applicationName("Drakum Demo")
				.engineName("Drakum")
				.debugMode(true)
				.create();
			
			windowRenderContext = new WindowRenderContext();
			
			windowRenderContext.window = new Window.Builder()
				.width(800)
				.height(600)
				.title("Drakum Demo")
				.create();
			
			windowRenderContext.surface = Utils.createWindowSurface(CommonRenderContext.instance().vkInstance.handle(), windowRenderContext.window.handle, stack);

			GPU gpu = new GPU();
			gpu.initDevice(windowRenderContext.surface, stack);
			CommonRenderContext.instance().gpu = gpu;
			
			createRenderPass(stack);
			
			VkSurfaceCapabilitiesKHR surfaceCapabilities = Utils.getPhysicalDeviceSurfaceCapabilities(gpu.physicalDevice, windowRenderContext.surface, stack);

			VkExtent2D actualExtent = Utils.getFramebufferSize(windowRenderContext.window.handle, stack);

			windowRenderContext.framebufferExtent = VkExtent2D.calloc();

			windowRenderContext.framebufferExtent.width(Math.clamp(actualExtent.width(), surfaceCapabilities.minImageExtent().width(), surfaceCapabilities.maxImageExtent().width()));
			windowRenderContext.framebufferExtent.height(Math.clamp(actualExtent.height(), surfaceCapabilities.minImageExtent().height(), surfaceCapabilities.maxImageExtent().height()));

			int imageCount = surfaceCapabilities.minImageCount() + 1;
			if (surfaceCapabilities.maxImageCount() > 0)
			{
				imageCount = Math.clamp(imageCount, surfaceCapabilities.minImageCount(), surfaceCapabilities.maxImageCount());
			}
			
			windowRenderContext.swapchainImageCount = imageCount;
			
			windowRenderContext.swapchain = new Swapchain();
			windowRenderContext.swapchain.create(surfaceCapabilities, actualExtent.width(), actualExtent.height(), windowRenderContext.surface, renderPass, windowRenderContext.swapchainImageCount);
			
			createDescriptorSetLayout(stack);

			graphicsPipeline = new Pipeline.Builder().create(windowRenderContext.framebufferExtent, descriptorSetLayout, renderPass, Model.Vertex.getBindingDecription(stack), Model.Vertex.getAttributeDescription(stack));
			
			initCommandBuffer(stack);
			createSyncObjects(stack);

			model = new Model();
			model.createVertexBuffer(stack);
			model.createIndexBuffer(stack);

			initUniformBuffers(stack);
			createDescriptorPool(stack);
			createDescriptorSets(stack);
			
			windowRenderContext.window.show();
		
			lastTime = System.nanoTime();
		}
	}

	private void createDescriptorSets(MemoryStack stack)
	{
		long[] layouts = new long[windowRenderContext.swapchainImageCount];
		Arrays.fill(layouts, descriptorSetLayout);
		
		VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
		descriptorSetAllocateInfo.sType$Default();
		descriptorSetAllocateInfo.descriptorPool(descriptorPool);
		descriptorSetAllocateInfo.pSetLayouts(stack.longs(layouts));
		
		LongBuffer buf = stack.callocLong(windowRenderContext.swapchainImageCount);
		
		vkAllocateDescriptorSets(CommonRenderContext.instance().gpu.device, descriptorSetAllocateInfo, buf);
		
		descriptorSets = new long[windowRenderContext.swapchainImageCount];
		
		buf.get(descriptorSets);
		
		for(int i = 0; i < windowRenderContext.swapchainImageCount; i++)
		{
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
			descriptorBufferInfo.buffer(uniformBuffers[i].handle());
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(UBO.byteSize());
			
			VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(1, stack);
			writeDescriptorSet.sType$Default();
			writeDescriptorSet.dstSet(descriptorSets[i]);
			writeDescriptorSet.dstBinding(0);
			writeDescriptorSet.dstArrayElement(0);
			writeDescriptorSet.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			writeDescriptorSet.descriptorCount(1);
			writeDescriptorSet.pBufferInfo(descriptorBufferInfo);
			
			vkUpdateDescriptorSets(CommonRenderContext.instance().gpu.device, writeDescriptorSet, null);
		}
	}
	
	private void createDescriptorPool(MemoryStack stack)
	{
		VkDescriptorPoolSize.Buffer descriptorPoolSize = VkDescriptorPoolSize.calloc(1, stack);
		descriptorPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		descriptorPoolSize.descriptorCount(windowRenderContext.swapchainImageCount);
		
		VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack);
		descriptorPoolCreateInfo.sType$Default();
		descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
		descriptorPoolCreateInfo.maxSets(windowRenderContext.swapchainImageCount);
		
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorPool(CommonRenderContext.instance().gpu.device, descriptorPoolCreateInfo, null, buf);
		
		descriptorPool = buf.get(0);
	}
	
	private void createDescriptorSetLayout(MemoryStack stack)
	{
		VkDescriptorSetLayoutBinding.Buffer descriptorSetLayoutBinding = VkDescriptorSetLayoutBinding.calloc(1, stack);
		descriptorSetLayoutBinding.binding(0);
		descriptorSetLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		descriptorSetLayoutBinding.descriptorCount(1);
		descriptorSetLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
		
		VkDescriptorSetLayoutCreateInfo descriptorSetLayoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
		descriptorSetLayoutCreateInfo.sType$Default();
		descriptorSetLayoutCreateInfo.pBindings(descriptorSetLayoutBinding);
		
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorSetLayout(CommonRenderContext.instance().gpu.device, descriptorSetLayoutCreateInfo, null, buf);
		
		descriptorSetLayout = buf.get(0);
	}
	
	private void createRenderPass(MemoryStack stack)
	{
		VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
		colorAttachment.format(windowRenderContext.swapchainFormat);
		colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
		colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
		colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
		colorAttachment.stencilLoadOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
		colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
		colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
		colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

		VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
		colorAttachmentRef.attachment(0);
		colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
		subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
		subpass.colorAttachmentCount(1);
		subpass.pColorAttachments(colorAttachmentRef);

		VkSubpassDependency.Buffer subpassDependency = VkSubpassDependency.calloc(1, stack);
		subpassDependency.srcSubpass(VK_SUBPASS_EXTERNAL);
		subpassDependency.dstSubpass(0);
		subpassDependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		subpassDependency.srcAccessMask(0);
		subpassDependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		subpassDependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

		VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
		renderPassCreateInfo.sType$Default();
		renderPassCreateInfo.pAttachments(colorAttachment);
		renderPassCreateInfo.pSubpasses(subpass);
		renderPassCreateInfo.pDependencies(subpassDependency);

		renderPass = Utils.createRenderPass(CommonRenderContext.instance().gpu.device, renderPassCreateInfo, stack);
	}
	
	private void initUniformBuffers(MemoryStack stack)
	{
		uniformBuffers = new VulkanBuffer[windowRenderContext.swapchainImageCount];
		
		for (int i = 0; i < windowRenderContext.swapchainImageCount; i++)
		{
			VulkanBuffer buffer = new VulkanBuffer.Builder()
					.size(UBO.byteSize())
					.usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
					.properties(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
					.create();
			
			buffer.map();

			uniformBuffers[i] = buffer;
		}
	}
	
	private void initCommandBuffer(MemoryStack stack)
	{
		VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
		commandPoolCreateInfo.sType$Default();
		commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		commandPoolCreateInfo.queueFamilyIndex(CommonRenderContext.instance().gpu.queueFamilies.graphicsFamily);

		CommonRenderContext.instance().commandPool = Utils.createCommandPool(CommonRenderContext.instance().gpu.device, commandPoolCreateInfo, stack);

		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.commandPool(CommonRenderContext.instance().commandPool);
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandBufferCount(1);

		commandBuffer = Utils.allocateCommandBuffer(CommonRenderContext.instance().gpu.device, commandBufferAllocateInfo, stack);

	}

	public void run()
	{
		while (!windowRenderContext.window.shouldClose())
		{
			update();
			render();
		}

		vkDeviceWaitIdle(CommonRenderContext.instance().gpu.device);
	}

	public void update()
	{
		glfwPollEvents();
	}

	public void render()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			inFlightFence.waitFor();
			inFlightFence.reset();

			int imageIndex = windowRenderContext.swapchain.acquireNextImage(imageAvailableSemaphore);

			vkResetCommandBuffer(commandBuffer, 0);

			updateUniformBuffer(imageIndex);
			
			recordCommandBuffer(imageIndex);

			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType$Default();
			submitInfo.pWaitSemaphores(stack.longs(imageAvailableSemaphore.handle()));
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
			submitInfo.pSignalSemaphores(stack.longs(renderFinishedSemaphore.handle()));

			vkQueueSubmit(CommonRenderContext.instance().gpu.graphicsQueue, submitInfo, inFlightFence.handle());

			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType$Default();
			presentInfo.pWaitSemaphores(stack.longs(renderFinishedSemaphore.handle()));
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(windowRenderContext.swapchain.swapchain));
			presentInfo.pImageIndices(stack.ints(imageIndex));

			vkQueuePresentKHR(CommonRenderContext.instance().gpu.presentQueue, presentInfo);
		}
	}

	private void updateUniformBuffer(int imageIndex)
	{
		long diff = System.nanoTime() - lastTime;
		
		float passedSeconds = (float)(diff / 1000000000.0);
		
		UBO ubo = new UBO();
		ubo.projection = new Matrix4f().perspective(MathUtils.DEG_TO_RADf * 45.0f, windowRenderContext.framebufferExtent.width() / windowRenderContext.framebufferExtent.height(), 0.1f, 10.0f);
		ubo.view = new Matrix4f().lookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		ubo.model = new Matrix4f().rotate(MathUtils.DEG_TO_RADf * 90.0f * passedSeconds, 0.0f, 0.0f, 1.0f);
		
		ubo.projection.m11(ubo.projection.m11() * -1);
		
		VulkanBuffer currentBuffer = uniformBuffers[imageIndex];
		
		FloatBuffer buf = MemoryUtil.memFloatBuffer(currentBuffer.mappedMemoryHandle(), UBO.floatSize());
		
		ubo.model.get(buf);
		buf.position(16);
		ubo.view.get(buf);
		buf.position(16 + 16);
		ubo.projection.get(buf);
	}
	
	private void recordCommandBuffer(int imageIndex)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
			commandBufferBeginInfo.sType$Default();

			vkBeginCommandBuffer(commandBuffer, commandBufferBeginInfo);

			VkOffset2D offset = VkOffset2D.calloc(stack);

			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.offset(offset);
			renderArea.extent(windowRenderContext.framebufferExtent);

			VkClearColorValue clearColorValue = VkClearColorValue.calloc(stack);
			clearColorValue.float32(0, 0.0f);
			clearColorValue.float32(1, 0.0f);
			clearColorValue.float32(2, 0.0f);
			clearColorValue.float32(3, 1.0f);

			VkClearValue.Buffer clearColor = VkClearValue.calloc(1, stack);
			clearColor.color(clearColorValue);

			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack);
			renderPassBeginInfo.sType$Default();
			renderPassBeginInfo.renderPass(renderPass);
			renderPassBeginInfo.framebuffer(windowRenderContext.swapchain.swapchainFramebuffers[imageIndex]);
			renderPassBeginInfo.renderArea(renderArea);
			renderPassBeginInfo.clearValueCount(1);
			renderPassBeginInfo.pClearValues(clearColor);

			vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

			vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.graphicsPipeline);

			VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
			viewport.x(0.0f);
			viewport.y(0.0f);
			viewport.width(windowRenderContext.framebufferExtent.width());
			viewport.height(windowRenderContext.framebufferExtent.height());
			viewport.minDepth(0.0f);
			viewport.maxDepth(1.0f);

			vkCmdSetViewport(commandBuffer, 0, viewport);

			VkOffset2D scissorOffset = VkOffset2D.calloc(stack);
			scissorOffset.x(0);
			scissorOffset.y(0);

			VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset(scissorOffset);
			scissor.extent(windowRenderContext.framebufferExtent);

			vkCmdSetScissor(commandBuffer, 0, scissor);

			LongBuffer vertexBuffers = stack.longs(model.vertexBuffer.handle());
			LongBuffer offsets = stack.longs(0);
			
			vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
			
			vkCmdBindIndexBuffer(commandBuffer, model.indexBuffer.handle(), 0, VK_INDEX_TYPE_UINT32);
			
			vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline.pipelineLayout, 0, stack.longs(descriptorSets[imageIndex]), null);
			
			vkCmdDrawIndexed(commandBuffer, model.indices.length, 1, 0, 0, 0);

			vkCmdEndRenderPass(commandBuffer);

			vkEndCommandBuffer(commandBuffer);
		}
	}

	public void createSyncObjects(MemoryStack stack)
	{
		imageAvailableSemaphore = new Semaphore.Builder().create();
		
		renderFinishedSemaphore = new Semaphore.Builder().create();

		inFlightFence = new Fence.Builder()
			.signaled()
			.create();
	}

	public void __release()
	{
		model.__release();

		imageAvailableSemaphore.__release();
		renderFinishedSemaphore.__release();

		inFlightFence.__release();

		vkDestroyCommandPool(CommonRenderContext.instance().gpu.device, CommonRenderContext.instance().commandPool, null);

		vkDestroyRenderPass(CommonRenderContext.instance().gpu.device, renderPass, null);
		
		graphicsPipeline.__release();

		for (int i = 0; i < uniformBuffers.length; i++)
		{
			uniformBuffers[i].__release();
		}

		windowRenderContext.swapchain.__release();
		
		vkDestroyDescriptorPool(CommonRenderContext.instance().gpu.device, descriptorPool, null);
		
		vkDestroyDescriptorSetLayout(CommonRenderContext.instance().gpu.device, descriptorSetLayout, null);

		windowRenderContext.framebufferExtent.free();

		vkDestroySurfaceKHR(CommonRenderContext.instance().vkInstance.handle(), windowRenderContext.surface, null);

		CommonRenderContext.instance().gpu.__release();

		CommonRenderContext.instance().vkInstance.__release();
		
		windowRenderContext.window.__release();

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
}
