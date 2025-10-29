package org.drakum.engine;

public interface IEngineLoop
{
	public void setRoutine(IEngineRoutine routine);
	
	public void init();
	
	public void cycle();
	
	public void releaseResources();
}
