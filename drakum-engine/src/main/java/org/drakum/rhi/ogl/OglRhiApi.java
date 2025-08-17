package org.drakum.rhi.ogl;

import static org.lwjgl.glfw.GLFW.*;

import org.drakum.hid.GlfwHidProvider;
import org.drakum.hid.HidManager;
import org.drakum.rhi.IRhiApi;
import org.drakum.rhi.IRhiCapabilities;
import org.drakum.rhi.IRhiWindowFactory;
import org.lwjgl.opengl.GL;

public class OglRhiApi implements IRhiApi
{
	private OglRhiCapabilities capabilities;
	private OglRhiWindowFactory windowFactory;
	
	public OglRhiApi()
	{
		if(!glfwInit())
		{
			throw new Error("Cannot init glfw");
		}
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);

		createCapabilities();
		
		this.windowFactory = new OglRhiWindowFactory();
		
		HidManager.registerProvider(new GlfwHidProvider());
	}
	
	@Override
	public void init()
	{
		
	}

	private void createCapabilities()
	{
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		long tempWindowHandle = glfwCreateWindow(100, 100, "", 0, 0);
		
		if(tempWindowHandle == 0)
		{
			throw new Error("Cannot create Window");
		}
		
		glfwMakeContextCurrent(tempWindowHandle);
		
		GL.createCapabilities();
		
		glfwDestroyWindow(tempWindowHandle);
		
		OglRhiCapabilities capabilities = new OglRhiCapabilities();
		
		this.capabilities = capabilities;
	}
	
	@Override
	public void freeResources()
	{
		glfwTerminate();
	}

	@Override
	public IRhiCapabilities capabilities()
	{
		return this.capabilities;
	}

	@Override
	public IRhiWindowFactory windowFactory()
	{
		return this.windowFactory;
	}
	
}
