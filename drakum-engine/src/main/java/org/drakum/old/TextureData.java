package org.drakum.old;

public class TextureData
{
	private byte[] data;
	private int width;
	private int height;
	
	public TextureData(byte[] data, int width, int height)
	{
		this.data = data;
		this.width = width;
		this.height = height;
	}
	
	public byte[] data()
	{
		return this.data;
	}
	
	public int width()
	{
		return this.width;
	}
	
	public int height()
	{
		return this.height;
	}
}
