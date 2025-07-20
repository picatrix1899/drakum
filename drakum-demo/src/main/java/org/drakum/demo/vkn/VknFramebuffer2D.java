package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.barghos.util.container.ints.Extent2I;
import org.drakum.demo.VknObjectType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

public class VknFramebuffer2D
{
	private final VknContext context;
	
	private long handle = VK_NULL_HANDLE;
	
	private int width;
	private int height;
	
	public VknFramebuffer2D(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			this.width = settings.width;
			this.height = settings.height;
			
			int attachmentCount = settings.attachments.size();
			LongBuffer attachmentBuffer = stack.callocLong(attachmentCount);
			for(int i = 0; i < attachmentCount; i++)
			{
				attachmentBuffer.put(i, settings.attachments.get(i).handle().handle());
			}
			
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
			framebufferCreateInfo.sType$Default();
			framebufferCreateInfo.renderPass(settings.renderPass.handle().handle());
			framebufferCreateInfo.attachmentCount(attachmentCount);
			framebufferCreateInfo.pAttachments(attachmentBuffer);
			framebufferCreateInfo.width(settings.width);
			framebufferCreateInfo.height(settings.height);
			framebufferCreateInfo.layers(1);

			this.handle = VknInternalUtils.createFramebuffer(this.context.gpu.handle(), framebufferCreateInfo, stack);
		}
	}
	
	public long handle()
	{
		ensureValid(null);
		
		return this.handle;
	}
	
	public int width()
	{
		ensureValid(null);
		
		return this.width;
	}
	
	public int height()
	{
		ensureValid(null);
		
		return this.height;
	}
	
	public boolean isValid()
	{
		return this.handle != VK_NULL_HANDLE;
	}
	
	public void close()
	{
		if(this.handle == VK_NULL_HANDLE) return;
		
		vkDestroyFramebuffer(this.context.gpu.handle(), this.handle, null);
		
		this.handle = VK_NULL_HANDLE;
	}
	
	private void ensureValid(VknObjectType type)
	{
		if(VknContext.OBJECT_VALIDATION && !isValid()) throw new RuntimeException("Fence object already closed.");
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		private int width;
		private int height;
		private List<VknImageView2D> attachments = new ArrayList<>();
		private VknRenderPass renderPass;
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
		
		public Settings size(int width, int height)
		{
			this.width = width;
			this.height = height;
			
			return this;
		}
		
		public Settings size(Extent2I extent)
		{
			this.width = extent.width();
			this.height = extent.height();
			
			return this;
		}
		
		public Settings width(int width)
		{
			this.width = width;
			
			return this;
		}
		
		public int width()
		{
			return this.width;
		}
		
		public Settings height(int height)
		{
			this.height = height;
			
			return this;
		}
		
		public int height()
		{
			return this.height;
		}
		
		public Settings addAttachment(VknImageView2D view)
		{
			this.attachments.add(view);
			
			return this;
		}
		
		public List<VknImageView2D> attachments()
		{
			return this.attachments;
		}
		
		public Settings renderPass(VknRenderPass renderPass)
		{
			this.renderPass = renderPass;
			
			return this;
		}
		
		public VknRenderPass renderPass()
		{
			return this.renderPass;
		}
	}
}
