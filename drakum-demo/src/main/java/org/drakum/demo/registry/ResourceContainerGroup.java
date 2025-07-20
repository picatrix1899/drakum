package org.drakum.demo.registry;

import java.util.ArrayList;
import java.util.List;

public class ResourceContainerGroup implements IResourceContainer
{
	public List<IResourceContainer> containers = new ArrayList<>();
	
	@Override
	public void close()
	{
		for(IResourceContainer container : containers)
		{
			container.close();
		}
	}
	
}
