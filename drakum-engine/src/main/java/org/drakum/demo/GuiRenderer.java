package org.drakum.demo;

import static org.lwjgl.opengl.GL45C.*;

import org.barghos.glfw.window.GlfwWindow;
import org.drakum.Shader;
import org.drakum.Texture;
import org.drakum.TextureData;
import org.drakum.TextureLoader;
import org.drakum.TextureUtils;
import org.drakum.boilerplate.FFMGL;
import org.drakum.model.Quad;

public class GuiRenderer
{
	private Quad quad1;
	private Quad quad2;
	
	private Shader uiTextureShader;
	private Shader uiColorShader;
	
	private Texture tex;
	
	public void init()
	{
		this.uiTextureShader = new UiTextureShader();
		this.uiColorShader = new UiColorShader();
		
		this.quad1 = new Quad(200, 200, 300, 600);
		//this.quad2 = new Quad(200, 0, 100, 300);
		
		TextureData texData = TextureLoader.loadTexture("/res/materials/pac01.png");
		this.tex = TextureUtils.genTexture(texData);
	}
	
	public void render()
	{
		GlfwWindow window = Game.engine.window();
		
		glDisable(GL_DEPTH_TEST);
		
		int scissorX = 0;
		int scissorY = 0;
		int scissorWidth = 100;
		int scissorHeight = 90;
		
		//glEnable(GL_SCISSOR_TEST);
		//glScissor(scissorX, window.framebufferHeight() - (scissorY + scissorHeight), scissorWidth, scissorHeight);
		
//		uiColorShader.start();
//		uiColorShader.setVector2f("screenSpace", window.framebufferWidthf(), window.framebufferHeightf());
//		uiColorShader.setVector3f("color", 1.0f, 0.0f, 1.0f);
//		
//		this.quad1.vao.bind();
//		FFMGL.glEnableVertexAttribArray(0);
//		
//		this.quad1.draw();
		
		uiTextureShader.start();
		uiTextureShader.setVector2f("screenSpace", window.framebufferWidthf(), window.framebufferHeightf());
		uiTextureShader.setTexture("textureMap", this.tex);
		
		this.quad1.vao.bind();
		FFMGL.glEnableVertexAttribArray(0);
		FFMGL.glEnableVertexAttribArray(1);
		this.quad1.draw();
		
		glEnable(GL_DEPTH_TEST);
		//glDisable(GL_SCISSOR_TEST);
	}
	
	public void releaseResources()
	{
		uiTextureShader.releaseResources();
		uiColorShader.releaseResources();
		
		this.quad1.releaseResources();
		//this.quad2.releaseResources();
		
		this.tex.cleanup();
	}
}
