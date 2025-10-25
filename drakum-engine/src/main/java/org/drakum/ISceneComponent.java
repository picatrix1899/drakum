package org.drakum;

public interface ISceneComponent
{
	void setParent(ISceneComponent parent);
	void getLocalTransform();
	void getGlobalTransform();
}
