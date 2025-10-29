package org.drakum.entity;

import org.drakum.IdManager;
import org.drakum.Texture;
import org.drakum.model.RawModel;

public class EntityTemplateStaticModel implements IEntityTemplate, ITexturedModelProvider
{
	public final long id;
	public RawModel model;
	public Texture texture;
	
	public EntityTemplateStaticModel()
	{
		this.id = IdManager.nextEntityTemplateId();
	}
	
	public long getId()
	{
		return this.id;
	}
	
	public Entity createEntity()
	{
		Entity entity = new Entity(IdManager.nextEntityId(), this.id);
		
		return entity;
	}
	
	public void destroyEntity(Entity entity)
	{
		
	}
	
	public RawModel getModel()
	{
		return this.model;
	}
	
	public Texture getTexture()
	{
		return this.texture;
	}
}
