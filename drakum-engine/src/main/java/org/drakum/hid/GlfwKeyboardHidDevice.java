package org.drakum.hid;

import java.util.HashSet;
import java.util.Set;

import org.barghos.hid.IHidDevice;
import org.barghos.hid.IHidDeviceInputSnapshot;
import org.barghos.hid.IHidDeviceInputState;
import org.lwjgl.glfw.GLFW;

public class GlfwKeyboardHidDevice implements IHidDevice
{
	public InputSnapshot snapshot = new InputSnapshot();
	
	public Set<Long> windowIds = new HashSet<>();
	
	public void bindWindow(long windowId)
	{
		if(this.windowIds.contains(windowId)) return;

		this.windowIds.add(windowId);
		
		GLFW.glfwSetKeyCallback(windowId, (_, key, scancode, action, _) -> onKey(key, scancode, action));
	}
	
	public void unbindWindow(long windowId)
	{
		if(!this.windowIds.contains(windowId)) return;
		
		this.windowIds.remove(windowId);
		
		GLFW.glfwSetKeyCallback(windowId, null);
	}
	
	public void onKey(int key, int scancode, int action)
	{
		long timestamp = System.nanoTime();
		
		if(action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS)
		{
			long inputKey = key != GLFW.GLFW_KEY_UNKNOWN ? key : 100000 + scancode;
			
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
		return "glfwKeyboard";
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
