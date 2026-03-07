package org.drakum.hid;

import org.barghos.hid.IHidDevice;
import org.barghos.hid.IHidDeviceInputSnapshot;
import org.lwjgl.glfw.GLFW;

public class GlfwKeyboardHidDevice implements IHidDevice
{
	public DefaultHidDeviceInputSnapshot<DefaultHidDeviceInputState> snapshot = new DefaultHidDeviceInputSnapshot<>();
	
	public void onKey(int key, int scancode, int action)
	{
		long timestamp = System.nanoTime();
		
		if(action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS)
		{
			long inputKey = key != GLFW.GLFW_KEY_UNKNOWN ? key : 100000 + scancode;
			
			float value = action == GLFW.GLFW_PRESS ? 1.0f : 0.0f;
			
			DefaultHidDeviceInputState state = new DefaultHidDeviceInputState();
			state.id = inputKey;
			state.timestamp = timestamp;
			state.value = value;
			
			this.snapshot.states.add(state);
		}
	}
	
	@Override
	public String id()
	{
		return "glfwKeyboard";
	}

	@Override
	public void reset()
	{
		this.snapshot = new DefaultHidDeviceInputSnapshot<>();
	}
	
	@Override
	public void poll()
	{
	}

	@Override
	public IHidDeviceInputSnapshot query()
	{
		return this.snapshot;
	}
}
