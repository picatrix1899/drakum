package org.drakum.hid;

import java.util.HashSet;
import java.util.Set;

import org.barghos.hid.IHidDeviceInputSnapshot;
import org.barghos.hid.IHidDeviceInputState;

public class DefaultHidDeviceInputSnapshot<InputStateType extends IHidDeviceInputState>  implements IHidDeviceInputSnapshot
{
	public Set<InputStateType> states = new HashSet<>();
	
	@Override
	public Set<? extends IHidDeviceInputState> states()
	{
		return this.states;
	}
}
