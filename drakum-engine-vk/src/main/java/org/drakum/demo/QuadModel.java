package org.drakum.demo;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL46C.*;

import org.drakum.Vao;

public class QuadModel
{
	private Vao vao;
	
	public QuadModel()
	{
		float vertices[] = {
		         0.5f,  0.5f, 0.0f,  // top right
		         0.5f, -0.5f, 0.0f,  // bottom right
		        -0.5f, -0.5f, 0.0f,  // bottom left
		        -0.5f,  0.5f, 0.0f   // top left 
	    };
		int indices[] = {  // note that we start from 0!
	        0, 1, 3,  // first Triangle
	        1, 2, 3   // second Triangle
	    };
		
		vao = new Vao();
		vao.storeFloatData(0, 3, vertices, 0, 0, GL_STATIC_DRAW);
		vao.storeIndices(indices, GL_STATIC_DRAW);
	}
	
	public void bind()
	{
		this.vao.bind();
		
		glEnableVertexAttribArray(0);
	}
	
	public void draw()
	{
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
	}
	
	public void releaseResources()
	{
		this.vao.releaseResources();
	}
}
