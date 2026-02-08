package org.drakum.hid;

import org.barghos.hid.IHidPhantomDevice;
import org.lwjgl.glfw.GLFW;

public class GlfwHidPhantomDevice implements IHidPhantomDevice
{
	@Override
	public String id()
	{
		return "glfw";
	}

	@Override
	public void poll()
	{
		GLFW.glfwPollEvents();
	}
	
}
