package org.drakum.rhi;

import org.drakum.window.WindowEventHandler;
import org.drakum.window.WindowState;

public interface IRhiWindowFactory
{
	IRhiWindow create(CreateSettings settings);
	
	void destroy(IRhiWindow window);
	
	public static class CreateSettings
	{
		public WindowState state;
		public WindowEventHandler windowEventHandler;
		public boolean isResizable;
	}
}
