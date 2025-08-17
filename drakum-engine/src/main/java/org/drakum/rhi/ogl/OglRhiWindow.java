package org.drakum.rhi.ogl;

import static org.lwjgl.glfw.GLFW.*;

import org.drakum.rhi.IRhiWindow;
import org.drakum.rhi.glfw.GlfwWindowShell;
import org.drakum.window.WindowEventHandler;

public class OglRhiWindow implements IRhiWindow
{
	public GlfwWindowShell windowShell;

	@Override
	public void show()
	{
		this.windowShell.show();
	}

	@Override
	public void hide()
	{
		this.windowShell.hide();
	}

	@Override
	public void makeCurrent()
	{
		glfwMakeContextCurrent(this.windowShell.handle());
	}

	@Override
	public void beginFrame(long frame)
	{
		glfwSwapBuffers(this.windowShell.handle());
	}

	@Override
	public void endFrame(long frame)
	{
		
	}
	
	public int posX()
	{
		return this.windowShell.posX();
	}
	
	public int posY()
	{
		return this.windowShell.posY();
	}
	
	public int windowWidth()
	{
		return this.windowShell.windowWidth();
	}
	
	public int windowHeight()
	{
		return this.windowShell.windowHeight();
	}
	
	public int width()
	{
		return this.windowShell.width();
	}
	
	public int height()
	{
		return this.windowShell.height();
	}
	
	public void windowEventHandler(WindowEventHandler eventHandler)
	{
		this.windowShell.windowEventHandler(eventHandler);
	}
}
