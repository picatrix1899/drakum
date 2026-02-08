package org.drakum.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL46C.*;

import org.barghos.glfw.window.GlfwWindow;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;

public class Engine
{
	private boolean isRunning;
	private IEngineRoutine routine;
	private IEngineLoop loop;
	private boolean debug;
	private GlfwWindow window;
	
	public void setRoutine(IEngineRoutine routine)
	{
		this.routine = routine;
	}
	
	public void setLoop(IEngineLoop loop)
	{
		this.loop = loop;
	}
	
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}
	
	public void setWindow(GlfwWindow window)
	{
		this.window = window;
		
		window.makeContextCurrent();

		createCapabilities();
		
		if(this.debug)
		{
			if (getCapabilities().GL_KHR_debug)
			{
				glEnable(GL_DEBUG_OUTPUT);
				glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);

			    GLDebugMessageCallback callback = GLDebugMessageCallback.create((_, _, _, severity, length, message, _) -> {
			        String msg = GLDebugMessageCallback.getMessage(length, message);

			        String sev = mapSeverity(severity);

			        System.err.println("[GL DEBUG " + sev + "] " + msg);
			    });
			    
			    glDebugMessageCallback(callback, 0);
			}
		}
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
	
	private String mapSeverity(int severity)
	{
		return switch(severity) {
    	case GL_DEBUG_SEVERITY_NOTIFICATION -> "Info";
    	case GL_DEBUG_SEVERITY_LOW -> "Low";
    	case GL_DEBUG_SEVERITY_MEDIUM -> "Medium";
    	case GL_DEBUG_SEVERITY_HIGH -> "High";
    	default -> "";
		};
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
		
		window.releaseResources();
		
		glfwTerminate();
	}
	
	public GlfwWindow window()
	{
		return this.window;
	}
}
