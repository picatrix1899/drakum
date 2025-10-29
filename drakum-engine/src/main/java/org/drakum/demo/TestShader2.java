package org.drakum.demo;

import org.drakum.Shader;

public class TestShader2 extends Shader
{

	public TestShader2()
	{
		vertexShader("/resources/testShader2.vs");
		fragmentShader("/resources/testShader2.fs");
		
		link();
	}

}
