package org.drakum.old;

import java.util.HashMap;
import java.util.Map;

import org.barghos.math.matrix.Mat4F;
import org.barghos.math.vector.Vec2F;
import org.barghos.math.vector.Vec3F;
import org.barghos.util.nio.buffer.FloatBufferUtils;
import org.barghos.util.tuple.floats.Tup4F;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public abstract class Uniform
{
	protected String name;
	
	private Map<String,Integer> uniforms = new HashMap<>();
	
	public Uniform(String name)
	{
		this.name = name;
	}
	
	public void getUniformLocations(ShaderProgram shader)
	{
		for(String uniform : this.uniforms.keySet())
		{
			this.uniforms.put(uniform, GL20.glGetUniformLocation(shader.programID, uniform));
		}
	}
	
	protected void addUniform(String uniform)
	{
		this.uniforms.put(this.name + "." + uniform, 0);
	}
	
	protected void loadTextureId(int location, int attrib, int texture)
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + attrib);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		loadInt(location, attrib);
	}
	
	protected void loadTexture(int location, int attrib, Texture texture)
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + attrib);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		loadInt(location, attrib);
	}
	
	protected void loadTextureMSId(int location, int attrib, int texture)
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + attrib);
		GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, texture);
		loadInt(location, attrib);
	}
	
	protected void loadTextureMS(int location, int attrib, Texture texture)
	{
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + attrib);
		GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, texture.getId());
		loadInt(location, attrib);
	}
	
	protected void loadFloat(int location, float val)
	{
		GL20.glUniform1f(location, val);
	}
	
	protected void loadColor3(int location, Vec3F val)
	{
		GL20.glUniform3f(location, val.x(), val.y(), val.z());
	}
	
	protected void loadVector2(int location, Vec2F val)
	{
		GL20.glUniform2f(location,val.x(), val.y());
	}
	
	protected void loadVector3(int location, Vec3F val)
	{
		GL20.glUniform3f(location, val.x(), val.y(), val.z());
	}
	
	protected void loadVector4(int location, Tup4F val)
	{
		GL20.glUniform4f(location, val.v0(), val.v1(), val.v2(), val.v3());
	}
	
	protected void loadBoolean(int location, boolean val)
	{
		loadFloat(location, val ? 1 : 0);
	}
	
	protected void loadInt(int location, int val)
	{
		GL20.glUniform1i(location, val);
	}
	

	protected void loadMatrix(int location, Mat4F val)
	{
		GL20.glUniformMatrix4fv(location, false, FloatBufferUtils.directFromFloat(true, val.toArrayColumnMajor()));
	}
	
	protected void loadFloat(String uniform, float val) { loadFloat(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadVector2(String uniform, Vec2F val) { loadVector2(this.uniforms.get(this.name + "." + uniform),val); }
	
	protected void loadColor3(String uniform, Vec3F val) { loadColor3(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadVector3(String uniform, Vec3F val) { loadVector3(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadVector4(String uniform, Tup4F val) { loadVector4(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadBoolean(String uniform, boolean val) { loadBoolean(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadInt(String uniform, int val) { loadInt(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadMatrix(String uniform, Mat4F val) { loadMatrix(this.uniforms.get(this.name + "." + uniform), val); }
	
	protected void loadTextureId(String uniform, int attrib, int texture) { loadTextureId(this.uniforms.get(this.name + "." + uniform), attrib, texture); }
	
	protected void loadTexture(String uniform, int attrib, Texture texture) { loadTexture(this.uniforms.get(this.name + "." + uniform), attrib, texture); }
	
	protected void loadTextureMSId(String uniform, int attrib, int texture) { loadTextureMSId(this.uniforms.get(this.name + "." + uniform), attrib, texture); }
	
	protected void loadTextureMS(String uniform, int attrib, Texture texture) { loadTextureMS(this.uniforms.get(this.name + uniform), attrib, texture); }
}
