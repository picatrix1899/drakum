package org.drakum.demo;

import org.barghos.api.math.transform.LerpTransformOpsI3F;
import org.barghos.api.math.transform.TransformOpsI3F;
import org.barghos.impl.math.transform.Transform3F;
import org.drakum.PerspectiveProjection;

public class Camera implements IActorComponent
{
	public IActorComponent parent;
	public final Transform3F previousLocalTransform = new Transform3F();
	public final Transform3F localTransform = new Transform3F();
	
	public PerspectiveProjection projection = new PerspectiveProjection();
	
	public Camera()
	{
	}

	public void swapTransforms()
	{
		this.previousLocalTransform.set(this.localTransform);
	}
	
	@Override
	public Transform3F localTransform(float alpha)
	{
		return this.localTransform;
	}

	@Override
	public Transform3F worldTransform(float alpha)
	{
		Transform3F local = LerpTransformOpsI3F.lerp(alpha, previousLocalTransform, localTransform, new Transform3F());
		
		if(this.parent == null)
		{
			return local;
		}
		
		Transform3F parentTransform = this.parent.worldTransform(alpha);

		Transform3F worldTransform = TransformOpsI3F.mul(local, parentTransform, new Transform3F());
		
		return worldTransform;
	}
	
	
}
