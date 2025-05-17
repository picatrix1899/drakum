package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.ref.Cleaner.Cleanable;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

public class VknBuffer
{
	private long handle;
	private VknMemory memory;
	private final Cleanable cleanable;
	
	public VknBuffer(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc(stack);
			bufferCreateInfo.sType$Default();
			bufferCreateInfo.size(settings.size);
			bufferCreateInfo.usage(settings.usage);
			bufferCreateInfo.sharingMode(settings.sharingMode);
			
			long buffer = VknInternalUtils.createBuffer(CommonRenderContext.gpu.handle(), bufferCreateInfo, stack);
			
			VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc(stack);
			
			vkGetBufferMemoryRequirements(CommonRenderContext.gpu.handle(), buffer, memoryRequirements);
			
			VknMemory.Settings memoryCreateSettings = new VknMemory.Settings();
			memoryCreateSettings.size = memoryRequirements.size();
			memoryCreateSettings.memoryTypeBits = memoryRequirements.memoryTypeBits();
			memoryCreateSettings.properties = settings.properties;
			
			VknMemory memory = new VknMemory(memoryCreateSettings);
			
			vkBindBufferMemory(CommonRenderContext.gpu.handle(), buffer, memory.handle(), 0);
			
			this.handle = buffer;
			this.memory = memory;

			this.cleanable = VknCleanerUtils.CLEANER.register(this, () -> {
				vkDestroyBuffer(CommonRenderContext.gpu.handle(), handle, null);
				this.memory.close();
			});
		}
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public long memoryHandle()
	{
		return this.memory.handle();
	}
	
	public long mappedMemoryHandle()
	{
		return this.memory.mappedHandle();
	}
	
	public boolean isMapped()
	{
		return this.memory.isMapped();
	}
	
	public void map()
	{
		this.memory.map();
	}
	
	public void unmap()
	{
		this.memory.unmap();
	}
	
	public void close()
	{
		this.cleanable.clean();
	}

	public static class Settings
	{
		public VknWindow window;
		public long size;
		public int usage;
		public int sharingMode;
		public int properties;
	}
}
