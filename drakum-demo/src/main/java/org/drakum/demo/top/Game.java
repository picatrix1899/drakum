package org.drakum.demo.top;

public class Game
{
	public MasterRenderer masterRenderer = new MasterRenderer();
	public World world = new World();
	
	public static Game instance;
	
	public static Game getInstance()
	{
		if(instance == null)
		{
			instance = new Game();
		}
		
		return instance;
	}
	
	public void render()
	{
		masterRenderer.render();
	}
}
