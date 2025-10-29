package org.drakum;

import org.barghos.impl.math.matrix.Mat4F;

public class PerspectiveProjection
{
	public Mat4F proj = new Mat4F();
	
	public void set(float fovY, float aspect, float near, float far)
	{
		this.proj.setPerspective(fovY, aspect, near, far);
	}
	
	public void uploadToShader(Shader shader)
	{
		shader.setMat4f("m_proj", proj);
	}
}
