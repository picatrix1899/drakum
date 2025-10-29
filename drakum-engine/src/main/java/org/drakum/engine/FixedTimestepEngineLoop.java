package org.drakum.engine;

public class FixedTimestepEngineLoop implements IEngineLoop
{

	private IEngineRoutine routine;

	private double updateTime;
	
	private long lastTime;
	private double unprocessedTime;
	
	private long startTime;
	private long passedTime;
	
	private static final double SECOND = 1000000000.0;
	
	public FixedTimestepEngineLoop(int desiredUpdateRate)
	{
		this.updateTime = 1.0 / desiredUpdateRate;
	}
	
	@Override
	public void setRoutine(IEngineRoutine routine)
	{
		this.routine = routine;
	}

	@Override
	public void cycle()
	{
		this.startTime = System.nanoTime();
		this.passedTime = this.startTime - this.lastTime;
		this.lastTime = this.startTime;
		
		this.unprocessedTime += this.passedTime / SECOND;
		
		while(this.unprocessedTime >= this.updateTime)
		{
			routine.earlyUpdate();
			routine.update();
			routine.lateUpdate();
			
			this.unprocessedTime -= this.updateTime;	
		}
		
		routine.earlyRender();
		routine.render();
		routine.lateRender();
	}

	@Override
	public void init()
	{
		this.lastTime = System.nanoTime();
		this.unprocessedTime = 0.0;
	}

	@Override
	public void releaseResources()
	{
		
	}

}
