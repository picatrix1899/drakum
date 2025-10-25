package org.drakum;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntSet;

public class InputKeyboard
{
	public static final int ACTION_RELEASED = 0;
	public static final int ACTION_PRESSED = 1;
	public static final int ACTION_REPEATED = 2;
	
	private Int2IntMap lastState = new Int2IntOpenHashMap();
	private Int2IntMap currentState = new Int2IntOpenHashMap();
	private IntSet keys = new IntOpenHashSet();
	
	private String lastCharacter;
	private boolean trackCharacter;
	
	public void sendKeyAction(int key, int action)
	{
		keys.add(key);
		currentState.put(key, action);
	}
	
	public void sendCharEntered(String character)
	{
		if(!this.trackCharacter) return;
		
		this.lastCharacter = character;
	}
	
	public void preUpdate()
	{
		for(int key : keys)
		{
			int current = currentState.getOrDefault(key, ACTION_RELEASED);
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
		int last = lastState.getOrDefault(key, ACTION_RELEASED);
		int current = currentState.getOrDefault(key, ACTION_RELEASED);
		
		return last == ACTION_RELEASED && current == ACTION_PRESSED;
	}
	
	public boolean isKeyHeld(int key)
	{
		int last = lastState.getOrDefault(key, ACTION_RELEASED);
		int current = currentState.getOrDefault(key, ACTION_RELEASED);
		
		return (last == ACTION_RELEASED && current == ACTION_PRESSED) || (last != ACTION_RELEASED && current != ACTION_RELEASED);
	}
	
	public boolean isKeyPressedOrRepeated(int key)
	{
		int last = lastState.getOrDefault(key, ACTION_RELEASED);
		int current = currentState.getOrDefault(key, ACTION_RELEASED);
		
		return (last == ACTION_RELEASED && current == ACTION_PRESSED) || (last != ACTION_RELEASED && current == ACTION_REPEATED);
	}
	
	public boolean isKeyRepeated(int key)
	{
		int last = lastState.getOrDefault(key, ACTION_RELEASED);
		int current = currentState.getOrDefault(key, ACTION_RELEASED);
		
		return last != ACTION_RELEASED && current == ACTION_PRESSED;
	}
	
	public boolean isKeyReleased(int key)
	{
		int last = lastState.getOrDefault(key, ACTION_RELEASED);
		int current = currentState.getOrDefault(key, ACTION_RELEASED);
		
		return last != ACTION_RELEASED && current == ACTION_RELEASED;
	}

}
