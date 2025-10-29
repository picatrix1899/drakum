package org.drakum.input;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputMouse
{
	private Map<Integer,Integer> lastState = new HashMap<>();
	private Map<Integer,Integer> currentState = new HashMap<>();
	private Set<Integer> buttons = new HashSet<>();
	
	private float posX;
	private float posY;
	
	public void sendButtonAction(int button, int action)
	{
		currentState.put(button, action);
		buttons.add(button);
	}
	
	public void preTick()
	{
		for(int button : buttons)
		{
			int current = currentState.getOrDefault(button, GLFW_RELEASE);
			lastState.put(button, current);
		}
	}
	
	public boolean isKeyPressed(int button)
	{
		int last = lastState.getOrDefault(button, GLFW_RELEASE);
		int current = currentState.getOrDefault(button, GLFW_RELEASE);
		
		return last == GLFW_RELEASE && current == GLFW_PRESS;
	}
	
	public boolean isKeyHeld(int button)
	{
		int last = lastState.getOrDefault(button, GLFW_RELEASE);
		int current = currentState.getOrDefault(button, GLFW_RELEASE);
		
		return (last == GLFW_RELEASE && current == GLFW_PRESS) || (last > GLFW_RELEASE && current > GLFW_RELEASE);
	}
	
	public boolean isKeyPressedOrRepeated(int button)
	{
		int last = lastState.getOrDefault(button, GLFW_RELEASE);
		int current = currentState.getOrDefault(button, GLFW_RELEASE);
		
		return (last == GLFW_RELEASE && current == GLFW_PRESS) || (last > GLFW_RELEASE && current == GLFW_REPEAT);
	}
	
	public boolean isKeyRepeated(int button)
	{
		int last = lastState.getOrDefault(button, GLFW_RELEASE);
		int current = currentState.getOrDefault(button, GLFW_RELEASE);
		
		return last > GLFW_RELEASE && current == GLFW_REPEAT;
	}
	
	public boolean isKeyReleased(int button)
	{
		int last = lastState.getOrDefault(button, GLFW_RELEASE);
		int current = currentState.getOrDefault(button, GLFW_RELEASE);
		
		return last > GLFW_RELEASE && current == GLFW_RELEASE;
	}
}
