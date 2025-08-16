package org.drakum;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.opengl.GL;

public class Window
{
	private long handle;
	
	private WindowState state;
	
	private CloseRequestedEvent closeRequestedEventCallback;
	
	public Window(Settings settings)
	{
		this.state = settings.state;
		
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, settings.isResizable ? GLFW_TRUE : GLFW_FALSE);
		
		long handle = glfwCreateWindow(this.state.width, this.state.height, this.state.title, 0, 0);
		
		if(handle == 0)
		{
			throw new Error("Cannot create Window");
		}
		
		this.handle = handle;
		
		glfwMakeContextCurrent(this.handle);
		
		GL.createCapabilities();
		
		this.closeRequestedEventCallback = settings.closeRequestedEventCallback;
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
	
	public void beginFrame(long frame)
	{
		glfwSwapBuffers(this.handle);
	}
	
	public void endFrame(long frame)
	{
		
	}
	
	public void destroyResource()
	{
		glfwDestroyWindow(this.handle);
		this.handle = 0;
	}
	
	private void onWindowClose(long window)
	{
		if(this.closeRequestedEventCallback != null) this.closeRequestedEventCallback.raise(this);
	}
	
	public static class Settings
	{
		public WindowState state;
		public boolean isResizable;
		public CloseRequestedEvent closeRequestedEventCallback;
	}
	
	@FunctionalInterface
	public static interface CloseRequestedEvent
	{
		void raise(Window window);
	}
}
