package org.drakum.rhi;

public class RenderApi
{
	private static IRhiApi api;
	
	public static void api(IRhiApi api)
	{
		RenderApi.api = api;
	}
	
	public static IRhiApi api()
	{
		return api;
	}
	
	public static IRhiWindowFactory windowFactory()
	{
		return api.windowFactory();
	}
	
	public static void init()
	{
		api.init();
	}
	
	public static void freeResources()
	{
		api.freeResources();
	}
}
