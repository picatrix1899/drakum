package org.drakum;

public interface IEngineLoop
{
	void start(IEngineLoopListener listener);
	void stop();
	boolean isRunning();
}
