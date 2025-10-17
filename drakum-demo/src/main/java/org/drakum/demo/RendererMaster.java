package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.vulkan.VK14.*;

import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

import org.barghos.math.matrix.Mat4F;
import org.barghos.math.quaternion.QuatF;
import org.barghos.util.math.MathUtils;
import org.drakum.demo.Model.Vertex;
import org.drakum.demo.registry.HandleRegistry;
import org.drakum.demo.registry.LongId;
import org.drakum.demo.registry.Registry;
import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.GLFWContext;
import org.drakum.demo.vkn.VknBuffer;
import org.drakum.demo.vkn.VknCmdImageMemoryBarrier;
import org.drakum.demo.vkn.VknCommandPool;
import org.drakum.demo.vkn.VknContext;
import org.drakum.demo.vkn.VknFramebuffer2D;
import org.drakum.demo.vkn.VknGPU;
import org.drakum.demo.vkn.VknImage2D;
import org.drakum.demo.vkn.VknImageView2D;
import org.drakum.demo.vkn.VknInstance;
import org.drakum.demo.vkn.VknInternalUtils;
import org.drakum.demo.vkn.VknMemory;
import org.drakum.demo.vkn.VknPhysicalGPU;
import org.drakum.demo.vkn.VknPhysicalGPUList;
import org.drakum.demo.vkn.VknPipeline;
import org.drakum.demo.vkn.VknRenderPass;
import org.drakum.demo.vkn.VknSampler;
import org.drakum.demo.vkn.VknShaderModule;
import org.drakum.demo.vkn.VknSimpleDescriptorPool;
import org.drakum.demo.vkn.VknSimpleDescriptorSetLayout;
import org.drakum.demo.vkn.VknUtil;
import org.drakum.demo.vkn.VknWindow;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class RendererMaster
{
	private VknRenderPass renderPass;
	
	public Texture sceneTexture;
	public VknFramebuffer2D sceneFramebuffer;
	
	private VknSimpleDescriptorSetLayout uboDescriptorSetLayout;
	
	private VknSimpleDescriptorPool uboDescriptorPool;
	private VknSimpleDescriptorPool textureDescriptorPool;
	
	private long[] descriptorSets;
	private VknBuffer[] uniformBuffers;
	
	private Model model;
	private long lastTime;
	
	private VknWindow window;
	
	private long currentFrameIndex;

	private VknShaderModule vertexShaderModule;
	private VknShaderModule fragmentShaderModule;
	
	private Material material;
	
	private TexturedModel texturedModel;
	
	public Entity entity;
	
	public void init(AppSettings appSettings)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			GLFWContext.init();
			FFMImpl.init();
			
			CommonRenderContext.context = new VknContext();
			CommonRenderContext.context.instance = new VknInstance(new VknInstance.Settings().applicationName("Drakum Demo").engineName("Drakum").debugMode(appSettings.debug));
			
			VknPhysicalGPUList physicalGpuList = new VknPhysicalGPUList(new VknPhysicalGPUList.Settings(CommonRenderContext.context));
			VknPhysicalGPU physicalGpu = physicalGpuList.physicalGpus()[0];
			CommonRenderContext.context.gpu = new VknGPU(new VknGPU.Settings(CommonRenderContext.context).physicalGpu(physicalGpu));
			CommonRenderContext.context.commandPool = new VknCommandPool(new VknCommandPool.Settings(CommonRenderContext.context).flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT).queueFamilyIndex(CommonRenderContext.context.gpu.queueFamilies().graphicsFamily));
			
			this.window = new VknWindow(new VknWindow.Settings(CommonRenderContext.context).physicalGpu(CommonRenderContext.context.gpu.physicalGpu()).size(800, 600).title("Drakum Demo").inFlightFrameCount(appSettings.inflightFrames));
			
			createRenderPass(stack);
			
			VknImage2D sceneImage = new VknImage2D(new VknImage2D.Settings(CommonRenderContext.context).size(window.framebufferExtent).usageColorAttachment().usageTransferSrc().usageSampled());
			VknMemory sceneImageMemory = sceneImage.allocateAndBindMemory(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
			VknImageView2D sceneImageView = sceneImage.createView();
			
			this.sceneTexture = new Texture();
			this.sceneTexture.width = window.framebufferExtent.width();
			this.sceneTexture.height = window.framebufferExtent.height();
			this.sceneTexture.format = sceneImage.format();
			this.sceneTexture.texture = sceneImage;
			this.sceneTexture.textureMemory = sceneImageMemory;
			this.sceneTexture.textureView = sceneImageView;
			
			this.sceneFramebuffer = new VknFramebuffer2D(new VknFramebuffer2D.Settings(CommonRenderContext.context).renderPass(this.renderPass).addAttachment(sceneImageView).size(window.framebufferExtent));

			this.uboDescriptorSetLayout = new VknSimpleDescriptorSetLayout(new VknSimpleDescriptorSetLayout.Settings(CommonRenderContext.context).type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).stageFlags(VK_SHADER_STAGE_VERTEX_BIT));
			
			Vertex[] vertices = new Vertex[] {
					new Vertex(new Vector3f(-1f, -1f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(0, 0)),	
					new Vertex(new Vector3f(1f, -1f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(1, 0)),	
					new Vertex(new Vector3f(1f, 1f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(1, 1)),	
					new Vertex(new Vector3f(-1f, 1f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector2f(0, 1)),	
				};
			
			int[] indices = new int[] {0, 1, 2, 2, 3, 0};
			
			model = new Model(vertices, indices);
			Registry.registerModel(model);
			
			this.vertexShaderModule = new VknShaderModule(CommonRenderContext.context, "/vert.spv");
			this.fragmentShaderModule = new VknShaderModule(CommonRenderContext.context, "/frag.spv");
			Registry.registerShaderModule(vertexShaderModule);
			Registry.registerShaderModule(fragmentShaderModule);
			
			uboDescriptorPool = new VknSimpleDescriptorPool(new VknSimpleDescriptorPool.Settings(CommonRenderContext.context).type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER).descriptorCount(1).setCount(window.inFlightFrameCount));
			textureDescriptorPool = new VknSimpleDescriptorPool(new VknSimpleDescriptorPool.Settings(CommonRenderContext.context).type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).setCount(1));
			
			Texture texture = TextureLoader.createTexture("/texture.png");
			Registry.registerTexture(texture);
			
			VknSampler.Settings samplerSettings = new VknSampler.Settings(CommonRenderContext.context);
			samplerSettings.anisotropy = 16.0f;
			
			VknSampler textureSampler = new VknSampler(samplerSettings);
			
			this.material = new Material();
			this.material.textures.put(MaterialType.TEXTURE_ALBEDO, texture);
			this.material.samplers.put(MaterialType.TEXTURE_ALBEDO, textureSampler);
			this.material.type = MaterialType.FLAT_ALBEDO;
			Registry.registerMaterial(material);
			
			texturedModel = new TexturedModel();
			texturedModel.model = model;
			texturedModel.material = material;

			this.entity = new Entity();
			this.entity.model = texturedModel;
			
			createPipeline(this.model.attribFormat(), material.type, vertexShaderModule, fragmentShaderModule);

			initUniformBuffers(stack);
			
			createDescriptorSets(stack);
			
			this.material.type.getDescSet(this.textureDescriptorPool.handle(), material);
			
			this.window.windowShell().show();
		
			lastTime = System.nanoTime();
			last = System.nanoTime();
		}
	}
	
	private void createPipeline(AttribFormat attribFormat, MaterialType materialType, VknShaderModule vertexShader, VknShaderModule fragmentShader)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			long[] pipelineDescriptorLayouts = new long[] {uboDescriptorSetLayout.handle(), materialType.getLayout().handle()};
			
			VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc(stack);
			pipelineLayoutCreateInfo.sType$Default();
			pipelineLayoutCreateInfo.pSetLayouts(stack.longs(pipelineDescriptorLayouts));
			pipelineLayoutCreateInfo.setLayoutCount(pipelineDescriptorLayouts.length);
			
			LongId pipelineLayout = VknInternalUtils.createPipelineLayout(CommonRenderContext.context, pipelineLayoutCreateInfo, stack);
			
			VknPipeline.Settings pipelineCreateSettings = new VknPipeline.Settings(CommonRenderContext.context);
			pipelineCreateSettings.pipelineLayout = pipelineLayout;
			pipelineCreateSettings.renderPass = renderPass.handle();
			pipelineCreateSettings.bindingDescriptions = attribFormat.genBindingDescription(0, stack);
			pipelineCreateSettings.attributeDescriptions = attribFormat.genAttribDescription(0, stack);
			pipelineCreateSettings.addShader(VK_SHADER_STAGE_VERTEX_BIT, vertexShader);
			pipelineCreateSettings.addShader(VK_SHADER_STAGE_FRAGMENT_BIT, fragmentShader);
			
			Registry.createPipeline(attribFormat, materialType, pipelineCreateSettings);
		}
	}
	
	private void createDescriptorSets(MemoryStack stack)
	{
		long[] layouts = new long[window.inFlightFrameCount];
		Arrays.fill(layouts, this.uboDescriptorSetLayout.handle());
		
		VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
		descriptorSetAllocateInfo.sType$Default();
		descriptorSetAllocateInfo.descriptorPool(uboDescriptorPool.handle());
		descriptorSetAllocateInfo.pSetLayouts(stack.longs(layouts));
		
		descriptorSets = VknInternalUtils.allocateDescriptorSets(CommonRenderContext.context, descriptorSetAllocateInfo, window.inFlightFrameCount, stack);
		
		for(int i = 0; i < window.inFlightFrameCount; i++)
		{
			long uniformBufferHandle = HandleRegistry.BUFFER.get(uniformBuffers[i].handle());
			
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
			descriptorBufferInfo.buffer(uniformBufferHandle);
			descriptorBufferInfo.offset(0);
			descriptorBufferInfo.range(UBO.byteSize());

			VkWriteDescriptorSet.Buffer writeDescriptorSet = VkWriteDescriptorSet.calloc(1, stack);
			writeDescriptorSet.get(0).sType$Default();
			writeDescriptorSet.get(0).dstSet(descriptorSets[i]);
			writeDescriptorSet.get(0).dstBinding(0);
			writeDescriptorSet.get(0).dstArrayElement(0);
			writeDescriptorSet.get(0).descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
			writeDescriptorSet.get(0).descriptorCount(1);
			writeDescriptorSet.get(0).pBufferInfo(descriptorBufferInfo);

			vkUpdateDescriptorSets(CommonRenderContext.context.gpu.handle(), writeDescriptorSet, null);
		}
	}
	
	private void createRenderPass(MemoryStack stack)
	{
		VknRenderPass.Attachment renderPassAttachment = new VknRenderPass.Attachment();
		renderPassAttachment.format = window.surface().idealFormat().format;
		renderPassAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
		renderPassAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
		renderPassAttachment.finalLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
		
		VknRenderPass.Subpass subpass = new VknRenderPass.Subpass();
		subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
		subpass.addColorAttachmentReference(0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		
		VknRenderPass.SubpassDependency subpassDependency = new VknRenderPass.SubpassDependency();
		subpassDependency.srcSubpass = VK_SUBPASS_EXTERNAL;
		subpassDependency.dstSubpass = 0;
		subpassDependency.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		subpassDependency.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		subpassDependency.srcAccessMask = 0;
		subpassDependency.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
		
		VknRenderPass.Settings renderPassCreateSettings = new VknRenderPass.Settings(CommonRenderContext.context);
		renderPassCreateSettings.attachments.add(renderPassAttachment);
		renderPassCreateSettings.subpasses.add(subpass);
		renderPassCreateSettings.dependencies.add(subpassDependency);
		
		renderPass = new VknRenderPass(renderPassCreateSettings);
		
		Registry.registerRenderPass(renderPass);
	}
	
	private void initUniformBuffers(MemoryStack stack)
	{
		uniformBuffers = new VknBuffer[window.inFlightFrameCount];
		
		for (int i = 0; i < window.inFlightFrameCount; i++)
		{
			VknBuffer.Settings bufferCreateSettings = new VknBuffer.Settings(CommonRenderContext.context);
			bufferCreateSettings.size(UBO.byteSize());
			bufferCreateSettings.usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);
			bufferCreateSettings.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
			bufferCreateSettings.properties(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
			
			VknBuffer buffer = new VknBuffer(bufferCreateSettings);
			
			buffer.map();

			uniformBuffers[i] = buffer;
			
			Registry.registerBuffer(buffer);
		}
	}

	private int frameCount;
	private long last;
	
	public void update()
	{
		glfwPollEvents();
		
		if(window.windowShell().shouldClose()) Engine.stop();
		
		long current = System.nanoTime();
		
		long diff = current - last;
		
		float passedSeconds = (float)(diff / 1000000000.0);
		
		if(passedSeconds >= 1.0)
		{
			System.out.println(frameCount);
			frameCount = 0;
			
			last = current;
		}
		
		//window.swapchain.recreate(window.framebufferExtent);
	}

	public void render()
	{
		window.swapchain.beginFrame(currentFrameIndex);

		updateUniformBuffer();
		
		recordCommandBuffer();

		window.swapchain.endFrame();
		
		currentFrameIndex++;
		
		frameCount++;
	}

	private void updateUniformBuffer()
	{
		long diff = System.nanoTime() - lastTime;
		
		float passedSeconds = (float)(diff / 1000000000.0);
		
		UBO ubo = new UBO();
		ubo.view = new Matrix4f().lookAt(0.0f, 200.0f, 400.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		ubo.projection = new Mat4F().setPerspective(MathUtils.DEG_TO_RADf * 45.0f, window.framebufferExtent.width() / window.framebufferExtent.height(), 0.1f, 1000.0f).baseChange(1, 0, 0, 0, -1, 0, 0, 0, 1);
		
		QuatF q = new QuatF().setFromAxisAngle(0.0f, 0.0f, 1.0f, MathUtils.DEG_TO_RADf * 90.0f * passedSeconds);
		
		ubo.model = new Mat4F().scale3(100, 100, 100).mul(new Mat4F().setRotationByQuat(q));
		//ubo.model = new Mat4F().scale3(100, 100, 100).mul(new Mat4F().setRotationDeg(0.0f, 0.0f, 1.0f, 90.0f * passedSeconds));
		//ubo.model = new Mat4F().scale3(100, 100, 100).rotateDeg(0.0f, 0.0f, 1.0f, 90.0f * passedSeconds);
		
		VknBuffer currentBuffer = uniformBuffers[window.swapchain.currentInFlightFrame()];
		
		FloatBuffer buf = MemoryUtil.memFloatBuffer(currentBuffer.mappedMemoryHandle(), UBO.floatSize());

		buf.put(ubo.model.toArray());
		ubo.view.get(buf);
		buf.position(16 + 16);
		buf.put(ubo.projection.toArray());
	}
	
	private void recordCommandBuffer()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
			commandBufferBeginInfo.sType$Default();

			vkBeginCommandBuffer(window.swapchain.currentCmdBuffer(), commandBufferBeginInfo);

			VkRect2D renderArea = VkRect2D.calloc(stack);
			renderArea.extent().width(window.framebufferExtent.width()).height(window.framebufferExtent.height());

			VkClearValue.Buffer clearColor = VkClearValue.calloc(1, stack);
			clearColor.color().float32(0, 1.0f).float32(1, 1.0f).float32(2, 0.0f).float32(3, 1.0f);

			new VknCmdImageMemoryBarrier(window.swapchain.currentCmdBuffer(), sceneTexture.texture.handle().handle())
			.layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
			.accessMask(0, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
			.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.run();
			
			long renderPassHandle = HandleRegistry.RENDERPASS.get(renderPass.handle());
			
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc(stack);
			renderPassBeginInfo.sType$Default();
			renderPassBeginInfo.renderPass(renderPassHandle);
			renderPassBeginInfo.framebuffer(sceneFramebuffer.handle());
			renderPassBeginInfo.renderArea(renderArea);
			renderPassBeginInfo.clearValueCount(1);
			renderPassBeginInfo.pClearValues(clearColor);

			vkCmdBeginRenderPass(window.swapchain.currentCmdBuffer(), renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

			renderModel(this.entity, stack);

			vkCmdEndRenderPass(window.swapchain.currentCmdBuffer());
			
			window.swapchain.cmdPresent(sceneTexture.texture, stack);
			
			vkEndCommandBuffer(window.swapchain.currentCmdBuffer());
		}
	}

	public void renderModel(Entity entity, MemoryStack stack)
	{
		TexturedModel model = entity.model;
		
		PipelineKey key = new PipelineKey();
		key.attribFormatId = model.model.attribFormat().id();
		key.materialTypeId = model.material.type.getId();
		
		VknPipeline pipeline = Registry.vkPipelineRegistry.get(key);
		
		long pipelineHandle = HandleRegistry.PIPELINE.get(pipeline.handle());
		long pipelineLayoutHandle = HandleRegistry.PIPELINE_LAYOUT.get(pipeline.layoutHandle());
		
		vkCmdBindPipeline(window.swapchain.currentCmdBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineHandle);

		VknUtil.cmdSetViewport(window.swapchain.currentCmdBuffer(), 0.0f, 0.0f, window.framebufferExtent.width(), window.framebufferExtent.height(), 0.0f, 1.0f, stack);
		
		VknUtil.cmdSetScissor(window.swapchain.currentCmdBuffer(), 0, 0, window.framebufferExtent.width(), window.framebufferExtent.height(), stack);
		
		long vertexBufferHandle = HandleRegistry.BUFFER.get(model.model.vertexBuffer.handle());
		long indexBufferHandle = HandleRegistry.BUFFER.get(model.model.indexBuffer.handle());
		
		LongBuffer vertexBuffers = stack.longs(vertexBufferHandle);
		LongBuffer offsets = stack.longs(0);
		
		vkCmdBindVertexBuffers(window.swapchain.currentCmdBuffer(), 0, vertexBuffers, offsets);
		
		vkCmdBindIndexBuffer(window.swapchain.currentCmdBuffer(), indexBufferHandle, 0, VK_INDEX_TYPE_UINT32);
		
		vkCmdBindDescriptorSets(window.swapchain.currentCmdBuffer(), VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayoutHandle, 0, stack.longs(descriptorSets[window.swapchain.currentInFlightFrame()], model.material.type.getDescSet(this.textureDescriptorPool.handle(), model.material)), null);
		
		vkCmdDrawIndexed(window.swapchain.currentCmdBuffer(), model.model.indicesCount, 1, 0, 0, 0);
	}
	
	public void close()
	{
		vkDeviceWaitIdle(CommonRenderContext.context.gpu.handle());
		
		this.sceneFramebuffer.close();
		this.sceneTexture.close();
		
		this.uboDescriptorPool.close();
		this.textureDescriptorPool.close();
		
		this.uboDescriptorSetLayout.close();

		Registry.close();
		
		CommonRenderContext.context.commandPool.close();
		
		MaterialType.closeAll();

		this.window.close();
		
		CommonRenderContext.context.gpu.close();
		CommonRenderContext.context.instance.close();

		GLFWContext.close();
	}
	
	public static class UBO
	{
		public Mat4F projection;
		public Matrix4f view;
		public Mat4F model;
		
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
