package org.drakum.old;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class BindingUtils
{
	public static void bindTexture2D(int texture) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture); }
	
	public static void bindVAO(VAO vao, int... attribs)
	{
		GL30.glBindVertexArray(vao.getID());
		
		for(int i : attribs)
			GL20.glEnableVertexAttribArray(i);
	}
	
	public static void bindVAO(int vao, int...attribs)
	{
		GL30.glBindVertexArray(vao);
		
		for(int i : attribs)
			GL20.glEnableVertexAttribArray(i);
	}
}
