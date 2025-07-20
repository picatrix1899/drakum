package org.drakum.demo.vkn;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWErrorCallback;

public class GLFWContext
{
	public static void init()
	{
		if (!glfwInit())
		{
			throw new Error("Cannot init glfw");
		}

		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.out));

		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
	}
	
	public static void close()
	{
		glfwTerminate();
	}
}
