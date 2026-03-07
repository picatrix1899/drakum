package org.drakum.advInput;

import org.barghos.hid.HidManager;

public interface ICondition
{
	boolean evaluate(HidManager hidManager);
}
