package org.drakum.advInput;

public class InputAction
{
	public ICondition condition;
	public Callback callback;
	
	public void perform()
	{
		this.callback.call();
	}
	
	@FunctionalInterface
	public static interface Callback
	{
		void call();
	}
}
