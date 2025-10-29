package org.drakum.entity;

public interface IEntityTemplate
{
	public long getId();
	
	public Entity createEntity();
	
	public void destroyEntity(Entity entity);
}
