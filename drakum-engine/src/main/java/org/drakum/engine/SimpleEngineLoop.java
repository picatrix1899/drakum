package org.drakum.engine;

public class SimpleEngineLoop implements IEngineLoop
{

	private IEngineRoutine routine;
	
	@Override
	public void setRoutine(IEngineRoutine routine)
	{
		this.routine = routine;
	}

	@Override
	public void cycle()
	{
		routine.earlyUpdate();
		routine.update();
		routine.lateUpdate();
		
		routine.earlyRender();
		routine.render();
		routine.lateRender();
	}

	@Override
	public void init()
	{
		
	}

	@Override
	public void releaseResources()
	{
		
	}

}
