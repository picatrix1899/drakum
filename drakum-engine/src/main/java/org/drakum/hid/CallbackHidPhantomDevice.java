package org.drakum.hid;

import org.barghos.hid.IHidPhantomDevice;

public class CallbackHidPhantomDevice implements IHidPhantomDevice
{
	private String id;
	private Callback callback;
	
	public CallbackHidPhantomDevice(String id, Callback callback)
	{
		this.id = id;
		this.callback = callback;
	}
	
	@Override
	public String id()
	{
		return this.id;
	}

	@Override
	public void poll()
	{
		callback.call();
	}
	
	@FunctionalInterface
	public static interface Callback
	{
		void call();
	}
}
