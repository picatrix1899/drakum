package org.drakum.engine;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWErrorCallback;

public class Engine
{
	private boolean isRunning;
	private IEngineRoutine routine;
	private IEngineLoop loop;
	
	public void setRoutine(IEngineRoutine routine)
	{
		this.routine = routine;
	}
	
	public void setLoop(IEngineLoop loop)
	{
		this.loop = loop;
	}
	
	public void start()
	{
		this.isRunning = true;
		
		this.loop.setRoutine(this.routine);
		
		run();
	}
	
	public void stop()
	{
		this.isRunning = false;
	}
	
	public void init()
	{
		glfwInit();
		
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
		this.routine.earlyInit();
		this.routine.init();
		this.routine.lateInit();
	}
	
	public void run()
	{
		init();
		
		this.loop.init();
		
		while(this.isRunning)
		{
			this.loop.cycle();
		}
		
		this.loop.releaseResources();
		
		releaseResources();
	}
	
	public void releaseResources()
	{
		this.routine.releaseResources();
		
		glfwTerminate();
	}
}
