package org.drakum;

import org.barghos.api.math.matrix.DefaultMatsI4F;
import org.barghos.impl.math.matrix.Mat4F;

public class PerspectiveProjection
{
	public Mat4F proj = new Mat4F();
	
	public void set(float fovY, float aspect, float near, float far)
	{
		DefaultMatsI4F.perspectiveProjectionDeg(fovY, aspect, near, far, proj);
	}
	
	public void uploadToShader(Shader shader)
	{
		shader.setMat4f("m_proj", proj);
	}
}
