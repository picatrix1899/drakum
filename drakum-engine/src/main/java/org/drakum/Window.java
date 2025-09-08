package org.drakum;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;

import org.lwjgl.opengl.GL;

public class Window
{
	private long handle;
	
	public Window(int width, int height, String title)
	{
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		handle = glfwCreateWindow(width, height, title, 0, 0);
		
		glfwMakeContextCurrent(handle);
		
		GL.createCapabilities();
		
		glViewport(0, 0, width,  height);
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public boolean shouldClose()
	{
		return glfwWindowShouldClose(this.handle);
	}
	
	public void show()
	{
		glfwShowWindow(this.handle);
	}
	
	public void swapBuffers()
	{
		glfwSwapBuffers(this.handle);
	}
	
	public void releaseResources()
	{
		glfwDestroyWindow(this.handle);
	}
}
