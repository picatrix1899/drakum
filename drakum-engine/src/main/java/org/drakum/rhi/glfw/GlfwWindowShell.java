package org.drakum.rhi.glfw;

import static org.lwjgl.glfw.GLFW.*;

import org.drakum.window.WindowEventHandler;
import org.drakum.window.WindowState;

public class GlfwWindowShell
{
	private long handle;
	
	private WindowState state;
	private WindowEventHandler windowEventHandler;
	private int posX;
	private int posY;
	private int windowWidth;
	private int windowHeight;
	private int width;
	private int height;
	
	public GlfwWindowShell(Settings settings)
	{
		this.state = settings.state;
		this.windowEventHandler = settings.windowEventHandler;
		
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, settings.isResizable ? GLFW_TRUE : GLFW_FALSE);
		
		long handle = glfwCreateWindow(this.state.width, this.state.height, this.state.title, 0, 0);
		
		if(handle == 0)
		{
			throw new Error("Cannot create Window");
		}
		
		this.handle = handle;
		
		glfwSetWindowCloseCallback(this.handle, this::onWindowClose);
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public void show()
	{
		glfwShowWindow(this.handle);
	}
	
	public void hide()
	{
		glfwHideWindow(this.handle);
	}
	
	public void freeResources()
	{
		glfwDestroyWindow(this.handle);
		
		this.handle = 0;
	}
	
	private void onWindowClose(long window)
	{
		if(this.windowEventHandler == null) return;
		
		this.windowEventHandler.onClose();
	}
	
	public int posX()
	{
		return this.posX;
	}
	
	public int posY()
	{
		return this.posY;
	}
	
	public int windowWidth()
	{
		return this.windowWidth;
	}
	
	public int windowHeight()
	{
		return this.windowHeight;
	}
	
	public int width()
	{
		return this.width;
	}
	
	public int height()
	{
		return this.height;
	}
	
	public void windowEventHandler(WindowEventHandler eventHandler)
	{
		this.windowEventHandler = eventHandler;
	}
	
	public static class Settings
	{
		public WindowState state;
		public WindowEventHandler windowEventHandler;
		public boolean isResizable;
	}
}
