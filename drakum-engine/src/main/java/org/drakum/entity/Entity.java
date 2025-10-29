package org.drakum.entity;

import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.transform.StaticTransformQuat3F;

public class Entity
{
	public final long id;
	public final long template;
	public final StaticTransformQuat3F localTransform = new StaticTransformQuat3F();
	
	public Entity(long id, long template)
	{
		this.id = id;
		this.template = template;
	}
	
	public Mat4F modelMatrix()
	{
		Mat4F scale = Mat4F.scaling3(this.localTransform.scale);
		Mat4F rot = Mat4F.rotationByQuat(this.localTransform.rot);
		Mat4F translate = Mat4F.translation3(this.localTransform.pos);
		
		return scale.rMul(rot).rMul(translate);
	}
}
