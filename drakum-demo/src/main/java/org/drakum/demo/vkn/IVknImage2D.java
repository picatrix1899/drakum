package org.drakum.demo.vkn;

import org.drakum.demo.registry.LongId;

public interface IVknImage2D
{
	LongId handle();
	
	int width();
	int height();
	
	int format();
	
	VknImageView2D createView();
	
	void close();
}
