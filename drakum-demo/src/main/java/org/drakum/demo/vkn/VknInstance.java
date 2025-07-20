package org.drakum.demo.vkn;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK14.*;
import static org.lwjgl.vulkan.KHRGetSurfaceCapabilities2.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

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

public class VknInstance
{
	private VkInstance handle;
	private long debugMessengerHandle;
	
	public VknInstance(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);
			appInfo.sType$Default();
			appInfo.pApplicationName(stack.UTF8(settings.applicationName));
			appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
			appInfo.pEngineName(stack.UTF8(settings.engineName));
			appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
			appInfo.apiVersion(VK_API_VERSION_1_4);

			PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

			List<String> extensions = new ArrayList<>();
			extensions.add(VK_KHR_GET_SURFACE_CAPABILITIES_2_EXTENSION_NAME);
			if(settings.debugMode) extensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
			
			PointerBuffer enabledExtensions = MemoryUtil.memAllocPointer(glfwExtensions.remaining() + extensions.size());
			enabledExtensions.put(glfwExtensions);
			for(String extension : extensions)
			{
				enabledExtensions.put(stack.UTF8(extension));
			}
			
			enabledExtensions.flip();

			PointerBuffer enabledLayers = stack.mallocPointer(1);
			if(settings.debugMode) enabledLayers.put(stack.UTF8("VK_LAYER_KHRONOS_validation"));
			enabledLayers.flip();

			VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = null;
			if(settings.debugMode)
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
			if(settings.debugMode) vkInstanceCreateInfo.pNext(debugCreateInfo);

			this.handle = VknInternalUtils.createInstance(vkInstanceCreateInfo, stack);

			LongBuffer pMessenger = stack.mallocLong(1);

			if(settings.debugMode)
			{
				if (EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(this.handle, debugCreateInfo, null, pMessenger) != VK_SUCCESS)
				{
					throw new RuntimeException("Fehler beim Erstellen des Vulkan Debug Messengers.");
				}
				
				this.debugMessengerHandle = pMessenger.get(0);
			}
		}
	}
	
	public VkInstance handle()
	{
		ensureValid();
		
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.handle != null;
	}
	
	public void close()
	{
		if(this.handle == null) return;
		
		if(this.debugMessengerHandle != 0) EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.handle, this.debugMessengerHandle, null);

		vkDestroyInstance(this.handle, null);
		
		this.handle = null;
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}

	public static class Settings
	{
		private String applicationName;
		private String engineName;
		private boolean debugMode;

		public Settings applicationName(String applicationName)
		{
			this.applicationName = applicationName;
			
			return this;
		}
		
		public String applicationName()
		{
			return this.applicationName;
		}
		
		public Settings engineName(String engineName)
		{
			this.engineName = engineName;
			
			return this;
		}
		
		public String engineName()
		{
			return this.engineName;
		}
		
		public Settings debugMode(boolean debug)
		{
			this.debugMode = debug;
			
			return this;
		}
		
		public Settings debug()
		{
			this.debugMode = true;
			
			return this;
		}
		
		public boolean debugMode()
		{
			return this.debugMode;
		}
		
		
	}

}
