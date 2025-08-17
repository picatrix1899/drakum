package org.drakum.rhi;

public interface IRhiApi
{
	void init();
	
	void freeResources();
	
	IRhiCapabilities capabilities();
	
	IRhiWindowFactory windowFactory();
}
