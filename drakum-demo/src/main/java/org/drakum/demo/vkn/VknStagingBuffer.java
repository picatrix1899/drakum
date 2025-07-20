package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

public class VknStagingBuffer
{
	private final VknContext context;
	
	private VknBuffer buffer;
	private ByteBuffer mappedMemoryByte;
	private FloatBuffer mappedMemoryFloat;
	private IntBuffer mappedMemoryInt;
	private MemorySegment memory;
	
	private VkCommandBuffer cmdBuffer;
	private long size;
	
	public VknStagingBuffer(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.size = settings.size;
			
			VknBuffer.Settings stagingBufferCreateSettings = new VknBuffer.Settings(this.context);
			stagingBufferCreateSettings.size(this.size);
			stagingBufferCreateSettings.usageTransferSrc();
			stagingBufferCreateSettings.propertyHostVisible();
			stagingBufferCreateSettings.propertyHostCoherent();
			
			this.buffer = new VknBuffer(stagingBufferCreateSettings);
			
			VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
			commandBufferAllocateInfo.sType$Default();
			commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
			commandBufferAllocateInfo.commandPool(this.context.commandPool.handle());
			commandBufferAllocateInfo.commandBufferCount(1);
			
			this.cmdBuffer = VknInternalUtils.allocateCommandBuffer(this.context.gpu.handle(), commandBufferAllocateInfo, stack);
		}
	}
	
	public void map()
	{
		ensureValid();
		
		this.buffer.map();
		this.memory = MemorySegment.ofAddress(this.buffer.mappedMemoryHandle()).reinterpret(this.size);
	}
	
	public void unmap()
	{
		ensureValid();
		
		this.memory = null;
		this.buffer.unmap();
	}
	
	public MemorySegment memory()
	{
		ensureValid();
		
		return this.memory;
	}
	
	public void mapByte()
	{
		ensureValid();
		
		buffer.map();
		this.mappedMemoryByte = MemoryUtil.memByteBuffer(buffer.mappedMemoryHandle(), (int)this.size);
	}
	
	public void mapFloat()
	{
		ensureValid();
		
		buffer.map();
		this.mappedMemoryFloat = MemoryUtil.memFloatBuffer(buffer.mappedMemoryHandle(), (int)(this.size / ValueLayout.JAVA_FLOAT.byteSize()));
	}
	
	public void mapInt()
	{
		ensureValid();
		
		buffer.map();
		this.mappedMemoryInt = MemoryUtil.memIntBuffer(buffer.mappedMemoryHandle(), (int)(this.size / ValueLayout.JAVA_INT.byteSize()));
	}
	
	public void unmapByte()
	{
		ensureValid();
		
		this.mappedMemoryByte = null;
		buffer.unmap();
	}
	
	public void unmapFloat()
	{
		ensureValid();
		
		this.mappedMemoryFloat = null;
		buffer.unmap();
	}
	
	public void unmapInt()
	{
		ensureValid();
		
		this.mappedMemoryInt = null;
		buffer.unmap();
	}
	
	public void put(byte data)
	{
		ensureValid();
		
		mappedMemoryByte.put(data);
	}
	
	public void put(byte[] data)
	{
		ensureValid();
		
		mappedMemoryByte.put(data);
	}
	
	public void put(float data)
	{
		ensureValid();
		
		mappedMemoryFloat.put(data);
	}
	
	public void put(float[] data)
	{
		ensureValid();
		
		mappedMemoryFloat.put(data);
	}
	
	public void put(int data)
	{
		ensureValid();
		
		mappedMemoryInt.put(data);
	}
	
	public void put(int[] data)
	{
		ensureValid();
		
		mappedMemoryInt.put(data);
	}
	
	public void store(byte[] data)
	{
		ensureValid();
		
		mapByte();
		put(data);
		unmapByte();
	}

	public void store(float[] data)
	{
		ensureValid();
		
		mapFloat();
		put(data);
		unmapFloat();
	}
	
	public void store(int[] data)
	{
		ensureValid();
		
		mapInt();
		put(data);
		unmapInt();
	}
	
	public void transferToImage(long image, int width, int height, int depth, int finalLayout, int finalAccessMask, int finalStageMask)
	{
		ensureValid();
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
			commandBufferBeginInfo.sType$Default();
			commandBufferBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			
			vkBeginCommandBuffer(cmdBuffer, commandBufferBeginInfo);
			
			new VknCmdImageMemoryBarrier(cmdBuffer, image).layout(VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			.accessMask(0, VK_ACCESS_TRANSFER_WRITE_BIT)
			.stageMask(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT)
			.run();
			
			VkBufferImageCopy.Buffer bufferImageCopy = VkBufferImageCopy.calloc(1, stack);
			bufferImageCopy.bufferOffset(0);
			bufferImageCopy.bufferRowLength(0);
			bufferImageCopy.bufferImageHeight(0);
			bufferImageCopy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0).baseArrayLayer(0).layerCount(1);
			bufferImageCopy.imageOffset().set(0, 0, 0);
			bufferImageCopy.imageExtent().set(width, height, depth);
			
			vkCmdCopyBufferToImage(cmdBuffer, buffer.handle(), image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, bufferImageCopy);
			
			new VknCmdImageMemoryBarrier(cmdBuffer, image)
			.layout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, finalLayout)
			.accessMask(VK_ACCESS_TRANSFER_WRITE_BIT, finalAccessMask)
			.stageMask(VK_PIPELINE_STAGE_TRANSFER_BIT, finalStageMask)
			.run();
			
			vkEndCommandBuffer(cmdBuffer);
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType$Default();
			submitInfo.pCommandBuffers(stack.pointers(cmdBuffer));
			
			vkQueueSubmit(this.context.gpu.graphicsQueue(), submitInfo, VK_NULL_HANDLE);
			
			vkQueueWaitIdle(this.context.gpu.graphicsQueue());
		}
	}
	
	public void trasferToBuffer(long buffer)
	{
		ensureValid();
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc(stack);
			commandBufferBeginInfo.sType$Default();
			commandBufferBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			
			vkBeginCommandBuffer(cmdBuffer, commandBufferBeginInfo);
			
			VkBufferCopy.Buffer bufferCopy = VkBufferCopy.calloc(1, stack);
			bufferCopy.srcOffset(0);
			bufferCopy.dstOffset(0);
			bufferCopy.size(size);
			
			vkCmdCopyBuffer(cmdBuffer, this.buffer.handle(), buffer, bufferCopy);
			
			vkEndCommandBuffer(cmdBuffer);
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
			submitInfo.sType$Default();
			submitInfo.pCommandBuffers(stack.pointers(cmdBuffer));
			
			vkQueueSubmit(this.context.gpu.graphicsQueue(), submitInfo, VK_NULL_HANDLE);
			
			vkQueueWaitIdle(this.context.gpu.graphicsQueue());
		}
	}
	
	public boolean isValid()
	{
		return this.buffer.isValid();
	}
	
	public void close()
	{
		if(!this.buffer.isValid()) return;
		
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			vkFreeCommandBuffers(this.context.gpu.handle(), this.context.commandPool.handle(), stack.pointers(cmdBuffer));
			
			buffer.close();
		}
	}
	
	private void ensureValid()
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private long size;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings size(long size)
		{
			this.size = size;
			
			return this;
		}
		
		public long size()
		{
			return this.size;
		}
	}
}
