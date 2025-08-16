package org.drakum.demo.top;

import java.util.List;

public class WorldRenderer
{
	public MasterRenderer masterRenderer;
	
	public void renderWorld(World world)
	{
		EntityRenderer entityRenderer = masterRenderer.entityRenderer;
		
		List<Entity> entities = world.entities;
		
		for(Entity entity : entities)
		{
			entityRenderer.renderEntity(entity);
		}
	}
}
