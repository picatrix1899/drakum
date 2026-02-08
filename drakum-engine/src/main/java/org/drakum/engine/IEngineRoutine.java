package org.drakum.engine;

public interface IEngineRoutine
{
	default void earlyInit() { }
	default void init() { }
	default void lateInit() { }
	
	default void earlyUpdate() { }
	default void update() { }
	default void lateUpdate() { }
	
	default void earlyRender(float alpha) { }
	default void render(float alpha) { }
	default void lateRender(float alpha) { }
	
	default void releaseResources() { }
}
