package org.drakum.demo;

import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;

import java.util.HashMap;
import java.util.Map;

import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknPipeline;

public class Registry
{
	public static Map<PipelineKey,VknPipeline> vkPipelineRegistry = new HashMap<>();
	public static Map<PipelineKey,Long> pipelineLayoutRegistry = new HashMap<>();
	public static Map<PipelineKey,RenderPipeline> pipelineRegistry = new HashMap<>();
	
	public static RenderPipeline registerPipeline(AttribFormat attribFormat, MaterialType materialType, VknPipeline pipeline)
	{
		PipelineKey key = new PipelineKey();
		key.attribFormatId = attribFormat.id();
		key.materialTypeId = materialType.getId();
		
		if(pipelineRegistry.containsKey(key)) return null;
		
		vkPipelineRegistry.put(key, pipeline);
		pipelineLayoutRegistry.put(key, pipeline.layoutHandle());
		
		RenderPipeline p = new RenderPipeline();
		p.handle = pipeline.handle();
		p.layoutHandle = pipeline.layoutHandle();
		
		pipelineRegistry.put(key, p);
		
		return p;
		
	}
	
	public static void close()
	{
		for(VknPipeline pipeline : vkPipelineRegistry.values())
		{
			pipeline.close();
		}
		
		for(long pipelineLayout : pipelineLayoutRegistry.values())
		{
			vkDestroyPipelineLayout(CommonRenderContext.context.gpu.handle(), pipelineLayout, null);
		}
	}
}
