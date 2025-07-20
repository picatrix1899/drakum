package org.drakum.demo.registry;

public class ModelResourceContainer implements IResourceContainer
{
	public BufferResourceContainer vertexBufferResource;
	public BufferResourceContainer indexBufferResource;
	
	@Override
	public void close()
	{
		vertexBufferResource.close();
		indexBufferResource.close();
	}
}
