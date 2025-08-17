package org.drakum.rhi;

import org.drakum.window.WindowEventHandler;

public interface IRhiWindow
{
	void show();
	
	void hide();
	
	void makeCurrent();
	
	void beginFrame(long frame);
	
	void endFrame(long frame);
	
	int posX();
	
	int posY();
	
	int windowWidth();
	
	int windowHeight();
	
	int width();
	
	int height();
	
	void windowEventHandler(WindowEventHandler eventHandler);
}
