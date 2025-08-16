package org.drakum.old;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class Texture
{
	
	private int id;
	
	private int width;
	private int height;
	private boolean wrap;
	
	
	public Texture(int id, int width, int height, boolean wrap)
	{
		this.id = id;
		this.width = width;
		this.height = height;
		this.wrap = wrap;
	}
	
	public static Texture createDepthTexture(int width, int height)
	{
		int id = GL11.glGenTextures();
		
		BindingUtils.bindTexture2D(id);
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		return new Texture(id, width, height, false);
	}
	
	public static Texture createTexture(int width, int height, boolean hdr)
	{
		int id = GL11.glGenTextures();
		
		BindingUtils.bindTexture2D(id);
		
		if(hdr)
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,  GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		else
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,  GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);	
			
		return new Texture(id, width, height, false);
	}
	
	public int getId() { return this.id; }
	
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	
	public boolean getWrap() { return this.wrap; }
	
	public void cleanup() { GL11.glDeleteTextures(this.id); }
}
