package org.drakum.demo;

import org.drakum.Shader;

public class UiTextureShader extends Shader
{

	public UiTextureShader()
	{
		vertexShader("/resources/shaders/gui/gui_texture.vs");
		fragmentShader("/resources/shaders/gui/gui_texture.fs");
		
		link();
	}

}
