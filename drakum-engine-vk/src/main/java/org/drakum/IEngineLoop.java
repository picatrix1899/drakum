package org.drakum;

public interface IEngineLoop
{
	public void setRoutine(IEngineRoutine routine);
	
	public void cycle();
}
