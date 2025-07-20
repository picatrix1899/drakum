package org.drakum.demo.registry;


import java.util.ArrayList;
import java.util.List;

public class MaterialResourceContainer implements IResourceContainer
{
	public List<SamplerResourceContainer> samplers = new ArrayList<>();
	
	@Override
	public void close()
	{
		for(SamplerResourceContainer sampler : samplers)
		{
			sampler.close();
		}
	}
}
