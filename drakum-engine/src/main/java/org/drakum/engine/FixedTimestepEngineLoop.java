package org.drakum.engine;

import org.barghos.core.time.TimeUtils;

public class FixedTimestepEngineLoop implements IEngineLoop
{

	private IEngineRoutine routine;

	private double updateTime;
	
	private long lastTime;
	private double unprocessedTime;
	
	private long startTime;
	private long passedTime;
	
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
		
		/*
		 * passedTime(ns)
		 * 
		 * unprocessedTime(s)
		 * 
		 * updateTime(s)
		 */
		
		this.unprocessedTime += this.passedTime / TimeUtils.NS_PER_Sd;
		
		while(this.unprocessedTime >= this.updateTime)
		{
			routine.earlyUpdate();
			routine.update();
			routine.lateUpdate();
			
			this.unprocessedTime -= this.updateTime;	
		}
		
		double alpha = this.unprocessedTime / this.updateTime;
		
		routine.earlyRender((float)alpha);
		routine.render((float)alpha);
		routine.lateRender((float)alpha);
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
