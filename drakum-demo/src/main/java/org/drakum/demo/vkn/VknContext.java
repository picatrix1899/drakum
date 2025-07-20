package org.drakum.demo.vkn;

public class VknContext
{
	public static final boolean OBJECT_VALIDATION;
	
	static
	{
		OBJECT_VALIDATION = VknConfig.OBJECT_VALIDATION;
	}
	
	public VknInstance instance;
	public VknGPU gpu;
	public VknCommandPool commandPool;
}
