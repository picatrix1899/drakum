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
		
		routine.earlyRender(1);
		routine.render(1);
		routine.lateRender(1);
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
