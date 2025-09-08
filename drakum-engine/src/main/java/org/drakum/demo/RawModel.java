package org.drakum.demo;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;

public class RawModel
{
	
	private Vao vao;
	private int vertexCount;

	public RawModel(Vao vao, int vertexCount)
	{
		this.vao = vao;
		this.vertexCount = vertexCount;
	}
	
	
	public Vao getVAO() { return this.vao; }

	public int getVertexCount() { return vertexCount; }
	
	public void bind()
	{
		this.vao.bind();
		
		glEnableVertexAttribArray(0);
	}
	
	public void draw()
	{
		glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
	}
}
