package org.drakum.old;

import org.barghos.math.vector.Vec2F;

public class UniformVector2 extends Uniform
{

	public UniformVector2(String name)
	{
		super(name);
		
		addUniform("");
	}

	public void load(Vec2F vector)
	{
		loadVector2("", vector);
	}
	
}
