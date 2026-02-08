package org.drakum.demo;

import org.barghos.impl.math.transform.Transform3F;

public interface IActorComponent
{
	public Transform3F localTransform(float alpha);
	public Transform3F worldTransform(float alpha);
}
