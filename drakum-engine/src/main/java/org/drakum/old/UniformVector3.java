package org.drakum.old;

import org.barghos.math.vector.Vec3F;

public class UniformVector3 extends Uniform
{

	public UniformVector3(String name)
	{
		super(name);
		
		addUniform("");
	}

	public void load(Vec3F vector)
	{
		loadVector3("", vector);
	}
	
}
