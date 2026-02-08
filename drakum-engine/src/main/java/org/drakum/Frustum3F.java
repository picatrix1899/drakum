package org.drakum;

import org.barghos.core.math.MathUtils;
import org.barghos.impl.math.matrix.Mat4F;

public class Frustum3F
{
	public final float[] f = new float[6];
	
	public Frustum3F()
	{
		this.f[0] = -1;
		this.f[1] = 1;
		this.f[2] = -1;
		this.f[3] = 1;
		this.f[4] = 0;
		this.f[5] = 1;
	}
	
	public Frustum3F(float left, float right, float bottom, float top, float near, float far)
	{
		this.f[0] = left;
		this.f[1] = right;
		this.f[2] = bottom;
		this.f[3] = top;
		this.f[4] = near;
		this.f[5] = far;
	}
	
	public Frustum3F set(float[] f)
	{
		System.arraycopy(f, 0, this.f, 0, 6);
		
		return this;
	}
	
	public Frustum3F set(float left, float right, float bottom, float top, float near, float far)
	{
		this.f[0] = left;
		this.f[1] = right;
		this.f[2] = bottom;
		this.f[3] = top;
		this.f[4] = near;
		this.f[5] = far;
		
		return this;
	}
	
	public Frustum3F setFromFovYAspectNearFar(float fovY, float aspect, float near, float far)
	{
		float top = MathUtils.tan(fovY * 0.5f) * near;
		float right = top * aspect;
		
		this.f[0] = -right;
		this.f[1] = right;
		this.f[2] = -top;
		this.f[3] = top;
		this.f[4] = near;
		this.f[5] = far;
		
		return this;
	}
	
	public Frustum3F setFromWidthHeightNearFar(float width, float height, float near, float far)
	{
		float halfW = width * 0.5f;
		float halfH = height * 0.5f;
		
		this.f[0] = -halfW;
		this.f[1] = halfW;
		this.f[2] = -halfH;
		this.f[3] = halfH;
		this.f[4] = near;
		this.f[5] = far;
		
		return this;
	}
	
	public float left()
	{
		return this.f[0];
	}
	
	public Frustum3F left(float left)
	{
		this.f[0] = left;
		
		return this;
	}
	
	public float right()
	{
		return this.f[1];
	}
	
	public Frustum3F right(float right)
	{
		this.f[1] = right;
		
		return this;
	}
	
	public float bottom()
	{
		return this.f[2];
	}
	
	public Frustum3F bottom(float bottom)
	{
		this.f[2] = bottom;
		
		return this;
	}
	
	public float top()
	{
		return this.f[3];
	}
	
	public Frustum3F top(float top)
	{
		this.f[3] = top;
		
		return this;
	}
	
	public float near()
	{
		return this.f[4];
	}
	
	public Frustum3F near(float near)
	{
		this.f[4] = near;
		
		return this;
	}
	
	public float far()
	{
		return this.f[5];
	}
	
	public Frustum3F far(float far)
	{
		this.f[5] = far;
		
		return this;
	}
	
	public Mat4F toOrthographicProjectionMatrix()
	{
		Mat4F m = new Mat4F();
		
		float left = this.f[0];
		float right = this.f[1];
		float bottom = this.f[2];
		float top = this.f[3];
		float near = this.f[4];
		float far = this.f[5];
		
		float rightMinusLeft = right - left;
		float topMinusBottom = top - bottom;
		float farMinusNear = far - near;
		
		m.set(
			2.0f / rightMinusLeft, 0f, 0f, 0f,
			0f, 2.0f / topMinusBottom, 0f, 0f,
			0f, 0f, -2.0f / farMinusNear, 0f,
			-(right + left) / rightMinusLeft, -(top + bottom) / topMinusBottom, -(far + near) / farMinusNear, 1.0f);
		 
		return m;
	}
	
	public Mat4F toPerspectiveProjectionMatrix()
	{
		Mat4F m = new Mat4F();
		
		float left = this.f[0];
		float right = this.f[1];
		float bottom = this.f[2];
		float top = this.f[3];
		float near = this.f[4];
		float far = this.f[5];
		
		float rightMinusLeft = right - left;
		float topMinusBottom = top - bottom;
		float farMinusNear = far - near;
		float twoNear = 2 * near;
		
		m.set(
			twoNear / rightMinusLeft, 0f, 0f, 0f,
			
			0f, twoNear / topMinusBottom, 0f, 0f,
			
			(right + left) / rightMinusLeft, (top + bottom) / topMinusBottom, -(far + near) / farMinusNear, -1f,
			
			0f, 0f, -twoNear * far / farMinusNear, 0f);
		 
		return m;
	}
	
	public String toString()
	{
		return "frustum3f(left=" + this.f[0] + ", right=" + this.f[1] + ", bottom=" + this.f[2] + ", top=" + this.f[3] + ", near=" + this.f[4] + ", far=" + this.f[5] + ")";
	}
	
	public static Frustum3F fromFovYAspectNearFar(float fovY, float aspect, float near, float far)
	{
		float top = MathUtils.tan(fovY * 0.5f) * near;
		float right = top * aspect;
		
		return new Frustum3F(-right, right, -top, top, near, far);
	}
	
	public static Frustum3F fromWidthHeightNearFar(float width, float height, float near, float far)
	{
		float halfW = width * 0.5f;
		float halfH = height * 0.5f;
		
		return new Frustum3F(-halfW, halfW, -halfH, halfH, near, far);
	}
}
