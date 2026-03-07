package org.drakum.hid;

import org.barghos.core.math.MathUtils;
import org.barghos.hid.IHidDevice;
import org.barghos.hid.IHidDeviceInputSnapshot;
import org.lwjgl.glfw.GLFW;

public class GlfwMouseHidDevice implements IHidDevice
{
	public DefaultHidDeviceInputSnapshot<DefaultHidDeviceInputState> snapshot = new DefaultHidDeviceInputSnapshot<>();
	
	public double horizontalMax = 400;
	public double verticalMax = 400;
	
	private boolean hasMovedX = false;
	private boolean hasMovedY = false;
	
	public void onMouseMove(double x, double y)
	{
		long timestamp = System.nanoTime();
		
		if(x != 0)
		{
			double valueX = MathUtils.clamp(MathUtils.clamp(x, -horizontalMax, horizontalMax) / horizontalMax, -1.0, 1.0);
			
			DefaultHidDeviceInputState stateX = new DefaultHidDeviceInputState();
			stateX.id = -2;
			stateX.timestamp = timestamp;
			stateX.value = (float)valueX;
			
			this.snapshot.states.add(stateX);
			
			hasMovedX = true;
		}
		
		if(y != 0)
		{
			double valueY = MathUtils.clamp(MathUtils.clamp(y, -verticalMax, verticalMax) / verticalMax, -1.0, 1.0);
			
			DefaultHidDeviceInputState stateY = new DefaultHidDeviceInputState();
			stateY.id = -1;
			stateY.timestamp = timestamp;
			stateY.value = (float)valueY;
			
			this.snapshot.states.add(stateY);
			
			hasMovedY = true;
		}
	}
	
	public void onButton(int key, int action)
	{
		long timestamp = System.nanoTime();
		
		if(action == GLFW.GLFW_RELEASE || action == GLFW.GLFW_PRESS)
		{
			long inputKey = key;
			
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
		return "glfwMouse";
	}

	@Override
	public void reset()
	{
		this.snapshot = new DefaultHidDeviceInputSnapshot<>();
		
		this.hasMovedX = false;
		this.hasMovedY = false;
	}
	
	@Override
	public void poll()
	{
		long timestamp = System.nanoTime();
		
		if(!this.hasMovedX)
		{
			DefaultHidDeviceInputState stateX = new DefaultHidDeviceInputState();
			stateX.id = -2;
			stateX.timestamp = timestamp;
			stateX.value = 0.0f;
			
			this.snapshot.states.add(stateX);
		}
		
		if(!this.hasMovedY)
		{
			DefaultHidDeviceInputState stateY = new DefaultHidDeviceInputState();
			stateY.id = -1;
			stateY.timestamp = timestamp;
			stateY.value = 0.0f;
			
			this.snapshot.states.add(stateY);
		}
	}

	@Override
	public IHidDeviceInputSnapshot query()
	{
		return this.snapshot;
	}
}
