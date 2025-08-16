package org.drakum.old;

import org.barghos.math.matrix.Mat4F;

public class UniformMatrix4x4 extends Uniform
{
	
	public UniformMatrix4x4(String name)
	{
		super(name);
		
		addUniform("");
	}

	public void load(Mat4F matrix)
	{
		loadMatrix("", matrix);
	}
}
