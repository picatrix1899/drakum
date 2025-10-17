package org.drakum;

import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.quaternion.QuatF;
import org.barghos.impl.math.vector.Vec3F;
import org.barghos.api.core.math.MathUtils;

public class Camera
{
	public Viewport3F viewport;
	public Vec3F pos = new Vec3F();
	
	public Mat4F proj = new Mat4F();
	
	public float pitch;
	public float yaw;
	
	public Camera(Vec3F pos, Viewport3F viewport)
	{
		pos(pos);
		
		viewport(viewport);
	}
	
	public Camera viewport(Viewport3F viewport)
	{
		this.viewport = viewport;
		
		this.proj.setPerspective(70.0f * MathUtils.DEG_TO_RADf, viewport.width / viewport.height, viewport.posZ, viewport.depth);
		
		return this;
	}
	
	public Camera pos(Vec3F pos)
	{
		this.pos.set(pos);
		
		return this;
	}
	
	public Camera pos(float x, float y, float z)
	{
		this.pos.set(x, y, z);
		
		return this;
	}
	
	public Vec3F pos()
	{
		return new Vec3F(this.pos);
	}
	
	public Mat4F projectionMatrix()
	{
		return this.proj;
	}
	
	public Vec3F forward()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, yaw).normalize();

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).nrm();
		
		return forward;
	}
	
	public Vec3F right()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, yaw).normalize();

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).nrm();
		
		Vec3F right = forward.crossN(0, 1, 0).nrm();
		
		return right;
	}
	
	public Mat4F viewMatrix()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, yaw).normalize();
		Mat4F mYaw = new Mat4F().setRotationByQuat(qYaw);

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).nrm();
		
		Vec3F right = forward.crossN(0, 1, 0).nrm();

		Mat4F mPitch = new Mat4F().setRotationRad(right.x(), right.y(), right.z(), pitch);
		
		Mat4F rot = mYaw.rMulN(mPitch);
		
		return Mat4F.translation3(pos.negN()).rMul(rot.transposeN());//.revMul(rot); // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
	}
}
