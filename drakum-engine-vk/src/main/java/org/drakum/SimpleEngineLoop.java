package org.drakum;

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
		routine.preTick();
		routine.tick();
		routine.postTick();
		
		routine.preRender();
		routine.render();
		routine.postRender();
	}

}
