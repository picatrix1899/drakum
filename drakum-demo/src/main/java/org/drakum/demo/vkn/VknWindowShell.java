package org.drakum.demo.vkn;

import static org.lwjgl.glfw.GLFW.*;

public class VknWindowShell
{
	private long handle;
	
	public VknWindowShell(Settings settings)
	{
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

		long handle = glfwCreateWindow(settings.width, settings.height, settings.title, 0, 0);

		if (handle == 0)
		{
			throw new Error("Cannot create window");
		}
		
		this.handle = handle;
	}
	
	public VknWindowShell(long handle)
	{
		this.handle = handle;
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public void show()
	{
		glfwShowWindow(handle);
	}
	
	public boolean shouldClose()
	{
		return glfwWindowShouldClose(handle);
	}
	
	public void close()
	{
		glfwDestroyWindow(handle);
	}
	
	public static class Settings
	{
		public int width;
		public int height;
		public String title;
	}
}
