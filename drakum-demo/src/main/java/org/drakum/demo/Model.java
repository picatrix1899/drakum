package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.foreign.ValueLayout;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknBuffer;
import org.drakum.demo.vkn.VknInternalUtils;
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
			new Vertex(new Vector3f(-1f, -1f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(0, 0)),	
			new Vertex(new Vector3f(1f, -1f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(1, 0)),	
			new Vertex(new Vector3f(1f, 1f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(1, 1)),	
			new Vertex(new Vector3f(-1f, 1f, 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector2f(0, 1)),	
		};
	
	public int[] indices = new int[] {0, 1, 2, 2, 3, 0};
	
	public VknBuffer vertexBuffer;
	public VknBuffer indexBuffer;
	
	public void createVertexBuffer(MemoryStack stack)
	{
		VknBuffer.Settings stagingBufferCreateSettings = new VknBuffer.Settings();
		stagingBufferCreateSettings.size = Vertex.byteSize() * vertices.length;
		stagingBufferCreateSettings.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
		stagingBufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		stagingBufferCreateSettings.properties = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
		
		VknBuffer stagingBuffer = new VknBuffer(stagingBufferCreateSettings);
				
		stagingBuffer.map();
		
		FloatBuffer floatMappedMemory = MemoryUtil.memFloatBuffer(stagingBuffer.mappedMemoryHandle(), Vertex.floatSize() * vertices.length);
		
		for(Vertex v : vertices)
		{
			floatMappedMemory.put(v.pos.x);
			floatMappedMemory.put(v.pos.y);
			floatMappedMemory.put(v.pos.z);
			floatMappedMemory.put(v.color.x);
			floatMappedMemory.put(v.color.y);
			floatMappedMemory.put(v.color.z);
			floatMappedMemory.put(v.texCoord.x);
			floatMappedMemory.put(v.texCoord.y);
		}
		
		stagingBuffer.unmap();

		VknBuffer.Settings vertexBufferCreateSettings = new VknBuffer.Settings();
		vertexBufferCreateSettings.size = Vertex.byteSize() * vertices.length;
		vertexBufferCreateSettings.usage = VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
		vertexBufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		vertexBufferCreateSettings.properties = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
		
		vertexBuffer = new VknBuffer(vertexBufferCreateSettings);

		copyBuffer(stagingBuffer.handle(), vertexBuffer.handle(), Vertex.byteSize() * vertices.length, stack);
		
		stagingBuffer.close();
	}
	
	public void copyBuffer(long srcBuffer, long dstBuffer, int size, MemoryStack stack)
	{
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack);
		commandBufferAllocateInfo.sType$Default();
		commandBufferAllocateInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
		commandBufferAllocateInfo.commandPool(CommonRenderContext.commandPool);
		commandBufferAllocateInfo.commandBufferCount(1);
		
		VkCommandBuffer cmdBuffer = VknInternalUtils.allocateCommandBuffer(CommonRenderContext.gpu.handle(), commandBufferAllocateInfo, stack);
		
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
		
		vkQueueSubmit(CommonRenderContext.gpu.graphicsQueue(), submitInfo, VK_NULL_HANDLE);
		
		vkQueueWaitIdle(CommonRenderContext.gpu.graphicsQueue());
		
		vkFreeCommandBuffers(CommonRenderContext.gpu.handle(), CommonRenderContext.commandPool, stack.pointers(cmdBuffer));
	}

	public void createIndexBuffer(MemoryStack stack)
	{
		VknBuffer.Settings stagingBufferCreateSettings = new VknBuffer.Settings();
		stagingBufferCreateSettings.size = 4 * indices.length;
		stagingBufferCreateSettings.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
		stagingBufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		stagingBufferCreateSettings.properties = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
		
		VknBuffer stagingBuffer = new VknBuffer(stagingBufferCreateSettings);

		stagingBuffer.map();

		IntBuffer intMappedMemory = MemoryUtil.memIntBuffer(stagingBuffer.mappedMemoryHandle(), indices.length);
		intMappedMemory.put(indices);
		
		stagingBuffer.unmap();

		VknBuffer.Settings indexBufferCreateSettings = new VknBuffer.Settings();
		indexBufferCreateSettings.size = 4 * indices.length;
		indexBufferCreateSettings.usage = VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
		indexBufferCreateSettings.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
		indexBufferCreateSettings.properties = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
		
		indexBuffer = new VknBuffer(indexBufferCreateSettings);

		copyBuffer(stagingBuffer.handle(), indexBuffer.handle(), 4 * indices.length, stack);
		
		stagingBuffer.close();
	}
	
	public void __release()
	{
		indexBuffer.close();
		vertexBuffer.close();
	}
	
	public static class Vertex
	{
		public Vector3f pos;
		public Vector3f color;
		public Vector2f texCoord;
		
		public static int byteSize()
		{
			return (int)ValueLayout.JAVA_FLOAT.byteSize() * floatSize();
		}
		
		public static int floatSize()
		{
			return 3 + 3 + 2;
		}
		
		public Vertex(Vector3f pos, Vector3f color, Vector2f texCoord)
		{
			this.pos = pos;
			this.color = color;
			this.texCoord = texCoord;
		}
		
		public static VkVertexInputBindingDescription.Buffer getBindingDecription(MemoryStack stack)
		{
			VkVertexInputBindingDescription.Buffer bindingDecription = VkVertexInputBindingDescription.calloc(1, stack);
			bindingDecription.binding(0);
			bindingDecription.stride((int)ValueLayout.JAVA_FLOAT.byteSize() * (3 + 3 + 2));
			bindingDecription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
			
			return bindingDecription;
		}
		
		public static VkVertexInputAttributeDescription.Buffer getAttributeDescription(MemoryStack stack)
		{
			VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(3, stack);
			attributeDescriptions.get(0).binding(0);
			attributeDescriptions.get(0).location(0);
			attributeDescriptions.get(0).format(VK_FORMAT_R32G32B32_SFLOAT);
			attributeDescriptions.get(0).offset(0);
			attributeDescriptions.get(1).binding(0);
			attributeDescriptions.get(1).location(1);
			attributeDescriptions.get(1).format(VK_FORMAT_R32G32B32_SFLOAT);
			attributeDescriptions.get(1).offset((int)ValueLayout.JAVA_FLOAT.byteSize() * 3);
			attributeDescriptions.get(2).binding(0);
			attributeDescriptions.get(2).location(2);
			attributeDescriptions.get(2).format(VK_FORMAT_R32G32_SFLOAT);
			attributeDescriptions.get(2).offset((int)ValueLayout.JAVA_FLOAT.byteSize() * (3 + 3));
			
			return attributeDescriptions;
		}
	}
}
