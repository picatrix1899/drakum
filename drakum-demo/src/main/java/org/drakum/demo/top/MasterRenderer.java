package org.drakum.demo.top;

public class MasterRenderer
{
	public ModelRenderer modelRenderer = new ModelRenderer();
	public WorldRenderer worldRenderer = new WorldRenderer();
	public EntityRenderer entityRenderer = new EntityRenderer();
	
	public MasterRenderer()
	{
		worldRenderer.masterRenderer = this;
		entityRenderer.masterRenderer = this;
	}
	
	public void render()
	{
		Game game = Game.getInstance();
		
		worldRenderer.renderWorld(game.world);
	}
}
