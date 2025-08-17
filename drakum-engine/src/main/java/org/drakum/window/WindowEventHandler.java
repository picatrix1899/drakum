package org.drakum.window;

public class WindowEventHandler
{
	public CloseCallback callbackClose;
	
	public void onClose()
	{
		if(this.callbackClose == null) return;
		
		this.callbackClose.call();
	}
	
	@FunctionalInterface
	public static interface CloseCallback
	{
		void call();
	}
}
