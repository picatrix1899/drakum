package org.drakum.old;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.barghos.math.matrix.Mat4F;
import org.barghos.math.vector.Vec2F;
import org.barghos.math.vector.Vec3F;
import org.barghos.util.nio.buffer.FloatBufferUtils;
import org.barghos.util.tuple.Tup2;
import org.barghos.util.tuple.floats.Tup4F;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;


public abstract class ShaderProgram
{
	protected int programID;
	
	private List<ShaderPart> geometryShaders = new ArrayList<>();
	private List<ShaderPart> vertexShaders = new ArrayList<>();
	private List<ShaderPart> fragmentShaders = new ArrayList<>();
	
	private Map<String,Integer> uniforms = new HashMap<>();
	
	private Map<String,Object> inputs = new HashMap<>();
	
	private List<Uniform> newUniforms = new ArrayList<>();
	
	public void setInput(String name, Object val)
	{
		this.inputs.put(name, val);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getInput(String name)
	{
		return (T) this.inputs.get(name);
	}
	
	public void addUniform(Uniform uniform)
	{
		this.newUniforms.add(uniform);
	}
	
	public void compile()
	{
		this.programID = GL20.glCreateProgram();

		attachShaderParts();
		
		for(ShaderPart p : this.geometryShaders)
			GL20.glAttachShader(programID, p.getId());
		
		for(ShaderPart p  : this.vertexShaders)
			GL20.glAttachShader(programID, p.getId());

		for(ShaderPart p  : this.fragmentShaders)
			GL20.glAttachShader(programID, p.getId());

		List<Tup2<Integer,String>> attribs = new ArrayList<>();
		
		getAttribs(attribs);
		
		for(Tup2<Integer,String> attrib : attribs)
		{
			bindAttribute(attrib.v0(), attrib.v1());
		}
		
		GL20.glLinkProgram(this.programID);
		
		GL20.glValidateProgram(this.programID);
	}
	
	protected void attachGeometryShader(ShaderPart part)
	{
		this.geometryShaders.add(part);
	}
	
	protected void attachFragmentShader(ShaderPart part)
	{
		this.fragmentShaders.add(part);
	}
	
	protected void attachVertexShader(ShaderPart part)
	{
		this.vertexShaders.add(part);
	}
	
	public abstract void attachShaderParts();
	
	public abstract void getAttribs(List<Tup2<Integer,String>> attribs);
	
	public void start()
	{
		GL20.glUseProgram(this.programID);
	}
	
	public void stop()
	{
		GL20.glUseProgram(0);
	}
	
	public void cleanup()
	{
		GL20.glUseProgram(0);
		
		for(ShaderPart p : this.geometryShaders)
			GL20.glDetachShader(this.programID, p.getId());
		
		for(ShaderPart p : this.vertexShaders)
			GL20.glDetachShader(this.programID, p.getId());
		
		for(ShaderPart p : this.fragmentShaders)
			GL20.glDetachShader(this.programID, p.getId());
		
		GL20.glDeleteProgram(this.programID);
		
		this.geometryShaders.clear();
		this.vertexShaders.clear();
		this.fragmentShaders.clear();
	}
	
	protected void bindAttribute(int attrib, String var)
	{
		GL20.glBindAttribLocation(this.programID, attrib, var);
	}
	
	protected int getUniformLocation(String uniform)
	{
		return GL20.glGetUniformLocation(this.programID, uniform);
	}
	
	protected void addUniform(String uniform)
	{
		this.uniforms.put(uniform, GL20.glGetUniformLocation(this.programID, uniform));
	}
	
	
	protected void getAllUniformLocations()
	{
		for(Uniform u : this.newUniforms)
		{
			u.getUniformLocations(this);
		}
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
		FloatBuffer matrixBuffer = FloatBufferUtils.directFromFloat(true, val.toArrayColumnMajor());
		
		GL20.glUniformMatrix4fv(location, false, matrixBuffer);
	}
	
	protected void loadFloat(String uniform, float val) { loadFloat(this.uniforms.get(uniform), val); }
	
	protected void loadVector2(String uniform, Vec2F val) { loadVector2(this.uniforms.get(uniform),val); }
	
	protected void loadColor3(String uniform, Vec3F val) { loadColor3(this.uniforms.get(uniform), val); }
	
	protected void loadVector3(String uniform, Vec3F val) { loadVector3(this.uniforms.get(uniform), val); }
	
	protected void loadVector4(String uniform, Tup4F val) { loadVector4(this.uniforms.get(uniform), val); }
	
	protected void loadBoolean(String uniform, boolean val) { loadBoolean(this.uniforms.get(uniform), val); }
	
	protected void loadInt(String uniform, int val) { loadInt(this.uniforms.get(uniform), val); }
	
	protected void loadMatrix(String uniform, Mat4F val) { loadMatrix(this.uniforms.get(uniform), val); }
	
	protected void loadTextureId(String uniform, int attrib, int texture) { loadTextureId(this.uniforms.get(uniform), attrib, texture); }
	
	protected void loadTexture(String uniform, int attrib, Texture texture) { loadTexture(this.uniforms.get(uniform), attrib, texture); }
	
	protected void loadTextureMSId(String uniform, int attrib, int texture) { loadTextureMSId(this.uniforms.get(uniform), attrib, texture); }
	
	protected void loadTextureMS(String uniform, int attrib, Texture texture) { loadTextureMS(this.uniforms.get(uniform), attrib, texture); }
}
