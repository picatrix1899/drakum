package org.drakum.advInput;

import java.util.ArrayList;
import java.util.List;

import org.barghos.hid.HidManager;

public class AdvancedInputManager
{
	public List<InputAction> actions = new ArrayList<>();
	public HidManager hidManager;
	
	public void update()
	{
		for(InputAction action : this.actions)
		{
			if(action.condition.evaluate(this.hidManager))
			{
				action.perform();
			}
		}
	}
}
