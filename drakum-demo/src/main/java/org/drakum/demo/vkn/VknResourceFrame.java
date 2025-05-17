package org.drakum.demo.vkn;

public class VknResourceFrame
{
	public VknWindowShell window(VknWindowShell.CreateSettings settings)
	{
		return VknWindowShell.create(settings);
	}
	
	public VknSurface surface(VknSurface.CreateSettings settings)
	{
		return VknSurface.create(settings);
	}
	
	public VknGPU gpu(VknGPU.CreateSettings settings)
	{
		return VknGPU.create(settings);
	}
}
