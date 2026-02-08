package org.drakum.entity;

import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.transform.Transform3F;

import org.barghos.api.math.matrix.DefaultMatsI4F;

public class Entity
{
	public final long id;
	public final long template;
	public final Transform3F localTransform = new Transform3F();
	
	public Entity(long id, long template)
	{
		this.id = id;
		this.template = template;
	}
	
	public Mat4F modelMatrix()
	{
		return DefaultMatsI4F.modelTRS(this.localTransform, new Mat4F());
	}
}
