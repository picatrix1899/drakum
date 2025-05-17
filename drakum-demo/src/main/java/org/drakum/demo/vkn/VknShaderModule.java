package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.drakum.demo.Engine;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public class VknShaderModule
{
	public long handle;
	
	public VknShaderModule()
	{
		
	}
	
	public void __release()
	{
		vkDestroyShaderModule(CommonRenderContext.gpu.handle(), handle, null);
	}
	
	private static ByteBuffer readFile(String file, MemoryStack stack)
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
	
	public static class Builder
	{
		public VknShaderModule create(String path)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				ByteBuffer vertexShaderData = readFile(path, stack);
				
				VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
				vertexShaderModuleCreateInfo.sType$Default();
				vertexShaderModuleCreateInfo.pCode(vertexShaderData);

				long shaderModule = VknInternalUtils.createShaderModule(CommonRenderContext.gpu.handle(), vertexShaderModuleCreateInfo, stack);
				
				VknShaderModule result = new VknShaderModule();
				result.handle = shaderModule;
				
				return result;
			}
		}
	}
	
	
}
