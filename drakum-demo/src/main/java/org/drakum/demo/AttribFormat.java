package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class AttribFormat
{
	protected final List<Attrib> attribs = new ArrayList<>();
	protected final Type type;
	protected final Packing packing;
	private int stride;
	protected MemoryLayout memLayout;
	
	protected AttribFormat(Type type, Packing packing)
	{
		this.type = type;
		this.packing = packing;
	}
	
	public int id()
	{
		return 0;
	}
	
	protected void addAttrib(Format format)
	{
		Attrib attrib = new Attrib();
		attrib.format = format;
		
		this.attribs.add(attrib);
	}
	
	protected void compile()
	{
		calculatePadding();
		generateMemoryLayout();
	}
	
	public int stride()
	{
		return this.stride;
	}
	
	private void calculatePadding()
	{
		int offset = 0;
		
		Attrib lastAttrib = null;
		
		int padding = 0;
		
		
		
		for(int i = 0; i < this.attribs.size(); i++)
		{
			Attrib attrib = this.attribs.get(i);
			
			int formatBytes = attrib.format.bytesi();

			padding = 0;
			if(this.packing == Packing.MINIMAL)
			{
				int rest = 16 - (offset % 16);
				if(rest < formatBytes)
				{
					padding = rest;
				}
			}
			
			offset += padding;

			attrib.offset = offset;
			
			offset += formatBytes;
			
			if(lastAttrib != null) lastAttrib.padding = padding;
			
			lastAttrib = attrib;
		}
		
		padding = 0;
		if(this.packing == Packing.TAIL || this.packing == Packing.MINIMAL)
		{
			int rest = 16 - (offset % 16);
			if(rest < 16)
			{
				padding = rest;
			}
		}
		
		offset += padding;
		
		this.stride = offset;
		
		lastAttrib.padding = padding;
	}
	
	private void generateMemoryLayout()
	{
		List<MemoryLayout> entries = new ArrayList<>(this.attribs.size() * 2);
		
		int index = 0;
		for(int i = 0; i < this.attribs.size(); i++)
		{
			Attrib attrib = this.attribs.get(i);
			
			entries.add(attrib.format.memLayout());
			
			attrib.segmentIndex = index;
			
			index++;
			
			if(attrib.padding > 0)
			{
				entries.add(MemoryLayout.paddingLayout(attrib.padding));
				
				index++;
			}
		}
		
		MemoryLayout[] layoutEntries = entries.toArray(new MemoryLayout[entries.size()]);
		
		this.memLayout = MemoryLayout.structLayout(layoutEntries);
	}
	
	protected SegmentHandle getVarHandle(int index)
	{
		VarHandle handle = this.memLayout.varHandle(PathElement.groupElement((long)this.attribs.get(index).segmentIndex), PathElement.sequenceElement());
		
		return new SegmentHandle(handle);
	}
	
	public MemorySegment slice(MemorySegment seg, long index)
	{
		return seg.asSlice(index * this.stride, this.stride);
	}
	
	public VkVertexInputAttributeDescription.Buffer genAttribDescription(int binding)
	{
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(this.attribs.size());
		int location = 0;
		for(int i = 0, attribsSize = this.attribs.size(); i < attribsSize; i++)
		{
			Attrib attrib = this.attribs.get(i);
			
			VkVertexInputAttributeDescription desc = attributeDescriptions.get(i);
			desc.binding(binding);
			desc.format(attrib.format.format);
			desc.location(location);
			desc.offset(attrib.offset);

			location++;
		}
		
		return attributeDescriptions;
	}
	
	public VkVertexInputAttributeDescription.Buffer genAttribDescription(int binding, MemoryStack stack)
	{
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(this.attribs.size(), stack);
		int location = 0;
		for(int i = 0, attribsSize = this.attribs.size(); i < attribsSize; i++)
		{
			Attrib attrib = this.attribs.get(i);
			
			VkVertexInputAttributeDescription desc = attributeDescriptions.get(i);
			desc.binding(binding);
			desc.format(attrib.format.format);
			desc.location(location);
			desc.offset(attrib.offset);

			location++;
		}
		
		return attributeDescriptions;
	}
	
	public VkVertexInputBindingDescription.Buffer genBindingDescription(int binding)
	{
		VkVertexInputBindingDescription.Buffer bindingDecription = VkVertexInputBindingDescription.calloc(1);
		bindingDecription.binding(binding);
		bindingDecription.stride(this.stride);
		bindingDecription.inputRate(this.type.rate());
		
		return bindingDecription;
	}
	
	public VkVertexInputBindingDescription.Buffer genBindingDescription(int binding, MemoryStack stack)
	{
		VkVertexInputBindingDescription.Buffer bindingDecription = VkVertexInputBindingDescription.calloc(1, stack);
		bindingDecription.binding(binding);
		bindingDecription.stride(this.stride);
		bindingDecription.inputRate(this.type.rate());
		
		return bindingDecription;
	}
	
	public static class SegmentHandle
	{
		private final VarHandle handle;
		
		private SegmentHandle(VarHandle handle)
		{
			this.handle = handle;
		}
		
		public void setFloat(MemorySegment segment, float v)
		{
			this.handle.set(segment, 0l, v);
		}
		
		public void setInt(MemorySegment segment, int v)
		{
			this.handle.set(segment, 0l, v);
		}
		
		public void setTup2F(MemorySegment segment, float v0, float v1)
		{
			this.handle.set(segment, 0l, 0, v0);
			this.handle.set(segment, 0l, 1, v1);
		}
		
		public void setTup3F(MemorySegment segment, float v0, float v1, float v2)
		{
			this.handle.set(segment, 0l, 0, v0);
			this.handle.set(segment, 0l, 1, v1);
			this.handle.set(segment, 0l, 2, v2);
		}
		
		public void setTup4F(MemorySegment segment, float v0, float v1, float v2, float v3)
		{
			this.handle.set(segment, 0l, 0, v0);
			this.handle.set(segment, 0l, 1, v1);
			this.handle.set(segment, 0l, 2, v2);
			this.handle.set(segment, 0l, 3, v3);
		}
	}
	
	public static class Attrib
	{
		public int segmentIndex = 0;
		public int offset = 0;
		public Format format;
		public int padding = 0;
	}
	
	public static enum Format
	{
		UINT32(VK_FORMAT_R32_UINT, 4, ValueLayout.JAVA_INT_UNALIGNED),
		INT32(VK_FORMAT_R32_SINT, 4, ValueLayout.JAVA_INT),
		FLOAT32(VK_FORMAT_R32_SFLOAT, 4, ValueLayout.JAVA_FLOAT),
		
		VEC2_FLOAT32(VK_FORMAT_R32G32_SFLOAT, 8, MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT)),
		VEC3_FLOAT32(VK_FORMAT_R32G32B32_SFLOAT, 12, MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT)),
		VEC4_FLOAT32(VK_FORMAT_R32G32B32A32_SFLOAT, 16, MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_FLOAT)),
		;
		
		private final int format;
		private final int bytes;
		private final MemoryLayout memLayout;
		
		private Format(int format, int bytes, MemoryLayout memLayout)
		{
			this.format = format;
			this.bytes = bytes;
			this.memLayout = memLayout;
		}
		
		public int format()
		{
			return this.format;
		}
		
		public int bytesi()
		{
			return this.bytes;
		}
		
		public MemoryLayout memLayout()
		{
			return this.memLayout;
		}
	}
	
	public static enum Type
	{
		VERTEX(VK_VERTEX_INPUT_RATE_VERTEX),
		INSTANCE(VK_VERTEX_INPUT_RATE_INSTANCE)
		;
		
		private final int rate;
		
		private Type(int rate)
		{
			this.rate = rate;
		}
		
		public int rate()
		{
			return this.rate;
		}
	}
	
	public static enum Packing
	{
		NONE,
		MINIMAL,
		TAIL,
		;
	}
}
