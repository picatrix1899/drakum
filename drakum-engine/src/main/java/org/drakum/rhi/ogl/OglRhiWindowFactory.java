package org.drakum.rhi.ogl;

import org.drakum.rhi.IRhiWindow;
import org.drakum.rhi.IRhiWindowFactory;
import org.drakum.rhi.glfw.GlfwWindowShell;

public class OglRhiWindowFactory implements IRhiWindowFactory
{

	@Override
	public IRhiWindow create(CreateSettings settings)
	{
		GlfwWindowShell.Settings windowShellSettings = new GlfwWindowShell.Settings();
		windowShellSettings.state = settings.state;
		windowShellSettings.windowEventHandler = settings.windowEventHandler;
		windowShellSettings.isResizable = settings.isResizable;
		
		GlfwWindowShell windowShell = new GlfwWindowShell(windowShellSettings);

		OglRhiWindow rhiWindow = new OglRhiWindow();
		rhiWindow.windowShell = windowShell;
		
		return rhiWindow;
	}

	@Override
	public void destroy(IRhiWindow window)
	{
		OglRhiWindow rhiWindow = (OglRhiWindow)window;
		rhiWindow.windowShell.freeResources();
	}
	
}
