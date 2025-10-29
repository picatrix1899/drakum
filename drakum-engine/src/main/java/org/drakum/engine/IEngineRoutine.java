package org.drakum.engine;

public interface IEngineRoutine
{
	default void earlyInit() { }
	default void init() { }
	default void lateInit() { }
	
	default void earlyUpdate() { }
	default void update() { }
	default void lateUpdate() { }
	
	default void earlyRender() { }
	default void render() { }
	default void lateRender() { }
	
	default void releaseResources() { }
}
