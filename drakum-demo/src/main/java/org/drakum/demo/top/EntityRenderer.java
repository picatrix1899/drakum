package org.drakum.demo.top;

public class EntityRenderer
{
	public MasterRenderer masterRenderer;
	
	public void renderEntity(Entity entity)
	{
		ModelRenderer modelRenderer = masterRenderer.modelRenderer;
		
		modelRenderer.renderModel(entity.model, entity.transform);
	}
}
