package org.drakum.demo;

public class CommonRenderContext
{
	private static CommonRenderContext INSTANCE;
	
	public static CommonRenderContext instance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new CommonRenderContext();
		}
		
		return INSTANCE;
	}
	
	public VulkanInstance vkInstance;
	public GPU gpu;
	public long commandPool;
}
