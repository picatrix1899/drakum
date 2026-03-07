package org.drakum.advInput;

import org.barghos.hid.HidInputKey;
import org.barghos.hid.HidInputState;
import org.barghos.hid.HidManager;

public class HighCondition implements ICondition
{
	public HidInputKey key;
	public boolean previousState;

	public HighCondition(HidInputKey key)
	{
		this.key = key;
		previousState = false;
	}
	
	@Override
	public boolean evaluate(HidManager hidManager)
	{
		HidInputState state = hidManager.getState(this.key);
		
		return state != null && state.value != 0;
	}
}
