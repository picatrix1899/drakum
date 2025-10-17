package org.drakum;

public interface IEngineRoutine
{
	default void preInit() { }
	default void init() { }
	default void postInit() { }
	
	default void preTick() { }
	default void tick() { }
	default void postTick() { }
	
	default void preRender() { }
	default void render() { }
	default void postRender() { }
	
	default void releaseResources() { }
}
