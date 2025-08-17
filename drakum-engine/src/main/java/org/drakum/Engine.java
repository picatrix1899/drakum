package org.drakum;

import org.drakum.demo.Routine;
import org.drakum.hid.HidManager;
import org.drakum.rhi.RenderApi;
import org.drakum.rhi.ogl.OglRhiApi;

public class Engine implements IEngineLoopListener
{
	private static Engine instance;
	
	public static Engine instance()
	{
		if(instance == null) instance = new Engine();
		
		return instance;
	}
	
	private IEngineLoop engineLoop;
	
	private Routine routine;
	
	private Engine() {}
	
	public void start()
	{
		this.routine = new Routine();
		
		this.engineLoop = new SimpleEngineLoop();
		this.engineLoop.start(this);
	}
	
	public void stop()
	{
		this.engineLoop.stop();
	}
	
	@Override
	public void preInit()
	{
		RenderApi.api(new OglRhiApi());
	}
	
	@Override
	public void init()
	{
		this.routine.init();
	}
	
	@Override
	public void preRender()
	{
		this.routine.preRender();
	}
	
	@Override
	public void render()
	{
		this.routine.render();
	}

	@Override
	public void postRender()
	{
		this.routine.postRender();
	}
	
	@Override
	public void preUpdate()
	{
		HidManager.update();
	}

	@Override
	public void freeResources()
	{
		this.routine.freeResources();
		
		RenderApi.freeResources();
	}
}
