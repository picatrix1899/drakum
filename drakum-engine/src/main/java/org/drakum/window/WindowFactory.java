package org.drakum.window;

import org.drakum.rhi.IRhiWindow;
import org.drakum.rhi.IRhiWindowFactory;
import org.drakum.rhi.RenderApi;

public class WindowFactory
{	
	public static Window create(CreateSettings settings)
	{
		WindowState state = settings.state;
		if(state == null) state = new WindowState();
		
		IRhiWindowFactory.CreateSettings rhiWindowCreateSettings = new IRhiWindowFactory.CreateSettings();
		rhiWindowCreateSettings.state = state;
		rhiWindowCreateSettings.windowEventHandler = settings.windowEventHandler;
		rhiWindowCreateSettings.isResizable = settings.isResizable;
		
		IRhiWindow rhiWindow = RenderApi.windowFactory().create(rhiWindowCreateSettings);
		
		Window window = new Window(rhiWindow);
		
		return window;
	}
	
	public static void destroy(Window window)
	{
		RenderApi.windowFactory().destroy(window.window);;
	}
	
	public static class CreateSettings
	{
		public WindowState state;
		public WindowEventHandler windowEventHandler;
		public boolean isResizable;
	}
}
