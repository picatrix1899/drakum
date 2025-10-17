package org.drakum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputKeyboard
{
	private Map<Integer,Action> lastState = new HashMap<>();
	private Map<Integer,Action> currentState = new HashMap<>();
	private Set<Integer> keys = new HashSet<>();
	
	private String lastCharacter;
	private boolean trackCharacter;
	
	public void sendKeyAction(int key, Action action)
	{
		keys.add(key);
		currentState.put(key, action);
	}
	
	public void sendCharEntered(String character)
	{
		if(!this.trackCharacter) return;
		
		this.lastCharacter = character;
	}
	
	public void preTick()
	{
		for(int key : keys)
		{
			Action current = currentState.getOrDefault(key, Action.RELEASED);
			lastState.put(key, current);
		}
		
		lastCharacter = null;
	}
	
	public void trackCharacter(boolean trackCharacter)
	{
		this.trackCharacter = trackCharacter;
	}
	
	public boolean trackCharacter()
	{
		return trackCharacter;
	}
	
	
	public String lastCharacter()
	{
		return this.lastCharacter;
	}
	
	public boolean isKeyPressed(int key)
	{
		Action last = lastState.getOrDefault(key, Action.RELEASED);
		Action current = currentState.getOrDefault(key, Action.RELEASED);
		
		return last == Action.RELEASED && current == Action.PRESSED;
	}
	
	public boolean isKeyHeld(int key)
	{
		Action last = lastState.getOrDefault(key, Action.RELEASED);
		Action current = currentState.getOrDefault(key, Action.RELEASED);
		
		return (last == Action.RELEASED && current == Action.PRESSED) || (last != Action.RELEASED && current != Action.RELEASED);
	}
	
	public boolean isKeyPressedOrRepeated(int key)
	{
		Action last = lastState.getOrDefault(key, Action.RELEASED);
		Action current = currentState.getOrDefault(key, Action.RELEASED);
		
		return (last == Action.RELEASED && current == Action.PRESSED) || (last != Action.RELEASED && current == Action.REPEATED);
	}
	
	public boolean isKeyRepeated(int key)
	{
		Action last = lastState.getOrDefault(key, Action.RELEASED);
		Action current = currentState.getOrDefault(key, Action.RELEASED);
		
		return last != Action.RELEASED && current == Action.PRESSED;
	}
	
	public boolean isKeyReleased(int key)
	{
		Action last = lastState.getOrDefault(key, Action.RELEASED);
		Action current = currentState.getOrDefault(key, Action.RELEASED);
		
		return last != Action.RELEASED && current == Action.RELEASED;
	}
	
	public static enum Action
	{
		RELEASED,
		PRESSED,
		REPEATED
		;
	}
}
