package org.drakum.input;

import java.util.HashSet;
import java.util.Set;

import org.barghos.hid.HidInputKey;
import org.barghos.hid.HidInputState;
import org.barghos.hid.HidManager;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public class InputMouse
{
	private Object2FloatMap<HidInputKey> lastState = new Object2FloatOpenHashMap<>();
	private Object2FloatMap<HidInputKey> currentState = new Object2FloatOpenHashMap<>();
	private Set<HidInputKey> buttons = new HashSet<>();
	
	public HidManager hidManager;
	
	public void addButton(HidInputKey key)
	{
		buttons.add(key);
	}
	
	public void update()
	{
		for(HidInputKey key : buttons)
		{
			float current = currentState.getOrDefault(key, 0.0f);
			lastState.put(key, current);
		}
		
		for(HidInputKey key : buttons)
		{
			HidInputState state = hidManager.getState(key);

			this.currentState.put(key, state == null ? 0.0f : state.value);
		}
	}
	
	public boolean isKeyPressed(HidInputKey key)
	{	
		if(!buttons.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return last == 0.0f && current != 0.0f;
	}
	
	public boolean isKeyHeld(HidInputKey key)
	{
		if(!buttons.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return (last == 0.0f && current != 0.0f) || (last != 0.0f && current != 0.0f);
	}
	
	public boolean isKeyReleased(HidInputKey key)
	{
		if(!buttons.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return last != 0.0f && current == 0.0f;
	}
}
