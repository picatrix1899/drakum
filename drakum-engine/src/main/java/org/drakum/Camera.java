package org.drakum;

import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.quaternion.QuatF;
import org.barghos.impl.math.transform.StaticTransformEuler3F;
import org.barghos.impl.math.vector.Vec3F;

public class Camera
{
	public final StaticTransformEuler3F transform = new StaticTransformEuler3F();
	
	public PerspectiveProjection projection = new PerspectiveProjection();
	
	public Camera(Vec3F pos)
	{
		this.transform.pos.set(pos);
	}
	
	public Camera pos(Vec3F pos)
	{
		this.transform.pos.set(pos);

		return this;
	}
	
	public Camera pos(float x, float y, float z)
	{
		this.transform.pos.set(x, y, z);

		return this;
	}
	
	public Camera move(Vec3F pos)
	{
		this.transform.pos.add(pos);

		return this;
	}
	
	public Camera move(float x, float y, float z)
	{
		this.transform.pos.add(x, y, z);

		return this;
	}
	
	public Vec3F pos()
	{
		return new Vec3F(this.transform.pos);
	}
	
	public Vec3F rot()
	{
		return new Vec3F(this.transform.rot);
	}
	
	public Camera rot(float pitch, float yaw, float roll)
	{
		this.transform.rot.set(pitch, yaw, roll);
		
		return this;
	}
	
	public Camera rotate(float pitch, float yaw, float roll)
	{
		this.transform.rot.add(pitch, yaw, roll);
		
		return this;
	}
	
	public Vec3F forward()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, this.transform.rot.v1()).normalize();

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).normalize();
		
		return forward;
	}
	
	public Vec3F right()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, this.transform.rot.v1()).normalize();

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).normalize();
		
		Vec3F right = forward.crossN(0, 1, 0).normalize();
		
		return right;
	}

	public Mat4F viewMatrix()
	{
		QuatF qYaw = new QuatF().setFromAxisAngle(0, 1, 0, this.transform.rot.v1()).normalize();
		Mat4F mYaw = new Mat4F().setRotationByQuat(qYaw);

		Vec3F forward = qYaw.transformT(0, 0, -1, new Vec3F()).normalize();
		
		Vec3F right = forward.crossN(0, 1, 0).normalize();

		Mat4F mPitch = new Mat4F().setRotationRad(right, this.transform.rot.v0());
		
		Mat4F rot = mYaw.rMulN(mPitch);
		
		return Mat4F.translation3(this.transform.pos.negateN()).rMul(rot.transposeN());//.revMul(rot); // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
	}
}
