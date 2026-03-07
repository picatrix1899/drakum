package org.drakum.engine;

import java.util.concurrent.locks.LockSupport;

import org.barghos.core.math.MathUtils;
import org.barghos.core.time.TimeUtils;
import org.barghos.impl.core.Debug;

public class FixedTimestepEngineLoop implements IEngineLoop
{

	private IEngineRoutine routine;

	private double updateTime;
	
	private long lastTime;
	private double unprocessedTime;
	
	private long startTime;
	private long passedTime;
	
	private int framecap = 120;
	
	private double frameTime = 1.0 / framecap;      // seconds
	private long nextFrameNs = System.nanoTime();
	private long spinWindowNs = 700_000;            // ~0.7ms, nach Bedarf
	
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
		
		if (this.unprocessedTime > 0.25) this.unprocessedTime = 0.25;
		
		int counter = 0;
		
		if(this.unprocessedTime >= this.updateTime)
		{
			routine.earlyUpdate();
			
			while(this.unprocessedTime >= this.updateTime)
			{
				counter++;

				routine.update();
				
				this.unprocessedTime -= this.updateTime;	
				
				if(this.unprocessedTime >= this.updateTime)
				{
					this.unprocessedTime = 0;
				}
			}
			
			routine.lateUpdate();
		}
		
		
		if(counter > 1)
		{
			Debug.println(counter);
		}
		
		
		double alpha = this.unprocessedTime / this.updateTime;
		
		alpha = MathUtils.saturate(alpha);
		
		routine.earlyRender((float)alpha);
		routine.render((float)alpha);
		routine.lateRender((float)alpha);
		
		long targetDtNs = (long)(frameTime * 1_000_000_000L);
		
		if (nextFrameNs == 0L) nextFrameNs = System.nanoTime();
		
		nextFrameNs += targetDtNs;
		
		long t = System.nanoTime();

	    // Wenn wir zu spät sind: resync, sonst drift/ruckeln
//	    if (t > nextFrameNs) {
//	        nextFrameNs = t;
//	        return;
//	    }

		// Wenn wir zu spät sind: Catch-up (Frames überspringen, aber Takt beibehalten)
		if (t > nextFrameNs) {
		    // so lange vorwärts schieben, bis wir wieder in der Zukunft sind
		    do {
		        nextFrameNs += targetDtNs;
		    } while (nextFrameNs <= t);
		    // kein return!
		}
		
	    // grob schlafen bis kurz vor Deadline (optional, aber spart CPU)
	    while (true) {
	        t = System.nanoTime();
	        long remaining = nextFrameNs - t;
	        if (remaining <= spinWindowNs) break;

	        // nicht "remaining" schlafen, nur grob in Häppchen
	        LockSupport.parkNanos(remaining - spinWindowNs);
	       // try { Thread.sleep(1); } catch (InterruptedException ignored) {}
	    }

	    // fein warten bis Deadline (präzise)
	    while (System.nanoTime() < nextFrameNs) {
	        Thread.onSpinWait(); // optional
	    }
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
