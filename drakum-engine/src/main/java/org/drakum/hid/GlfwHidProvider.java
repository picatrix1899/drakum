package org.drakum.hid;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwHidProvider implements IHidProvider
{

	@Override
	public void update()
	{
		glfwPollEvents();
	}

}
