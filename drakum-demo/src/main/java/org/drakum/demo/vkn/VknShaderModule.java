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
	private final VknContext context;
	
	public long handle;
	
	public VknShaderModule(VknContext context, String path)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = context;
			
			ByteBuffer vertexShaderData = readFile(path, stack);
			
			VkShaderModuleCreateInfo vertexShaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc(stack);
			vertexShaderModuleCreateInfo.sType$Default();
			vertexShaderModuleCreateInfo.pCode(vertexShaderData);

			this.handle = VknInternalUtils.createShaderModule(this.context.gpu.handle(), vertexShaderModuleCreateInfo, stack);
		}
	}
	
	public void close()
	{
		vkDestroyShaderModule(this.context.gpu.handle(), handle, null);
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
	
}
