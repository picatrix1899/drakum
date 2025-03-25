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

import static org.lwjgl.vulkan.EXTDebugUtils.*;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
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
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

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

	private VkInstance vkInstance = null;
	private PointerBuffer glfwExtensions;
	private PointerBuffer enabledLayers;
	private VkInstanceCreateInfo vkInstanceCreateInfo = null;
	private long debugMessenger = 0;
	private VkDevice device = null;
	private long window = 0;
	private long surface = 0;

	private int graphicsFamily = -1;
	private int presentFamily = -1;

	private VkQueue graphicsQueue = null;
	private VkQueue presentQueue = null;
	private VkPhysicalDevice physicalDevice = null;

	private int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
	private int swapchainColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;

	private long swapchain = 0;
	private long[] swapchainImageViews = null;
	private long[] swapchainFramebuffers = null;

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

	private Vertex[] vertices = new Vertex[] {
		new Vertex(new Vector2f(0.0f, -0.5f), new Vector3f(1.0f, 0.0f, 0.0f)),	
		new Vertex(new Vector2f(0.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f)),	
		new Vertex(new Vector2f(-0.5f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f)),	
	};
	
	private long vertexBuffer;
	private long vertexBufferMemory;
	
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
			initSwapchain(stack);
			initGraphicsPipeline(stack);
			initSwapchainFramebuffers(stack);
			initCommandBuffer(stack);
			createSyncObjects(stack);

			createVertextBuffer(stack);
			
			glfwShowWindow(window);
		}
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

		vkInstanceCreateInfo = VkInstanceCreateInfo.calloc();
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
		VkPhysicalDevice[] physicalDevices = Utils.enumeratePhysicalDevices(vkInstance, stack);

		for (VkPhysicalDevice physDevice : physicalDevices)
		{
			if (isDeviceSuitable(physDevice, stack))
			{
				physicalDevice = physDevice;

				break;
			}
		}

		VkQueueFamilyProperties[] familyPropertiesArray = Utils.getPhysicalDeviceQueueFamilyProperties(physicalDevice, stack);

		for (int i = 0; i < familyPropertiesArray.length; i++)
		{
			VkQueueFamilyProperties familyProperties = familyPropertiesArray[i];

			if ((familyProperties.queueFlags() & (VK_QUEUE_GRAPHICS_BIT)) > 0)
			{
				graphicsFamily = i;
			}

			if (Utils.getPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, stack))
			{
				presentFamily = i;
			}

			if (graphicsFamily > 1 && presentFamily > 1)
			{
				break;
			}

			i++;
		}

		if (graphicsFamily == -1)
		{
			throw new Error("Cannot find graphics family");
		}

		if (presentFamily == -1)
		{
			throw new Error("Cannot find present family");
		}

		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(2, stack);
		queueCreateInfo.get(0).sType$Default();
		queueCreateInfo.get(0).queueFamilyIndex(graphicsFamily);
		queueCreateInfo.get(0).pQueuePriorities(stack.floats(1.0f));

		queueCreateInfo.get(1).sType$Default();
		queueCreateInfo.get(1).queueFamilyIndex(presentFamily);
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

		device = Utils.createDevice(deviceCreateInfo, physicalDevice, stack);

		graphicsQueue = Utils.getDeviceQueue(device, graphicsFamily, 0, stack);
		presentQueue = Utils.getDeviceQueue(device, presentFamily, 0, stack);
	}

	private void initSwapchain(MemoryStack stack)
	{
		VkSurfaceCapabilitiesKHR surfaceCapabilities = Utils.getPhysicalDeviceSurfaceCapabilities(physicalDevice, surface, stack);

		VkExtent2D actualExtent = Utils.getFramebufferSize(window, stack);

		framebufferExtent = VkExtent2D.calloc();

		framebufferExtent.width(Math.clamp(actualExtent.width(), surfaceCapabilities.minImageExtent().width(), surfaceCapabilities.maxImageExtent().width()));
		framebufferExtent.height(Math.clamp(actualExtent.height(), surfaceCapabilities.minImageExtent().height(), surfaceCapabilities.maxImageExtent().height()));

		int imageCount = surfaceCapabilities.minImageCount() + 1;
		if (surfaceCapabilities.maxImageCount() > 0)
		{
			imageCount = Math.clamp(imageCount, surfaceCapabilities.minImageCount(), surfaceCapabilities.maxImageCount());
		}

		VkSwapchainCreateInfoKHR swapchainCreateInfo = VkSwapchainCreateInfoKHR.calloc(stack);
		swapchainCreateInfo.sType$Default();
		swapchainCreateInfo.surface(surface);
		swapchainCreateInfo.minImageCount(imageCount);
		swapchainCreateInfo.imageFormat(swapchainFormat);
		swapchainCreateInfo.imageColorSpace(swapchainColorSpace);
		swapchainCreateInfo.imageExtent(actualExtent);
		swapchainCreateInfo.imageArrayLayers(1);
		swapchainCreateInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

		if (graphicsFamily != presentFamily)
		{
			swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
			swapchainCreateInfo.pQueueFamilyIndices(stack.ints(graphicsFamily, presentFamily));
		} else
		{
			swapchainCreateInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
		}

		swapchainCreateInfo.preTransform(surfaceCapabilities.currentTransform());
		swapchainCreateInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		swapchainCreateInfo.presentMode(VK_PRESENT_MODE_MAILBOX_KHR);
		swapchainCreateInfo.clipped(true);

		swapchain = Utils.createSwapchain(device, swapchainCreateInfo, stack);

		long[] swapchainImages = Utils.getSwapchainImages(device, swapchain, stack);
		swapchainImageViews = new long[swapchainImages.length];

		for (int i = 0; i < swapchainImages.length; i++)
		{
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
			imageViewCreateInfo.image(swapchainImages[i]);
			imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			imageViewCreateInfo.format(swapchainFormat);
			imageViewCreateInfo.components(componentMapping);
			imageViewCreateInfo.subresourceRange(subresourceRange);

			swapchainImageViews[i] = Utils.createImageView(device, imageViewCreateInfo, stack);
		}
	}

	public void initGraphicsPipeline(MemoryStack stack)
	{
		ByteBuffer vertexShaderData = readFile("/vert.spv", stack);
		ByteBuffer fragmentShaderData = readFile("/frag.spv", stack);

		VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
		vertexShaderModuleCreateInfo.sType$Default();
		vertexShaderModuleCreateInfo.pCode(vertexShaderData);

		vertexShaderModule = Utils.createShaderModule(device, vertexShaderModuleCreateInfo, stack);

		VkShaderModuleCreateInfo fragmentShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
		fragmentShaderModuleCreateInfo.sType$Default();
		fragmentShaderModuleCreateInfo.pCode(fragmentShaderData);

		fragmentShaderModule = Utils.createShaderModule(device, fragmentShaderModuleCreateInfo, stack);

		VkPipelineShaderStageCreateInfo vertexPipelineShaderStageCreateInfo = VkPipelineShaderStageCreateInfo.calloc(stack);
		vertexPipelineShaderStageCreateInfo.sType$Default();
		vertexPipelineShaderStageCreateInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
		vertexPipelineShaderStageCreateInfo.module(vertexShaderModule);
		vertexPipelineShaderStageCreateInfo.pName(stack.UTF8("main"));

		VkPipelineShaderStageCreateInfo fragmentPipelineShaderStageCreateInfo = VkPipelineShaderStageCreateInfo.calloc(stack);
		fragmentPipelineShaderStageCreateInfo.sType$Default();
		fragmentPipelineShaderStageCreateInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
		fragmentPipelineShaderStageCreateInfo.module(fragmentShaderModule);
		fragmentPipelineShaderStageCreateInfo.pName(stack.UTF8("main"));

		VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
		shaderStages.put(0, vertexPipelineShaderStageCreateInfo);
		shaderStages.put(1, fragmentPipelineShaderStageCreateInfo);

		VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc(stack);
		dynamicStateCreateInfo.sType$Default();
		dynamicStateCreateInfo.pDynamicStates(stack.ints(VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR));

		VkVertexInputBindingDescription.Buffer bindingDescriptions = Vertex.getBindingDecription(stack);
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = Vertex.getAttributeDescription(stack);
		
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
		pipelineRasterizationStateCreateInfo.frontFace(VK_FRONT_FACE_CLOCKWISE);
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

		pipelineLayout = Utils.createPipelineLayout(device, pipelineLayoutCreateInfo, stack);

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

		renderPass = Utils.createRenderPass(device, renderPassCreateInfo, stack);

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

		graphicsPipeline = Utils.createGraphicsPipeline(device, graphicsPipelineCreateInfo, stack);
	}

	private void initSwapchainFramebuffers(MemoryStack stack)
	{
		swapchainFramebuffers = new long[swapchainImageViews.length];

		for (int i = 0; i < swapchainImageViews.length; i++)
		{
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
			framebufferCreateInfo.sType$Default();
			framebufferCreateInfo.renderPass(renderPass);
			framebufferCreateInfo.attachmentCount(1);
			framebufferCreateInfo.pAttachments(stack.longs(swapchainImageViews[i]));
			framebufferCreateInfo.width(framebufferExtent.width());
			framebufferCreateInfo.height(framebufferExtent.height());
			framebufferCreateInfo.layers(1);

			swapchainFramebuffers[i] = Utils.createFramebuffer(device, framebufferCreateInfo, stack);
		}
	}

	private void initCommandBuffer(MemoryStack stack)
	{
		VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack);
		commandPoolCreateInfo.sType$Default();
		commandPoolCreateInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		commandPoolCreateInfo.queueFamilyIndex(graphicsFamily);

		commandPool = Utils.createCommandPool(device, commandPoolCreateInfo, stack);

		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.commandPool(commandPool);
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandBufferCount(1);

		commandBuffer = Utils.allocateCommandBuffer(device, commandBufferAllocateInfo, stack);

	}

	public void run()
	{
		while (!glfwWindowShouldClose(window))
		{
			update();
			render();
		}

		vkDeviceWaitIdle(device);
	}

	public void update()
	{
		glfwPollEvents();
	}

	public void render()
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			vkWaitForFences(device, inFlightFence, true, Long.MAX_VALUE);

			vkResetFences(device, inFlightFence);

			int imageIndex = Utils.acquireNextImage(device, swapchain, imageAvailableSemaphore, stack);

			vkResetCommandBuffer(commandBuffer, 0);

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
			presentInfo.pSwapchains(stack.longs(swapchain));
			presentInfo.pImageIndices(stack.ints(imageIndex));

			vkQueuePresentKHR(presentQueue, presentInfo);
		}
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
			renderPassBeginInfo.framebuffer(swapchainFramebuffers[imageIndex]);
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

			LongBuffer vertexBuffers = stack.longs(vertexBuffer);
			LongBuffer offsets = stack.longs(0);
			
			vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
			
			vkCmdDraw(commandBuffer, vertices.length, 1, 0, 0);

			vkCmdEndRenderPass(commandBuffer);

			vkEndCommandBuffer(commandBuffer);
		}
	}

	public void createSyncObjects(MemoryStack stack)
	{
		VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack);
		semaphoreCreateInfo.sType$Default();

		imageAvailableSemaphore = Utils.createSemaphore(device, semaphoreCreateInfo, stack);
		renderFinishedSemaphore = Utils.createSemaphore(device, semaphoreCreateInfo, stack);

		VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack);
		fenceCreateInfo.sType$Default();
		fenceCreateInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

		inFlightFence = Utils.createFence(device, fenceCreateInfo, stack);
	}

	public void __release()
	{
		vkDestroyBuffer(device, vertexBuffer, null);
		vkFreeMemory(device, vertexBufferMemory, null);
		
		vkDestroySemaphore(device, imageAvailableSemaphore, null);
		vkDestroySemaphore(device, renderFinishedSemaphore, null);

		vkDestroyFence(device, inFlightFence, null);

		vkDestroyCommandPool(device, commandPool, null);

		for (long framebuffer : swapchainFramebuffers)
		{
			vkDestroyFramebuffer(device, framebuffer, null);
		}

		vkDestroyPipeline(device, graphicsPipeline, null);
		vkDestroyRenderPass(device, renderPass, null);
		vkDestroyPipelineLayout(device, pipelineLayout, null);

		vkDestroyShaderModule(device, vertexShaderModule, null);
		vkDestroyShaderModule(device, fragmentShaderModule, null);

		for (long imageView : swapchainImageViews)
		{
			vkDestroyImageView(device, imageView, null);
		}

		vkDestroySwapchainKHR(device, swapchain, null);

		framebufferExtent.free();

		vkDestroySurfaceKHR(vkInstance, surface, null);

		vkDestroyDevice(device, null);

		EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(vkInstance, debugMessenger, null);

		vkDestroyInstance(vkInstance, null);

		vkInstanceCreateInfo.free();

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
	
	public void createVertextBuffer(MemoryStack stack)
	{
		VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
		bufferCreateInfo.sType$Default();
		bufferCreateInfo.size(((int)ValueLayout.JAVA_FLOAT.byteSize() * (2 + 3)) * vertices.length);
		bufferCreateInfo.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
		bufferCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
		
		vertexBuffer = Utils.createBuffer(device, bufferCreateInfo, stack);
		
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
		
		vkGetBufferMemoryRequirements(device, vertexBuffer, memoryRequirements);
		
		VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc(stack);
		memoryAllocateInfo.sType$Default();
		memoryAllocateInfo.allocationSize(memoryRequirements.size());
		memoryAllocateInfo.memoryTypeIndex(findMemoryType(memoryRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stack));
		
		LongBuffer memoryBuffer = stack.mallocLong(1);
		
		vkAllocateMemory(device, memoryAllocateInfo, null, memoryBuffer);
		
		vertexBufferMemory = memoryBuffer.get(0);
		
		vkBindBufferMemory(device, vertexBuffer, vertexBufferMemory, 0);
		
		PointerBuffer mappedMemoryBuffer = stack.mallocPointer(1);
		
		vkMapMemory(device, vertexBufferMemory, 0, ((int)ValueLayout.JAVA_FLOAT.byteSize() * (2 + 3)) * vertices.length, 0, mappedMemoryBuffer);
		
//		ByteBuffer mappedMemory = stack.calloc(((int)ValueLayout.JAVA_FLOAT.byteSize() * (2 + 3)) * vertices.length);
		
		FloatBuffer floatMappedMemory = MemoryUtil.memFloatBuffer(mappedMemoryBuffer.get(0), (2 + 3) * vertices.length);
		
		for(Vertex v : vertices)
		{
			floatMappedMemory.put(v.pos.x);
			floatMappedMemory.put(v.pos.y);
			floatMappedMemory.put(v.color.x);
			floatMappedMemory.put(v.color.y);
			floatMappedMemory.put(v.color.z);
		}
		
		floatMappedMemory.flip();
		
		//MemoryUtil.memCopy(MemoryUtil.memAddress(memoryBuffer), mappedMemoryBuffer.get(0), ((int)ValueLayout.JAVA_FLOAT.byteSize() * (2 + 3)) * vertices.length);
		
		vkUnmapMemory(device, vertexBufferMemory);
	}
	
	public int findMemoryType(int typeFilter, int properties, MemoryStack stack)
	{
		VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc(stack);
		
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
		
		for(int i = 0; i < memoryProperties.memoryTypeCount(); i++)
		{
			if((typeFilter & (1 << i)) != 0 && (memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties)
			{
				return i;
			}
		}
		
		throw new Error();
	}
	
	public static class Vertex
	{
		Vector2f pos;
		Vector3f color;
		
		public int byteSize()
		{
			return 0;
		}
		
		public Vertex(Vector2f pos, Vector3f color)
		{
			this.pos = pos;
			this.color = color;
		}
		
		public static VkVertexInputBindingDescription.Buffer getBindingDecription(MemoryStack stack)
		{
			VkVertexInputBindingDescription.Buffer bindingDecription = VkVertexInputBindingDescription.calloc(1, stack);
			bindingDecription.binding(0);
			bindingDecription.stride((int)ValueLayout.JAVA_FLOAT.byteSize() * (2 + 3));
			bindingDecription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
			
			return bindingDecription;
		}
		
		public static VkVertexInputAttributeDescription.Buffer getAttributeDescription(MemoryStack stack)
		{
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2, stack);
			attributeDescriptions.get(0).binding(0);
			attributeDescriptions.get(0).location(0);
			attributeDescriptions.get(0).format(VK_FORMAT_R32G32_SFLOAT);
			attributeDescriptions.get(0).offset(0);
			attributeDescriptions.get(1).binding(0);
			attributeDescriptions.get(1).location(1);
			attributeDescriptions.get(1).format(VK_FORMAT_R32G32B32_SFLOAT);
			attributeDescriptions.get(1).offset((int)ValueLayout.JAVA_FLOAT.byteSize() * 2);
			
			return attributeDescriptions;
		}
	}
	
}
