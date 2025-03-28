package org.drakum.demo;

import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public class ShaderModule
{
	public long handle;
	
	public ShaderModule()
	{
		
	}
	
	public void __release(GPU gpu)
	{
		vkDestroyShaderModule(gpu.device, handle, null);
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
		public ShaderModule create(GPU gpu, String path)
		{
			try(MemoryStack stack = MemoryStack.stackPush())
			{
				ByteBuffer vertexShaderData = readFile(path, stack);
				
				VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
				vertexShaderModuleCreateInfo.sType$Default();
				vertexShaderModuleCreateInfo.pCode(vertexShaderData);

				long shaderModule = Utils.createShaderModule(gpu.device, vertexShaderModuleCreateInfo, stack);
				
				ShaderModule result = new ShaderModule();
				result.handle = shaderModule;
				
				return result;
			}
		}
	}
	
	
}
