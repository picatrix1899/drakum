package org.drakum.hid;

import org.barghos.hid.IHidDeviceInputState;

public class DefaultHidDeviceInputState implements IHidDeviceInputState
{
	public long id;
	public float value;
	public long timestamp;
	
	@Override
	public long id()
	{
		return id;
	}

	@Override
	public float value()
	{
		return value;
	}

	@Override
	public long timestamp()
	{
		return timestamp;
	}
}
