package org.drakum.demo;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK14.*;

import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

public class VulkanInstance
{
	private VkInstance handle;
	private long debugMessengerHandle;
	
	public VulkanInstance()
	{
		
	}
	
	public VkInstance handle()
	{
		return this.handle;
	}
	
	public static class Builder
	{
		private String applicationName;
		private String engineName;
		private boolean debugMode;
		
		public Builder applicationName(String name)
		{
			this.applicationName = name;
			
			return this;
		}
		
		public Builder engineName(String name)
		{
			this.engineName = name;
			
			return this;
		}
		
		public Builder debugMode(boolean enabled)
		{
			this.debugMode = enabled;
			
			return this;
		}
		
		public VulkanInstance create()
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);
				appInfo.sType$Default();
				appInfo.pApplicationName(stack.UTF8(applicationName));
				appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
				appInfo.pEngineName(stack.UTF8(engineName));
				appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
				appInfo.apiVersion(VK_API_VERSION_1_4);

				PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

				PointerBuffer enabledExtensions = MemoryUtil.memAllocPointer(glfwExtensions.remaining() + 1);
				enabledExtensions.put(glfwExtensions);
				if(this.debugMode) enabledExtensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
				enabledExtensions.flip();

				PointerBuffer enabledLayers = stack.mallocPointer(1);
				if(this.debugMode) enabledLayers.put(stack.UTF8("VK_LAYER_KHRONOS_validation"));
				enabledLayers.flip();

				VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = null;
				if(this.debugMode)
				{
					VkDebugUtilsMessengerCallbackEXT debugCallback = new VkDebugUtilsMessengerCallbackEXT() {
						
						@Override
						public int invoke(int messageSeverity, int messageType, long pCallbackData, long pUserData)
						{
							VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
							
							System.err.println("Vulkan Debug [" + messageSeverity + "]: " + callbackData.pMessageString());
							
							return VK_FALSE; // Nicht Vulkan beenden
						}
						
					};
					
					debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
					debugCreateInfo.sType$Default();
					debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
					debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
					debugCreateInfo.pfnUserCallback(debugCallback);
				}
				

				VkInstanceCreateInfo vkInstanceCreateInfo = VkInstanceCreateInfo.calloc(stack);
				vkInstanceCreateInfo.sType$Default();
				vkInstanceCreateInfo.pApplicationInfo(appInfo);
				vkInstanceCreateInfo.ppEnabledExtensionNames(enabledExtensions);
				vkInstanceCreateInfo.ppEnabledLayerNames(enabledLayers);
				if(this.debugMode) vkInstanceCreateInfo.pNext(debugCreateInfo);

				VkInstance handle = Utils.createInstance(vkInstanceCreateInfo, stack);

				LongBuffer pMessenger = stack.mallocLong(1);

				long debugMessengerHandle = -1;
				if(this.debugMode)
				{
					if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(handle, debugCreateInfo, null, pMessenger) != VK_SUCCESS)
					{
						throw new RuntimeException("Fehler beim Erstellen des Vulkan Debug Messengers.");
					}
					
					debugMessengerHandle = pMessenger.get(0);
				}
				
				
				
				VulkanInstance result = new VulkanInstance();
				result.handle = handle;
				result.debugMessengerHandle = debugMessengerHandle;
				
				
				return result;
			}
		}
	}
	
	
	
	public void __release()
	{
		if(this.debugMessengerHandle > -1) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(handle, debugMessengerHandle, null);

		vkDestroyInstance(handle, null);
	}
}
