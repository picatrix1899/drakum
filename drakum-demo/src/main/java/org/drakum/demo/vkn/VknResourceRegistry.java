package org.drakum.demo.vkn;

public class VknResourceRegistry
{
	private VknInstance vknInstance;
	
	public void __release()
	{
		vknInstance.__release(); vknInstance = null;
	}
}
