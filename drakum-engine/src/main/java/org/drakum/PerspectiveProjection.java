package org.drakum;

import org.barghos.impl.math.matrix.Mat4F;
import org.drakum.demo.Shader2;

public class PerspectiveProjection
{
	public Mat4F proj = new Mat4F();
	
	public void set(float fovY, float aspect, float near, float far)
	{
		this.proj.setPerspective(fovY, aspect, near, far);
	}
	
	public void uploadToShader(Shader shader)
	{
		shader.setProj(proj);
	}
	
	public void uploadToShader(Shader2 shader)
	{
		shader.setProj(proj);
	}
}
