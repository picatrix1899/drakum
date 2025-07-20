package org.drakum.demo;

import org.drakum.demo.vkn.VknImage2D;
import org.drakum.demo.vkn.VknImageView2D;
import org.drakum.demo.vkn.VknMemory;

public class Texture
{
	public VknImage2D texture;
	public VknImageView2D textureView;
	public VknMemory textureMemory;
	
	public int width;
	public int height;
	public int format;
	
	public Texture()
	{
		
	}
	
	public VknImage2D image()
	{
		return this.texture;
	}
	
	public VknImageView2D imageView()
	{
		return this.textureView;
	}
	
	public VknMemory memory()
	{
		return this.textureMemory;
	}
	
	public int width()
	{
		return this.width;
	}
	
	public int height()
	{
		return this.height;
	}
	
	public int format()
	{
		return this.format;
	}
	
	public void close()
	{
		this.textureView.close();
		this.texture.close();
		this.textureMemory.close();
	}
}
