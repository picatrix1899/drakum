package org.drakum.demo.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drakum.demo.AttribFormat;
import org.drakum.demo.Material;
import org.drakum.demo.MaterialType;
import org.drakum.demo.Model;
import org.drakum.demo.PipelineKey;
import org.drakum.demo.RenderPipeline;
import org.drakum.demo.Texture;
import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknBuffer;
import org.drakum.demo.vkn.VknPipeline;
import org.drakum.demo.vkn.VknRenderPass;
import org.drakum.demo.vkn.VknSampler;
import org.drakum.demo.vkn.VknShaderModule;

public class Registry
{
	public static Map<PipelineKey,VknPipeline> vkPipelineRegistry = new HashMap<>();
	public static Map<PipelineKey,LongId> pipelineLayoutRegistry = new HashMap<>();
	
	public static List<PipelineResourceContainer> pipelineResources = new ArrayList<>();
	public static List<PipelineLayoutResourceContainer> pipelineLayoutResources = new ArrayList<>();
	
	public static List<ModelResourceContainer> modelResources = new ArrayList<>();
	public static List<RenderPassResourceContainer> renderPassResources = new ArrayList<>();
	public static List<ShaderModuleResourceContainer> shaderModuleResources = new ArrayList<>();
	public static List<TextureResourceContainer> textureResources = new ArrayList<>();
	public static List<MaterialResourceContainer> materialResources = new ArrayList<>();
	public static List<BufferResourceContainer> bufferResources = new ArrayList<>();
	
	public static void registerPipeline(AttribFormat attribFormat, MaterialType materialType, VknPipeline pipeline)
	{
		PipelineKey key = new PipelineKey();
		key.attribFormatId = attribFormat.id();
		key.materialTypeId = materialType.getId();
		
		if(vkPipelineRegistry.containsKey(key)) return;
		
		vkPipelineRegistry.put(key, pipeline);
		pipelineLayoutRegistry.put(key, pipeline.layoutHandle());
		
		PipelineResourceContainer pipelineResourceContainer = new PipelineResourceContainer();
		pipelineResourceContainer.context = CommonRenderContext.context;
		pipelineResourceContainer.handle = pipeline.handle();
		
		pipelineResources.add(pipelineResourceContainer);
		
		PipelineLayoutResourceContainer pipelineLayoutResourceContainer = new PipelineLayoutResourceContainer();
		pipelineLayoutResourceContainer.context = CommonRenderContext.context;
		pipelineLayoutResourceContainer.handle = pipeline.layoutHandle();
		
		pipelineLayoutResources.add(pipelineLayoutResourceContainer);
	}
	
	public static VknPipeline createPipeline(AttribFormat attribFormat, MaterialType materialType, VknPipeline.Settings settings)
	{
		VknPipeline graphicsPipeline = new VknPipeline(settings);
		
		Registry.registerPipeline(attribFormat, materialType, graphicsPipeline);
		
		return graphicsPipeline;
	}
	
	public static void registerModel(Model model)
	{
		MemoryResourceContainer vertexBufferMemoryResource = new MemoryResourceContainer();
		vertexBufferMemoryResource.context = CommonRenderContext.context;
		vertexBufferMemoryResource.handle = model.vertexBuffer.memoryHandle();
		vertexBufferMemoryResource.object = model.vertexBuffer.memory;
		
		BufferResourceContainer vertexBufferResource = new BufferResourceContainer();
		vertexBufferResource.context = CommonRenderContext.context;
		vertexBufferResource.handle = model.vertexBuffer.handle();
		vertexBufferResource.memory = vertexBufferMemoryResource;
		
		MemoryResourceContainer indexBufferMemoryResource = new MemoryResourceContainer();
		indexBufferMemoryResource.context = CommonRenderContext.context;
		indexBufferMemoryResource.handle = model.indexBuffer.memoryHandle();
		indexBufferMemoryResource.object = model.indexBuffer.memory;
		
		BufferResourceContainer indexBufferResource = new BufferResourceContainer();
		indexBufferResource.context = CommonRenderContext.context;
		indexBufferResource.handle = model.indexBuffer.handle();
		indexBufferResource.memory = indexBufferMemoryResource;
		
		ModelResourceContainer modelResource = new ModelResourceContainer();
		modelResource.vertexBufferResource = vertexBufferResource;
		modelResource.indexBufferResource = indexBufferResource;
		
		modelResources.add(modelResource);
	}
	
	public static void registerRenderPass(VknRenderPass renderPass)
	{
		RenderPassResourceContainer renderPassResource = new RenderPassResourceContainer();
		renderPassResource.context = CommonRenderContext.context;
		renderPassResource.handle = renderPass.handle();
		
		renderPassResources.add(renderPassResource);
	}
	
	public static void registerShaderModule(VknShaderModule shaderModule)
	{
		ShaderModuleResourceContainer shaderModuleResource = new ShaderModuleResourceContainer();
		shaderModuleResource.context = CommonRenderContext.context;
		shaderModuleResource.handle = shaderModule.handle;
		
		shaderModuleResources.add(shaderModuleResource);
	}
	
	public static void registerMaterial(Material material)
	{
		MaterialResourceContainer materialResource = new MaterialResourceContainer();
		
		for(VknSampler sampler : material.samplers.values())
		{
			SamplerResourceContainer samplerResource = new SamplerResourceContainer();
			samplerResource.context = CommonRenderContext.context;
			samplerResource.handle = sampler.handle();
			
			materialResource.samplers.add(samplerResource);
		}
		
		materialResources.add(materialResource);
	}
	
	public static void registerTexture(Texture texture)
	{
		MemoryResourceContainer textureMemoryResource = new MemoryResourceContainer();
		textureMemoryResource.context = CommonRenderContext.context;
		textureMemoryResource.handle = texture.memory().handle();
		textureMemoryResource.object = texture.memory();
		
		ImageResourceContainer textureImageResource = new ImageResourceContainer();
		textureImageResource.context = CommonRenderContext.context;
		textureImageResource.handle = texture.image().handle();
		
		ImageViewResourceContainer textureImageViewResource = new ImageViewResourceContainer();
		textureImageViewResource.context = CommonRenderContext.context;
		textureImageViewResource.handle = texture.imageView().handle();
		
		TextureResourceContainer textureResource = new TextureResourceContainer();
		textureResource.memory = textureMemoryResource;
		textureResource.image = textureImageResource;
		textureResource.imageView = textureImageViewResource;
		
		textureResources.add(textureResource);
	}
	
	public static void registerBuffer(VknBuffer buffer)
	{
		MemoryResourceContainer memoryResource = new MemoryResourceContainer();
		memoryResource.context = CommonRenderContext.context;
		memoryResource.handle = buffer.memoryHandle();
		memoryResource.object = buffer.memory;
		
		BufferResourceContainer bufferResource = new BufferResourceContainer();
		bufferResource.context = CommonRenderContext.context;
		bufferResource.handle = buffer.handle();
		bufferResource.memory = memoryResource;
		
		bufferResources.add(bufferResource);
	}
	
	public static void close()
	{
		for(IResourceContainer model : modelResources)
		{
			model.close();
		}
		
		for(IResourceContainer material : materialResources)
		{
			material.close();
		}
		
		for(IResourceContainer texture : textureResources)
		{
			texture.close();
		}
		
		for(IResourceContainer pipeline : pipelineResources)
		{
			pipeline.close();
		}
		
		for(IResourceContainer pipelineLayout : pipelineLayoutResources)
		{
			pipelineLayout.close();
		}
		
		for(IResourceContainer shaderModule : shaderModuleResources)
		{
			shaderModule.close();
		}
		
		for(IResourceContainer renderPass : renderPassResources)
		{
			renderPass.close();
		}
		
		for(IResourceContainer buffer : bufferResources)
		{
			buffer.close();
		}
	}
}
