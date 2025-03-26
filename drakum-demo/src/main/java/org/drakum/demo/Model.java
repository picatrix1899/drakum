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
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
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
	
	public void createVertexBuffer(VkDevice device, VkPhysicalDevice physicalDevice, long commandPool, VkQueue graphicsQueue, MemoryStack stack)
	{
		VulkanBuffer stagingBuffer = createBuffer(device, physicalDevice, Vertex.byteSize() * vertices.length, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stack);
		
		long mappedMemoryAddress = Utils.mapMemory(device, stagingBuffer.bufferMemory, 0, Vertex.byteSize() * vertices.length, 0, stack);
		
		FloatBuffer floatMappedMemory = MemoryUtil.memFloatBuffer(mappedMemoryAddress, Vertex.floatSize() * vertices.length);
		
		for(Vertex v : vertices)
		{
			floatMappedMemory.put(v.pos.x);
			floatMappedMemory.put(v.pos.y);
			floatMappedMemory.put(v.color.x);
			floatMappedMemory.put(v.color.y);
			floatMappedMemory.put(v.color.z);
		}
		
		vkUnmapMemory(device, stagingBuffer.bufferMemory);

		vertexBuffer = createBuffer(device, physicalDevice, Vertex.byteSize() * vertices.length, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, stack);

		copyBuffer(device, commandPool, stagingBuffer.buffer, vertexBuffer.buffer, Vertex.byteSize() * vertices.length, graphicsQueue, stack);
		
		stagingBuffer.__release(device);
	}
	
	public void copyBuffer(VkDevice device, long commandPool, long srcBuffer, long dstBuffer, int size, VkQueue graphicsQueue, MemoryStack stack)
	{
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandPool(commandPool);
		commandBufferAllocateInfo.commandBufferCount(1);
		
		VkCommandBuffer cmdBuffer = Utils.allocateCommandBuffer(device, commandBufferAllocateInfo, stack);
		
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
		
		vkFreeCommandBuffers(device, commandPool, stack.pointers(cmdBuffer));
	}
	
	public VulkanBuffer createBuffer(VkDevice device, VkPhysicalDevice physicalDevice, long size, int usage, int properties, MemoryStack stack)
	{
		VulkanBuffer buffer = new VulkanBuffer();
		buffer.createBuffer(device, physicalDevice, size, usage, VK_SHARING_MODE_EXCLUSIVE, properties);
		
		return buffer;
	}
	
	public void createIndexBuffer(VkDevice device, VkPhysicalDevice physicalDevice, long commandPool, VkQueue graphicsQueue, MemoryStack stack)
	{
		VulkanBuffer stagingBuffer = createBuffer(device, physicalDevice, 4 * indices.length, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stack);
		
		long mappedMemoryAddress = Utils.mapMemory(device, stagingBuffer.bufferMemory, 0, 4 * indices.length, 0, stack);

		IntBuffer intMappedMemory = MemoryUtil.memIntBuffer(mappedMemoryAddress, indices.length);
		intMappedMemory.put(indices);
		
		vkUnmapMemory(device, stagingBuffer.bufferMemory);

		indexBuffer = createBuffer(device, physicalDevice, 4 * indices.length, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, stack);

		copyBuffer(device, commandPool, stagingBuffer.buffer, indexBuffer.buffer, 4 * indices.length, graphicsQueue, stack);
		
		stagingBuffer.__release(device);
	}
	
	public void __release(VkDevice device)
	{
		indexBuffer.__release(device);
		
		vertexBuffer.__release(device);
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
