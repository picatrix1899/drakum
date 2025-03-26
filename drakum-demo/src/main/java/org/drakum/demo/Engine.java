package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.VK14.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;

import java.lang.foreign.ValueLayout;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.lwjgl.vulkan.EXTDebugUtils.*;

import org.barghos.util.byref.ByRef;
import org.barghos.util.math.MathUtils;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
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
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public class Engine
{
	private static final VkDebugUtilsMessengerCallbackEXT DEBUG_CALLBACK = new VkDebugUtilsMessengerCallbackEXT() {
		@Override
		public int invoke(int messageSeverity, int messageType, long pCallbackData, long pUserData)
		{
			VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
			System.err.println("Vulkan Debug [" + messageSeverity + "]: " + callbackData.pMessageString());
			return VK_FALSE; // Nicht Vulkan beenden
		}
	};

	private GPU gpu;
	
	private VkInstance vkInstance = null;
	private PointerBuffer glfwExtensions;
	private PointerBuffer enabledLayers;
	private long debugMessenger = 0;
	private long window = 0;
	private long surface = 0;
	
	private QueueFamilyList queueFamilies;

	private VkQueue graphicsQueue = null;
	private VkQueue presentQueue = null;
	
	private long vertexShaderModule;
	private long fragmentShaderModule;

	private VkExtent2D framebufferExtent;

	private long pipelineLayout;
	private long renderPass;
	private long graphicsPipeline;

	private long commandPool;
	private VkCommandBuffer commandBuffer;

	private long imageAvailableSemaphore;
	private long renderFinishedSemaphore;
	private long inFlightFence;

	private int swapchainImageCount;
	public int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
	public int swapchainColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
	private Swapchain swapchain;
	
	private long descriptorSetLayout;
	
	private long lastTime;
	
	private long descriptorPool;
	private long[] descriptorSets;
	private long[] uniformBuffers = null;
	private long[] uniformBuffersMemory = null;
	private long[] uniformBuffersMappedMemory = null;
	
	private Model model;
	
	public void start()
	{
		__init();

		run();

		__release();
	}

	public void __init()
	{
		initGLFW();
		initWindow();

		try (MemoryStack stack = MemoryStack.stackPush())
		{
			initVulkanInstance(stack);
			initDebugMessenger(vkInstance, stack);
			initSurface(stack);
			initDevice(stack);
			createRenderPass(stack);
			initSwapchain(stack);
			createDescriptorSetLayout(stack);
			initGraphicsPipeline(stack);
			initCommandBuffer(stack);
			createSyncObjects(stack);

			model = new Model();
			model.createVertexBuffer(gpu.device, gpu.physicalDevice, commandPool, graphicsQueue, stack);
			model.createIndexBuffer(gpu.device, gpu.physicalDevice, commandPool, graphicsQueue, stack);

			initUniformBuffers(stack);
			createDescriptorPool(stack);
			createDescriptorSets(stack);
			
			glfwShowWindow(window);
		}
		
		lastTime = System.nanoTime();
	}

	private void createDescriptorSets(MemoryStack stack)
	{
		long[] layouts = new long[swapchainImageCount];
		Arrays.fill(layouts, descriptorSetLayout);
		
		VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc(stack);
		descriptorSetAllocateInfo.sType$Default();
		descriptorSetAllocateInfo.descriptorPool(descriptorPool);
		descriptorSetAllocateInfo.pSetLayouts(stack.longs(layouts));
		
		LongBuffer buf = stack.callocLong(swapchainImageCount);
		
		vkAllocateDescriptorSets(gpu.device, descriptorSetAllocateInfo, buf);
		
		descriptorSets = new long[swapchainImageCount];
		
		buf.get(descriptorSets);
		
		for(int i = 0; i < swapchainImageCount; i++)
		{
			VkDescriptorBufferInfo.Buffer descriptorBufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
			descriptorBufferInfo.buffer(uniformBuffers[i]);
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
			
			vkUpdateDescriptorSets(gpu.device, writeDescriptorSet, null);
		}
	}
	
	private void createDescriptorPool(MemoryStack stack)
	{
		VkDescriptorPoolSize.Buffer descriptorPoolSize = VkDescriptorPoolSize.calloc(1, stack);
		descriptorPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		descriptorPoolSize.descriptorCount(swapchainImageCount);
		
		VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack);
		descriptorPoolCreateInfo.sType$Default();
		descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
		descriptorPoolCreateInfo.maxSets(swapchainImageCount);
		
		LongBuffer buf = stack.callocLong(1);
		
		vkCreateDescriptorPool(gpu.device, descriptorPoolCreateInfo, null, buf);
		
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
		
		vkCreateDescriptorSetLayout(gpu.device, descriptorSetLayoutCreateInfo, null, buf);
		
		descriptorSetLayout = buf.get(0);
	}
	
	private ByteBuffer readFile(String file, MemoryStack stack)
	{
		URL url = Engine.class.getResource(file);
		try
		{
			Path path = Paths.get(url.toURI());
			byte[] data = Files.readAllBytes(path);
			ByteBuffer code = stack.malloc(data.length);
			code.put(data);
			code.flip();

			return code;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private void initGLFW()
	{
		if (!glfwInit())
		{
			throw new Error("Cannot init glfw");
		}

		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.out));

		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
	}

	private void initWindow()
	{
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

		window = glfwCreateWindow(800, 600, "Drakum Demo", 0, 0);

		if (window == 0)
		{
			throw new Error("Cannot create window");
		}
	}

	private void initVulkanInstance(MemoryStack stack)
	{
		VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);
		appInfo.sType$Default();
		appInfo.pApplicationName(stack.UTF8("Drakum Demo"));
		appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
		appInfo.pEngineName(stack.UTF8("Drakum"));
		appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
		appInfo.apiVersion(VK_API_VERSION_1_4);

		glfwExtensions = glfwGetRequiredInstanceExtensions();

		PointerBuffer enabledExtensions = MemoryUtil.memAllocPointer(glfwExtensions.remaining() + 1);
		enabledExtensions.put(glfwExtensions);
		enabledExtensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
		enabledExtensions.flip();

		enabledLayers = stack.mallocPointer(1);
		enabledLayers.put(stack.UTF8("VK_LAYER_KHRONOS_validation"));
		enabledLayers.flip();

		VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc();
		debugCreateInfo.sType$Default();
		debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
		debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
		debugCreateInfo.pfnUserCallback(DEBUG_CALLBACK);

		VkInstanceCreateInfo vkInstanceCreateInfo = VkInstanceCreateInfo.calloc(stack);
		vkInstanceCreateInfo.sType$Default();
		vkInstanceCreateInfo.pApplicationInfo(appInfo);
		vkInstanceCreateInfo.ppEnabledExtensionNames(enabledExtensions);
		vkInstanceCreateInfo.ppEnabledLayerNames(enabledLayers);
		vkInstanceCreateInfo.pNext(debugCreateInfo);

		vkInstance = Utils.createInstance(vkInstanceCreateInfo, stack);
	}

	private void initSurface(MemoryStack stack)
	{
		surface = Utils.createWindowSurface(vkInstance, window, stack);
	}

	private void initDevice(MemoryStack stack)
	{
		gpu = new GPU();
		
		VkPhysicalDevice[] physicalDevices = Utils.enumeratePhysicalDevices(vkInstance, stack);

		for (VkPhysicalDevice physDevice : physicalDevices)
		{
			if (isDeviceSuitable(physDevice, stack))
			{
				gpu.physicalDevice = physDevice;

				break;
			}
		}

		VkQueueFamilyProperties[] familyPropertiesArray = Utils.getPhysicalDeviceQueueFamilyProperties(gpu.physicalDevice, stack);

		queueFamilies = new QueueFamilyList();
		
		for (int i = 0; i < familyPropertiesArray.length; i++)
		{
			VkQueueFamilyProperties familyProperties = familyPropertiesArray[i];

			if ((familyProperties.queueFlags() & (VK_QUEUE_GRAPHICS_BIT)) > 0)
			{
				queueFamilies.graphicsFamily = i;
			}

			if (Utils.getPhysicalDeviceSurfaceSupportKHR(gpu.physicalDevice, i, surface, stack))
			{
				queueFamilies.presentFamily = i;
			}

			if (queueFamilies.graphicsFamily > 1 && queueFamilies.presentFamily > 1)
			{
				break;
			}

			i++;
		}

		if (queueFamilies.graphicsFamily == -1)
		{
			throw new Error("Cannot find graphics family");
		}

		if (queueFamilies.presentFamily == -1)
		{
			throw new Error("Cannot find present family");
		}

		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(2, stack);
		queueCreateInfo.get(0).sType$Default();
		queueCreateInfo.get(0).queueFamilyIndex(queueFamilies.graphicsFamily);
		queueCreateInfo.get(0).pQueuePriorities(stack.floats(1.0f));

		queueCreateInfo.get(1).sType$Default();
		queueCreateInfo.get(1).queueFamilyIndex(queueFamilies.presentFamily);
		queueCreateInfo.get(1).pQueuePriorities(stack.floats(1.0f));

		VkPhysicalDeviceFeatures physicalDeviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

		PointerBuffer deviceExtensions = stack.mallocPointer(glfwExtensions.remaining() + 1);
		deviceExtensions.put(stack.UTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
		deviceExtensions.flip();

		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack);
		deviceCreateInfo.sType$Default();
		deviceCreateInfo.pQueueCreateInfos(queueCreateInfo);
		deviceCreateInfo.pEnabledFeatures(physicalDeviceFeatures);
		deviceCreateInfo.ppEnabledLayerNames(enabledLayers);
		deviceCreateInfo.ppEnabledExtensionNames(deviceExtensions);

		gpu.device = Utils.createDevice(deviceCreateInfo, gpu.physicalDevice, stack);

		graphicsQueue = Utils.getDeviceQueue(gpu.device, queueFamilies.graphicsFamily, 0, stack);
		presentQueue = Utils.getDeviceQueue(gpu.device, queueFamilies.presentFamily, 0, stack);
	}

	private void initSwapchain(MemoryStack stack)
	{
		VkSurfaceCapabilitiesKHR surfaceCapabilities = Utils.getPhysicalDeviceSurfaceCapabilities(gpu.physicalDevice, surface, stack);

		VkExtent2D actualExtent = Utils.getFramebufferSize(window, stack);

		framebufferExtent = VkExtent2D.calloc();

		framebufferExtent.width(Math.clamp(actualExtent.width(), surfaceCapabilities.minImageExtent().width(), surfaceCapabilities.maxImageExtent().width()));
		framebufferExtent.height(Math.clamp(actualExtent.height(), surfaceCapabilities.minImageExtent().height(), surfaceCapabilities.maxImageExtent().height()));

		int imageCount = surfaceCapabilities.minImageCount() + 1;
		if (surfaceCapabilities.maxImageCount() > 0)
		{
			imageCount = Math.clamp(imageCount, surfaceCapabilities.minImageCount(), surfaceCapabilities.maxImageCount());
		}
		
		swapchainImageCount = imageCount;
		
		swapchain = new Swapchain();
		swapchain.create(surfaceCapabilities, actualExtent.width(), actualExtent.height(), gpu.device, surface, queueFamilies, renderPass, swapchainImageCount);
	}

	public void initGraphicsPipeline(MemoryStack stack)
	{
		ByteBuffer vertexShaderData = readFile("/vert.spv", stack);
		ByteBuffer fragmentShaderData = readFile("/frag.spv", stack);

		VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
		vertexShaderModuleCreateInfo.sType$Default();
		vertexShaderModuleCreateInfo.pCode(vertexShaderData);

		vertexShaderModule = Utils.createShaderModule(gpu.device, vertexShaderModuleCreateInfo, stack);

		VkShaderModuleCreateInfo fragmentShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
		fragmentShaderModuleCreateInfo.sType$Default();
		fragmentShaderModuleCreateInfo.pCode(fragmentShaderData);

		fragmentShaderModule = Utils.createShaderModule(gpu.device, fragmentShaderModuleCreateInfo, stack);

		VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
		shaderStages.get(0).sType$Default();
		shaderStages.get(0).stage(VK_SHADER_STAGE_VERTEX_BIT);
		shaderStages.get(0).module(vertexShaderModule);
		shaderStages.get(0).pName(stack.UTF8("main"));
		shaderStages.get(1).sType$Default();
		shaderStages.get(1).stage(VK_SHADER_STAGE_FRAGMENT_BIT);
		shaderStages.get(1).module(fragmentShaderModule);
		shaderStages.get(1).pName(stack.UTF8("main"));

		VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack);
		dynamicStateCreateInfo.sType$Default();
		dynamicStateCreateInfo.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));

		VkVertexInputBindingDescription.Buffer bindingDescriptions = Model.Vertex.getBindingDecription(stack);
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = Model.Vertex.getAttributeDescription(stack);
		
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
		
		pipelineLayout = Utils.createPipelineLayout(gpu.device, pipelineLayoutCreateInfo, stack);

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

		graphicsPipeline = Utils.createGraphicsPipeline(gpu.device, graphicsPipelineCreateInfo, stack);
	}

	
	private void createRenderPass(MemoryStack stack)
	{
		VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
		colorAttachment.format(swapchainFormat);
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

		renderPass = Utils.createRenderPass(gpu.device, renderPassCreateInfo, stack);
	}
	
	private void initUniformBuffers(MemoryStack stack)
	{
		uniformBuffers = new long[swapchainImageCount];
		uniformBuffersMemory = new long[swapchainImageCount];
		uniformBuffersMappedMemory = new long[swapchainImageCount];
		
		for (int i = 0; i < swapchainImageCount; i++)
		{
			ByRef<Long> byrefBuffer = new ByRef<>();
			ByRef<Long> byrefBufferMemory = new ByRef<>();
			
			createBuffer(UBO.byteSize(), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, byrefBuffer, byrefBufferMemory, stack);
			
			long buffer = byrefBuffer.value;
			long bufferMemory = byrefBufferMemory.value;
			long mappedMemoryAddress = Utils.mapMemory(gpu.device, bufferMemory, 0, UBO.byteSize(), 0, stack);
			
			uniformBuffers[i] = buffer;
			uniformBuffersMemory[i] = bufferMemory;
			uniformBuffersMappedMemory[i] = mappedMemoryAddress;
		}
	}
	
	private void initCommandBuffer(MemoryStack stack)
	{
		VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
		commandPoolCreateInfo.sType$Default();
		commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		commandPoolCreateInfo.queueFamilyIndex(queueFamilies.graphicsFamily);

		commandPool = Utils.createCommandPool(gpu.device, commandPoolCreateInfo, stack);

		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.commandPool(commandPool);
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandBufferCount(1);

		commandBuffer = Utils.allocateCommandBuffer(gpu.device, commandBufferAllocateInfo, stack);

	}

	public void run()
	{
		while (!glfwWindowShouldClose(window))
		{
			update();
			render();
		}

		vkDeviceWaitIdle(gpu.device);
	}

	public void update()
	{
		glfwPollEvents();
	}

	public void render()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vkWaitForFences(gpu.device, inFlightFence, true, Long.MAX_VALUE);

			vkResetFences(gpu.device, inFlightFence);

			int imageIndex = Utils.acquireNextImage(gpu.device, swapchain.swapchain, imageAvailableSemaphore, stack);

			vkResetCommandBuffer(commandBuffer, 0);

			updateUniformBuffer(imageIndex);
			
			recordCommandBuffer(imageIndex);

			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType$Default();
			submitInfo.pWaitSemaphores(stack.longs(imageAvailableSemaphore));
			submitInfo.waitSemaphoreCount(1);
			submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
			submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
			submitInfo.pSignalSemaphores(stack.longs(renderFinishedSemaphore));

			vkQueueSubmit(graphicsQueue, submitInfo, inFlightFence);

			VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
			presentInfo.sType$Default();
			presentInfo.pWaitSemaphores(stack.longs(renderFinishedSemaphore));
			presentInfo.swapchainCount(1);
			presentInfo.pSwapchains(stack.longs(swapchain.swapchain));
			presentInfo.pImageIndices(stack.ints(imageIndex));

			vkQueuePresentKHR(presentQueue, presentInfo);
		}
	}

	private void updateUniformBuffer(int imageIndex)
	{
		long diff = System.nanoTime() - lastTime;
		
		float passedSeconds = (float)(diff / 1000000000.0);
		
		UBO ubo = new UBO();
		ubo.projection = new Matrix4f().perspective(MathUtils.DEG_TO_RADf * 45.0f, framebufferExtent.width() / framebufferExtent.height(), 0.1f, 10.0f);
		ubo.view = new Matrix4f().lookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		ubo.model = new Matrix4f().rotate(MathUtils.DEG_TO_RADf * 90.0f * passedSeconds, 0.0f, 0.0f, 1.0f);
		
		ubo.projection.m11(ubo.projection.m11() * -1);
		
		long currentBufferMappedMemory = uniformBuffersMappedMemory[imageIndex];
		
		FloatBuffer buf = MemoryUtil.memFloatBuffer(currentBufferMappedMemory, UBO.floatSize());
		
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
			renderArea.extent(framebufferExtent);

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
			renderPassBeginInfo.framebuffer(swapchain.swapchainFramebuffers[imageIndex]);
			renderPassBeginInfo.renderArea(renderArea);
			renderPassBeginInfo.clearValueCount(1);
			renderPassBeginInfo.pClearValues(clearColor);

			vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

			vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

			VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
			viewport.x(0.0f);
			viewport.y(0.0f);
			viewport.width(framebufferExtent.width());
			viewport.height(framebufferExtent.height());
			viewport.minDepth(0.0f);
			viewport.maxDepth(1.0f);

			vkCmdSetViewport(commandBuffer, 0, viewport);

			VkOffset2D scissorOffset = VkOffset2D.calloc(stack);
			scissorOffset.x(0);
			scissorOffset.y(0);

			VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
			scissor.offset(scissorOffset);
			scissor.extent(framebufferExtent);

			vkCmdSetScissor(commandBuffer, 0, scissor);

			LongBuffer vertexBuffers = stack.longs(model.vertexBuffer.buffer);
			LongBuffer offsets = stack.longs(0);
			
			vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
			
			vkCmdBindIndexBuffer(commandBuffer, model.indexBuffer.buffer, 0, VK_INDEX_TYPE_UINT32);
			
			vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, stack.longs(descriptorSets[imageIndex]), null);
			
			vkCmdDrawIndexed(commandBuffer, model.indices.length, 1, 0, 0, 0);

			vkCmdEndRenderPass(commandBuffer);

			vkEndCommandBuffer(commandBuffer);
		}
	}

	public void createSyncObjects(MemoryStack stack)
	{
		VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
		semaphoreCreateInfo.sType$Default();

		imageAvailableSemaphore = Utils.createSemaphore(gpu.device, semaphoreCreateInfo, stack);
		renderFinishedSemaphore = Utils.createSemaphore(gpu.device, semaphoreCreateInfo, stack);

		VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack);
		fenceCreateInfo.sType$Default();
		fenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

		inFlightFence = Utils.createFence(gpu.device, fenceCreateInfo, stack);
	}

	public void __release()
	{
		model.__release(gpu.device);

		vkDestroySemaphore(gpu.device, imageAvailableSemaphore, null);
		vkDestroySemaphore(gpu.device, renderFinishedSemaphore, null);

		vkDestroyFence(gpu.device, inFlightFence, null);

		vkDestroyCommandPool(gpu.device, commandPool, null);

		vkDestroyPipeline(gpu.device, graphicsPipeline, null);
		vkDestroyRenderPass(gpu.device, renderPass, null);
		vkDestroyPipelineLayout(gpu.device, pipelineLayout, null);

		for (int i = 0; i < uniformBuffers.length; i++)
		{
			vkDestroyBuffer(gpu.device, uniformBuffers[i], null);
			vkFreeMemory(gpu.device, uniformBuffersMemory[i], null);
		}

		swapchain.__release(gpu.device);
		
		vkDestroyDescriptorPool(gpu.device, descriptorPool, null);
		
		vkDestroyDescriptorSetLayout(gpu.device, descriptorSetLayout, null);
		
		vkDestroyShaderModule(gpu.device, vertexShaderModule, null);
		vkDestroyShaderModule(gpu.device, fragmentShaderModule, null);


		framebufferExtent.free();

		vkDestroySurfaceKHR(vkInstance, surface, null);

		vkDestroyDevice(gpu.device, null);

		EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(vkInstance, debugMessenger, null);

		vkDestroyInstance(vkInstance, null);

		glfwDestroyWindow(window);

		glfwTerminate();
	}

	private void initDebugMessenger(VkInstance instance, MemoryStack stack)
	{
		VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc();
		debugCreateInfo.sType$Default();
		debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
		debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
		debugCreateInfo.pfnUserCallback(DEBUG_CALLBACK);

		LongBuffer pMessenger = stack.mallocLong(1);

		if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, debugCreateInfo, null, pMessenger) != VK_SUCCESS)
		{
			throw new RuntimeException("Fehler beim Erstellen des Vulkan Debug Messengers.");
		}

		this.debugMessenger = pMessenger.get(0);
	}

	public boolean isDeviceSuitable(VkPhysicalDevice device, MemoryStack stack)
	{
		VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.calloc(stack);
		VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

		vkGetPhysicalDeviceProperties(device, deviceProperties);
		vkGetPhysicalDeviceFeatures(device, deviceFeatures);

		return deviceProperties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU && deviceFeatures.geometryShader();
	}
	
	public void copyBuffer(long srcBuffer, long dstBuffer, int size, MemoryStack stack)
	{
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandPool(commandPool);
		commandBufferAllocateInfo.commandBufferCount(1);
		
		VkCommandBuffer cmdBuffer = Utils.allocateCommandBuffer(gpu.device, commandBufferAllocateInfo, stack);
		
		VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
		commandBufferBeginInfo.sType$Default();
		commandBufferBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
		
		vkBeginCommandBuffer(cmdBuffer, commandBufferBeginInfo);
		
		VkBufferCopy.Buffer bufferCopy = VkBufferCopy.calloc(1, stack);
		bufferCopy.srcOffset(0);
		bufferCopy.dstOffset(0);
		bufferCopy.size(size);
		
		vkCmdCopyBuffer(cmdBuffer, srcBuffer, dstBuffer, bufferCopy);
		
		vkEndCommandBuffer(cmdBuffer);
		
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
		submitInfo.sType$Default();
		submitInfo.pCommandBuffers(stack.pointers(cmdBuffer));
		
		vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
		
		vkQueueWaitIdle(graphicsQueue);
		
		vkFreeCommandBuffers(gpu.device, commandPool, stack.pointers(cmdBuffer));
	}
	
	public void createBuffer(long size, int usage, int properties, ByRef<Long> buffer, ByRef<Long> bufferMemory, MemoryStack stack)
	{
		VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
		bufferCreateInfo.sType$Default();
		bufferCreateInfo.size(size);
		bufferCreateInfo.usage(usage);
		bufferCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
		
		long newBuffer = Utils.createBuffer(gpu.device, bufferCreateInfo, stack);
		
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
		
		vkGetBufferMemoryRequirements(gpu.device, newBuffer, memoryRequirements);
		
		VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
		memoryAllocateInfo.sType$Default();
		memoryAllocateInfo.allocationSize(memoryRequirements.size());
		memoryAllocateInfo.memoryTypeIndex(findMemoryType(memoryRequirements.memoryTypeBits(), properties, stack));
		
		LongBuffer memoryBuffer = stack.mallocLong(1);
		
		vkAllocateMemory(gpu.device, memoryAllocateInfo, null, memoryBuffer);
		
		long newBufferMemory = memoryBuffer.get(0);
		
		vkBindBufferMemory(gpu.device, newBuffer, newBufferMemory, 0);
		
		buffer.value = newBuffer;
		bufferMemory.value = newBufferMemory;
	}
	
	public int findMemoryType(int typeFilter, int properties, MemoryStack stack)
	{
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc(stack);
		
		vkGetPhysicalDeviceMemoryProperties(gpu.physicalDevice, memoryProperties);
		
		for(int i = 0; i < memoryProperties.memoryTypeCount(); i++)
		{
			if((typeFilter & (1 << i)) != 0 && (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)
			{
				return i;
			}
		}
		
		throw new Error();
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
