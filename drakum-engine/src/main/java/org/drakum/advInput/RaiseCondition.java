package org.drakum.advInput;

import org.barghos.hid.HidInputKey;
import org.barghos.hid.HidInputState;
import org.barghos.hid.HidManager;

public class RaiseCondition implements ICondition
{
	public HidInputKey key;
	public boolean previousState;

	public RaiseCondition(HidInputKey key)
	{
		this.key = key;
		previousState = false;
	}
	
	@Override
	public boolean evaluate(HidManager hidManager)
	{
		if(previousState) return false;
		
		HidInputState state = hidManager.getState(this.key);
		if(state != null && state.value != 0)
		{
			this.previousState = true;
			return true;
		}
		
		this.previousState = false;
		return false;
	}
}
