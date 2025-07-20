package org.drakum.demo;

import java.lang.foreign.MemorySegment;

import org.barghos.util.CoreType;
import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknBuffer;
import org.drakum.demo.vkn.VknStagingBuffer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public class Model
{
	public VknBuffer vertexBuffer;
	public VknBuffer indexBuffer;
	public int indicesCount;
	
	public Model(Vertex[] vertices, int[] indices)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			createVertexBuffer(vertices, stack);
			createIndexBuffer(indices, stack);
			this.indicesCount = indices.length;
		}
	}

	public AttribFormat attribFormat()
	{
		return AttribFormats.POS3_COL3_TEX2; 
	}
	
	private void createVertexBuffer(Vertex[] vertices, MemoryStack stack)
	{
		VknBuffer.Settings vertexBufferCreateSettings = new VknBuffer.Settings(CommonRenderContext.context);
		vertexBufferCreateSettings.size(Vertex.byteSize() * vertices.length);
		vertexBufferCreateSettings.usageTransferDst();
		vertexBufferCreateSettings.usageVertexBuffer();
		vertexBufferCreateSettings.propertyDeviceLocal();
		
		vertexBuffer = new VknBuffer(vertexBufferCreateSettings);
		
		VknStagingBuffer stagingBuffer = new VknStagingBuffer(new VknStagingBuffer.Settings(CommonRenderContext.context).size(Vertex.byteSize() * vertices.length));
		
		stagingBuffer.map();
		
		MemorySegment seg = stagingBuffer.memory();
		
		for(int i = 0; i < vertices.length; i++)
		{
			Vertex vertex = vertices[i];
			
			MemorySegment slice = AttribFormats.POS3_COL3_TEX2.slice(seg, i);

			AttribFormats.POS3_COL3_TEX2.setPos(slice, vertex.pos.x, vertex.pos.y, vertex.pos.z);
			AttribFormats.POS3_COL3_TEX2.setCol(slice, vertex.color.x, vertex.color.y, vertex.color.z);
			AttribFormats.POS3_COL3_TEX2.setTex(slice, vertex.texCoord.x, vertex.texCoord.y);
		}
				
		stagingBuffer.unmap();

		stagingBuffer.trasferToBuffer(this.vertexBuffer.handle());
		stagingBuffer.close();
	}

	private void createIndexBuffer(int[] indices, MemoryStack stack)
	{
		VknBuffer.Settings indexBufferCreateSettings = new VknBuffer.Settings(CommonRenderContext.context);
		indexBufferCreateSettings.size(CoreType.INT.bytesi(indices.length));
		indexBufferCreateSettings.usageTransferDst();
		indexBufferCreateSettings.usageIndexBuffer();
		indexBufferCreateSettings.propertyDeviceLocal();
		
		indexBuffer = new VknBuffer(indexBufferCreateSettings);

		VknStagingBuffer stagingBuffer = new VknStagingBuffer(new VknStagingBuffer.Settings(CommonRenderContext.context).size(4 * indices.length));
		stagingBuffer.store(indices);
		stagingBuffer.trasferToBuffer(indexBuffer.handle());
		stagingBuffer.close();
	}
	
	public void close()
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
			return AttribFormats.POS3_COL3_TEX2.stride();
		}
		
		public Vertex(Vector3f pos, Vector3f color, Vector2f texCoord)
		{
			this.pos = pos;
			this.color = color;
			this.texCoord = texCoord;
		}
	}
}
