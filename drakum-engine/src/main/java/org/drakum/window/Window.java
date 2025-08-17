package org.drakum.window;

import org.drakum.rhi.IRhiWindow;

public class Window
{
	public IRhiWindow window;
	
	public Window(IRhiWindow window)
	{
		this.window = window;
	}
	
	public void show()
	{
		this.window.show();
	}
	
	public void hide()
	{
		this.window.hide();
	}
	
	public void makeCurrent()
	{
		this.window.makeCurrent();
	}
	
	public void beginFrame()
	{
		this.window.beginFrame(0);
	}
	
	public void endFrame()
	{
		this.window.endFrame(0);
	}
}
