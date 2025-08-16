package org.drakum.old;

public class UniformTexture extends Uniform
{

	public UniformTexture(String name)
	{
		super(name);
		
		addUniform("");
	}

	public void load(Texture texture, int attrib)
	{
		loadTexture("", attrib, texture);
	}
	
	public void load(int texture, int attrib)
	{
		loadTextureId("", attrib, texture);
	}
	
}
