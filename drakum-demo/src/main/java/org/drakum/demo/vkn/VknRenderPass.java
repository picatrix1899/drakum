package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK14.*;

import java.util.ArrayList;
import java.util.List;

import org.drakum.demo.registry.LongId;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class VknRenderPass
{
	private final VknContext context;
	
	private LongId handle;
	
	public VknRenderPass(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VkAttachmentDescription.Buffer attachmentDescriptions = VkAttachmentDescription.calloc(settings.attachments.size(), stack);
			for(int i = 0; i < settings.attachments.size(); i++)
			{
				Attachment attachment = settings.attachments.get(i);
				VkAttachmentDescription attachmentDescription = attachmentDescriptions.get(i);
				
				attachmentDescription.format(attachment.format);
				attachmentDescription.samples(attachment.msSamples);
				attachmentDescription.loadOp(attachment.loadOp);
				attachmentDescription.storeOp(attachment.storeOp);
				attachmentDescription.stencilLoadOp(attachment.stencilLoadOp);
				attachmentDescription.stencilStoreOp(attachment.stencilStoreOp);
				attachmentDescription.initialLayout(attachment.initialLayout);
				attachmentDescription.finalLayout(attachment.finalLayout);
			}

			VkSubpassDescription.Buffer subpassDescriptions = VkSubpassDescription.calloc(settings.subpasses.size(), stack);
			for(int i = 0; i < settings.subpasses.size(); i++)
			{
				Subpass subpass = settings.subpasses.get(i);
				VkSubpassDescription subpassDescription = subpassDescriptions.get(i);
				
				VkAttachmentReference.Buffer colorAttachmentReferences = null;
				if(subpass.colorAttachmentReferences.size() > 0)
				{
					colorAttachmentReferences = VkAttachmentReference.calloc(subpass.colorAttachmentReferences.size(), stack);
					for(int j = 0; j < subpass.colorAttachmentReferences.size(); j++)
					{
						SubpassAttachmentRef subpassAttachmentRef = subpass.colorAttachmentReferences.get(j);
						VkAttachmentReference attachmentReference = colorAttachmentReferences.get(i);
						
						attachmentReference.attachment(subpassAttachmentRef.attachementIndex);
						attachmentReference.layout(subpassAttachmentRef.layout);
					}
				}
				
				VkAttachmentReference depthStencilAttachmentReference = null;
				if(subpass.depthStencilAttachmentReference != null)
				{
					depthStencilAttachmentReference = VkAttachmentReference.calloc(stack);
					depthStencilAttachmentReference.attachment(subpass.depthStencilAttachmentReference.attachementIndex);
					depthStencilAttachmentReference.layout(subpass.depthStencilAttachmentReference.layout);
				}
				
				VkAttachmentReference.Buffer resolveAttachmentReferences = null;
				if(subpass.resolveAttachmentReferences.size() > 0)
				{
					resolveAttachmentReferences = VkAttachmentReference.calloc(subpass.resolveAttachmentReferences.size(), stack);
					for(int j = 0; j < subpass.resolveAttachmentReferences.size(); j++)
					{
						SubpassAttachmentRef subpassAttachmentRef = subpass.resolveAttachmentReferences.get(j);
						VkAttachmentReference attachmentReference = resolveAttachmentReferences.get(i);
						
						attachmentReference.attachment(subpassAttachmentRef.attachementIndex);
						attachmentReference.layout(subpassAttachmentRef.layout);
					}
				}
				
				subpassDescription.pipelineBindPoint(subpass.pipelineBindPoint);
				subpassDescription.colorAttachmentCount(1);
				if(colorAttachmentReferences != null) subpassDescription.pColorAttachments(colorAttachmentReferences);
				if(depthStencilAttachmentReference != null) subpassDescription.pDepthStencilAttachment(depthStencilAttachmentReference);
				if(resolveAttachmentReferences != null) subpassDescription.pResolveAttachments(resolveAttachmentReferences);
				if(subpass.preserveAttachmentReferences.size() > 0) subpassDescription.pPreserveAttachments(stack.ints(subpass.preserveAttachmentReferences.toIntArray()));
			}

			VkSubpassDependency.Buffer subpassDependencies = VkSubpassDependency.calloc(settings.dependencies.size(), stack);
			for(int i = 0; i < settings.dependencies.size(); i++)
			{
				SubpassDependency subpassDependency = settings.dependencies.get(i);
				VkSubpassDependency vkSubpassDependency = subpassDependencies.get(i);
				
				vkSubpassDependency.srcSubpass(subpassDependency.srcSubpass);
				vkSubpassDependency.dstSubpass(subpassDependency.dstSubpass);
				vkSubpassDependency.srcStageMask(subpassDependency.srcStageMask);
				vkSubpassDependency.srcAccessMask(subpassDependency.srcAccessMask);
				vkSubpassDependency.dstStageMask(subpassDependency.dstStageMask);
				vkSubpassDependency.dstAccessMask(subpassDependency.dstAccessMask);
			}
			
			VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack);
			renderPassCreateInfo.sType$Default();
			renderPassCreateInfo.pAttachments(attachmentDescriptions);
			renderPassCreateInfo.pSubpasses(subpassDescriptions);
			renderPassCreateInfo.pDependencies(subpassDependencies);

			this.handle = VknInternalUtils.createRenderPass(this.context.gpu.handle(), renderPassCreateInfo, stack);
		}
	}
	
	public LongId handle()
	{
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.handle.isValid();
	}
	
	public void close()
	{
		vkDestroyRenderPass(this.context.gpu.handle(), this.handle.handle(), null);
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		public final List<Attachment> attachments = new ArrayList<>();
		public final List<Subpass> subpasses = new ArrayList<>();
		public final List<SubpassDependency> dependencies = new ArrayList<>();
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
	}
	
	public static class Attachment
	{
		public int format;
		public int loadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
		public int storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
		public int stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
		public int stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
		public int initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
		public int finalLayout;
		public int msSamples = VK_SAMPLE_COUNT_1_BIT;
	}
	
	public static class Subpass
	{
		public int pipelineBindPoint;
		public final List<SubpassAttachmentRef> colorAttachmentReferences = new ArrayList<>();
		public SubpassAttachmentRef depthStencilAttachmentReference;
		public final IntList preserveAttachmentReferences = new IntArrayList();
		public final List<SubpassAttachmentRef> resolveAttachmentReferences = new ArrayList<>();
		
		public Subpass addColorAttachmentReference(int index, int layout)
		{
			SubpassAttachmentRef reference = new SubpassAttachmentRef();
			reference.attachementIndex = index;
			reference.layout = layout;
			
			this.colorAttachmentReferences.add(reference);
			
			return this;
		}
		
		public Subpass addResolveAttachmentReference(int index, int layout)
		{
			SubpassAttachmentRef reference = new SubpassAttachmentRef();
			reference.attachementIndex = index;
			reference.layout = layout;
			
			this.resolveAttachmentReferences.add(reference);
			
			return this;
		}
		
		public Subpass setDepthStencilAttachmentReference(int index, int layout)
		{
			SubpassAttachmentRef reference = new SubpassAttachmentRef();
			reference.attachementIndex = index;
			reference.layout = layout;
			
			this.depthStencilAttachmentReference = reference;
			
			return this;
		}
	}
	
	public static class SubpassAttachmentRef
	{
		public int attachementIndex;
		public int layout;
	}
	
	public static class SubpassDependency
	{
		public int srcSubpass;
		public int dstSubpass;
		public int srcStageMask;
		public int dstStageMask;
		public int srcAccessMask;
		public int dstAccessMask;
	}
}
