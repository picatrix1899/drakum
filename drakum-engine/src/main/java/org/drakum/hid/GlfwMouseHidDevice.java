package org.drakum.hid;

import java.util.HashSet;
import java.util.Set;

import org.barghos.hid.IHidDevice;
import org.barghos.hid.IHidDeviceInputSnapshot;
import org.barghos.hid.IHidDeviceInputState;
import org.lwjgl.glfw.GLFW;

public class GlfwMouseHidDevice implements IHidDevice
{
	public InputSnapshot snapshot = new InputSnapshot();
	
	private double lastX;
	private double lastY;
	
	public void setLastCursorPos(double x, double y)
	{
		lastX = x;
		lastY = y;
	}
	
	public void onMouseMove(double x, double y)
	{
		double rX = x - lastX;
		double rY = y - lastY;
		
		System.out.println("adx=" + lastX + " " + "ady=" + lastY + " " + "x=" + x + " " + "y=" + y + " " + "rX=" + rX + " " + "rY=" + rY);
		
		this.lastX = x;
		this.lastY = y;
	}
	
	public void onButton(int key, int action)
	{
		long timestamp = System.nanoTime();
		
		if(action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS)
		{
			long inputKey = key;
			
			float value = action == GLFW.GLFW_PRESS ? 1.0f : 0.0f;
			
			InputState state = new InputState();
			state.id = inputKey;
			state.timestamp = timestamp;
			state.value = value;
			
			this.snapshot.states.add(state);
		}
	}
	
	@Override
	public String id()
	{
		return "glfwMouse";
	}

	@Override
	public void reset()
	{
		this.snapshot = new InputSnapshot();
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

	private static class InputSnapshot implements IHidDeviceInputSnapshot
	{
		public Set<InputState> states = new HashSet<>();
		
		@Override
		public Set<? extends IHidDeviceInputState> states()
		{
			return this.states;
		}
		
	}
	
	private static class InputState implements IHidDeviceInputState
	{
		public long id;
		public float value;
		public long timestamp;
		
		
		@Override
		public long id()
		{
			return id;
		}

		@Override
		public float value()
		{
			return value;
		}

		@Override
		public long timestamp()
		{
			return timestamp;
		}
		
	}
}
