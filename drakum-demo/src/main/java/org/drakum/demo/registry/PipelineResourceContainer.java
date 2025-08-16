package org.drakum.demo.registry;

import org.drakum.demo.vkn.VknContext;
import org.drakum.demo.vkn.VknInternalUtils;

public class PipelineResourceContainer implements IResourceContainer
{
	public LongId handle;
	public VknContext context;
	
	@Override
	public void close()
	{
		VknInternalUtils.destroyPipeline(context, handle);
	}

}
