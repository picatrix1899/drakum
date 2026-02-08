package org.drakum.input;

import java.util.HashSet;
import java.util.Set;

import org.barghos.hid.HidInputKey;
import org.barghos.hid.HidInputState;
import org.barghos.hid.HidManager;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public class InputKeyboard
{
	private Object2FloatMap<HidInputKey> lastState = new Object2FloatOpenHashMap<>();
	private Object2FloatMap<HidInputKey> currentState = new Object2FloatOpenHashMap<>();
	private Set<HidInputKey> keys = new HashSet<>();
	
	private String lastCharacter;
	private boolean trackCharacter;
	
	public HidManager hidManager;
	
	public void addKey(HidInputKey key)
	{
		keys.add(key);
	}
	
	public void addGlfwKey(int key)
	{
		HidInputKey hidKey = new HidInputKey(0, key);
		
		keys.add(hidKey);
	}
	
	public void sendCharEntered(String character)
	{
		if(!this.trackCharacter) return;
		
		this.lastCharacter = character;
	}
	
	public void update()
	{
		for(HidInputKey key : keys)
		{
			float current = currentState.getOrDefault(key, 0.0f);
			lastState.put(key, current);
		}
		
		lastCharacter = null;
		
		for(HidInputKey key : keys)
		{
			HidInputState state = hidManager.getState(key);

			this.currentState.put(key, state == null ? 0.0f : state.value);
		}
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
	
	public boolean isKeyPressed(HidInputKey key)
	{	
		if(!keys.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return last == 0.0f && current != 0.0f;
	}
	
	public boolean isKeyHeld(HidInputKey key)
	{
		if(!keys.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return (last == 0.0f && current != 0.0f) || (last != 0.0f && current != 0.0f);
	}
	
	public boolean isKeyReleased(HidInputKey key)
	{
		if(!keys.contains(key)) return false;
		
		float last = lastState.getOrDefault(key, 0.0f);
		float current = currentState.getOrDefault(key, 0.0f);
		
		return last != 0.0f && current == 0.0f;
	}

}
