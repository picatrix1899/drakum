package org.drakum;

import org.barghos.math.matrix.Mat4F;
import org.barghos.math.vector.Vec3F;
import org.barghos.util.math.MathUtils;

public class Camera
{
	public Mat4F proj = new Mat4F().setPerspective(70.0f * MathUtils.DEG_TO_RADf, 800.0f / 600.0f, 0.1f, 1000.0f);
	public Vec3F pos = new Vec3F();
	
	public Camera()
	{
		proj = new Mat4F().setPerspective(70.0f * MathUtils.DEG_TO_RADf, 800.0f / 600.0f, 0.1f, 1000.0f);
	}
	
	public Mat4F projectionMatrix()
	{
		return this.proj;
	}
	
	public Mat4F viewMatrix()
	{
		return Mat4F.translation3(pos); // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
	}
}
