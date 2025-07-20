package org.drakum.demo.vkn;

public interface IVknImage2D
{
	long handle();
	
	int width();
	int height();
	
	int format();
	
	VknImageView2D createView();
	
	void close();
}
