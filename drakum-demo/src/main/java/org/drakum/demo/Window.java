package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;

public class Window
{
	public long handle;
	
	public Window(long handle)
	{
		this.handle = handle;
	}
	
	public void show()
	{
		glfwShowWindow(handle);
	}
	
	public boolean shouldClose()
	{
		return glfwWindowShouldClose(handle);
	}
	
	public void __release()
	{
		glfwDestroyWindow(handle);
	}
	
	public static class Builder
	{
		public Window create()
		{
			glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
			glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

			long handle = glfwCreateWindow(800, 600, "Drakum Demo", 0, 0);

			if (handle == 0)
			{
				throw new Error("Cannot create window");
			}
			
			Window result = new Window(handle);
			
			return result;
		}
	}
}
