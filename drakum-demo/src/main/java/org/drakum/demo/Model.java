package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class Model
{
	public Vertex[] vertices = new Vertex[] {
			new Vertex(new Vector2f(-0.5f, -0.5f), new Vector3f(1.0f, 0.0f, 0.0f)),	
			new Vertex(new Vector2f(0.5f, -0.5f), new Vector3f(0.0f, 1.0f, 0.0f)),	
			new Vertex(new Vector2f(0.5f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f)),	
			new Vertex(new Vector2f(-0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f)),	
		};
	
	public int[] indices = new int[] {0, 1, 2, 2, 3, 0};
	
	public VulkanBuffer vertexBuffer;
	public VulkanBuffer indexBuffer;
	
	public void createVertexBuffer(MemoryStack stack)
	{
		VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
			.size(Vertex.byteSize() * vertices.length)
			.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.properties(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
			.create();
				
		stagingBuffer.map();
		
		FloatBuffer floatMappedMemory = MemoryUtil.memFloatBuffer(stagingBuffer.mappedMemoryHandle(), Vertex.floatSize() * vertices.length);
		
		for(Vertex v : vertices)
		{
			floatMappedMemory.put(v.pos.x);
			floatMappedMemory.put(v.pos.y);
			floatMappedMemory.put(v.color.x);
			floatMappedMemory.put(v.color.y);
			floatMappedMemory.put(v.color.z);
		}
		
		stagingBuffer.unmap();

		vertexBuffer = new VulkanBuffer.Builder()
			.size(Vertex.byteSize() * vertices.length)
			.usage(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.properties(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
			.create();

		copyBuffer(stagingBuffer.handle(), vertexBuffer.handle(), Vertex.byteSize() * vertices.length, stack);
		
		stagingBuffer.__release();
	}
	
	public void copyBuffer(long srcBuffer, long dstBuffer, int size, MemoryStack stack)
	{
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandPool(CommonRenderContext.instance().commandPool);
		commandBufferAllocateInfo.commandBufferCount(1);
		
		VkCommandBuffer cmdBuffer = Utils.allocateCommandBuffer(CommonRenderContext.instance().gpu.device, commandBufferAllocateInfo, stack);
		
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
		
		vkQueueSubmit(CommonRenderContext.instance().gpu.graphicsQueue, submitInfo, VK_NULL_HANDLE);
		
		vkQueueWaitIdle(CommonRenderContext.instance().gpu.graphicsQueue);
		
		vkFreeCommandBuffers(CommonRenderContext.instance().gpu.device, CommonRenderContext.instance().commandPool, stack.pointers(cmdBuffer));
	}

	public void createIndexBuffer(MemoryStack stack)
	{
		VulkanBuffer stagingBuffer = new VulkanBuffer.Builder()
			.size(4 * indices.length)
			.usage(VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.properties(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
			.create();

		stagingBuffer.map();

		IntBuffer intMappedMemory = MemoryUtil.memIntBuffer(stagingBuffer.mappedMemoryHandle(), indices.length);
		intMappedMemory.put(indices);
		
		stagingBuffer.unmap();

		indexBuffer = new VulkanBuffer.Builder()
			.size(4 * indices.length)
			.usage(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
			.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
			.properties(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
			.create();

		copyBuffer(stagingBuffer.handle(), indexBuffer.handle(), 4 * indices.length, stack);
		
		stagingBuffer.__release();
	}
	
	public void __release()
	{
		indexBuffer.__release();
		vertexBuffer.__release();
	}
	
	public static class Vertex
	{
		public Vector2f pos;
		public Vector3f color;
		
		public static int byteSize()
		{
			return (int)ValueLayout.JAVA_FLOAT.byteSize() * floatSize();
		}
		
		public static int floatSize()
		{
			return 2 + 3;
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
