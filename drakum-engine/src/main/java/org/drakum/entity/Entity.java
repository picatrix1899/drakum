package org.drakum.entity;

import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.transform.Transform3F;

import org.barghos.api.math.matrix.DefaultMatsI4F;
import org.barghos.api.math.transform.LerpTransformOpsI3F;
import org.barghos.api.math.transform.TransformOpsI3F;

public class Entity
{
	public final long id;
	public final long template;
	public final Transform3F previousLocalTransform = new Transform3F();
	public final Transform3F localTransform = new Transform3F();
	
	public Entity(long id, long template)
	{
		this.id = id;
		this.template = template;
	}
	
	public void swapTransforms()
	{
		this.previousLocalTransform.set(this.localTransform);
	}
	
	public Mat4F modelMatrix(float alpha)
	{
		Transform3F worldTransform = worldTransform(alpha);
		
		return DefaultMatsI4F.modelTRS(worldTransform, new Mat4F());
	}
	
	public Transform3F worldTransform(float alpha)
	{
//		Transform3F local = LerpTransformOpsI3F.lerp(alpha, previousLocalTransform, localTransform, new Transform3F());
//
//		return local;
		
		return this.localTransform;
	}
	
}
