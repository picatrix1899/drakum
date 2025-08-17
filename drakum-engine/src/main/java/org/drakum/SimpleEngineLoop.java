package org.drakum;

public class SimpleEngineLoop implements IEngineLoop
{
	private IEngineLoopListener listener;
	private boolean isRunning;
	
	@Override
	public void start(IEngineLoopListener listener)
	{
		if(this.isRunning) throw new Error("Engine already running");
		
		this.listener = listener;
		
		this.isRunning = true;
		
		run();
	}

	@Override
	public void stop()
	{
		this.isRunning = false;
	}

	@Override
	public boolean isRunning()
	{
		return this.isRunning;
	}
	
	public void run()
	{
		this.listener.preInit();
		this.listener.init();
		this.listener.postInit();
		
		while(this.isRunning)
		{
			this.listener.preUpdate();
			this.listener.update();
			this.listener.postUpdate();
			
			this.listener.preRender();
			this.listener.render();
			this.listener.postRender();
		}
		
		freeResources();
	}
	
	public void freeResources()
	{
		this.listener.freeResources();
	}
	
}
