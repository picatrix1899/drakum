package org.drakum.demo;

import org.drakum.Shader;

public class UiColorShader extends Shader
{

	public UiColorShader()
	{
		vertexShader("/resources/shaders/gui/gui_color.vs");
		fragmentShader("/resources/shaders/gui/gui_color.fs");
		
		link();
	}

}
