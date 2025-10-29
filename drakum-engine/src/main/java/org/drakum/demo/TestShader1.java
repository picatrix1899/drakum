package org.drakum.demo;

import org.drakum.Shader;

public class TestShader1 extends Shader
{

	public TestShader1()
	{
		vertexShader("/resources/testShader.vs");
		fragmentShader("/resources/testShader.fs");
		
		link();
	}

}
