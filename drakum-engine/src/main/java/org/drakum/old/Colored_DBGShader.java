package org.drakum.old;

import static org.lwjgl.opengl.GL46C.*;

import java.io.InputStream;
import java.util.List;

import org.barghos.math.matrix.Mat4F;
import org.barghos.math.vector.Vec3F;
import org.barghos.util.tuple.Tup2;
import org.drakum.demo.App;

public class Colored_DBGShader extends DebugShader
{
	private ShaderPart vertex;
	private ShaderPart fragment;

	public Colored_DBGShader()
	{
		InputStream vertexStream = App.class.getResourceAsStream("/resources/shaders/debug/dbg_colored.vs");
		InputStream fragmentStream = App.class.getResourceAsStream("/resources/shaders/debug/dbg_colored.fs");
		
		ShaderPartData vertexData = ShaderPartLoader.readShader(vertexStream);
		ShaderPartData fragmentData = ShaderPartLoader.readShader(fragmentStream);
		
		
		vertex = new ShaderPart().loadShader("vertex", vertexData, GL_VERTEX_SHADER);
		fragment = new ShaderPart().loadShader("fragment", fragmentData, GL_FRAGMENT_SHADER);
		
		
		
		compile();
		getAllUniformLocations();
		
	}
	
	protected void getAllUniformLocations()
	{
		addUniform("T_model");
		addUniform("T_view");
		addUniform("T_projection");
		
		addUniform("color");
	}

	public void loadViewMatrix(Mat4F m) { setInput("T_view", m); }
	
	public void loadModelMatrix(Mat4F m) { setInput("T_model", m); }
	
	public void loadProjectionMatrix(Mat4F m) { setInput("T_projection", m); }
	
	public void loadColor(Vec3F c) { setInput("color", c); }
	
	
	private void loadMatrices0(Mat4F view, Mat4F model, Mat4F proj)
	{
		loadMatrix("T_model", model != null ? model : new Mat4F().setIdentity());
		//loadMatrix("T_view", view);
		loadMatrix("T_projection", proj);
	}
	
	public void use()
	{
		start();
		loadMatrices0(getInput("T_view"), getInput("T_model"), getInput("T_projection"));
		//loadColor3("color", getInput("color"));
	}

	public void attachShaderParts()
	{
		attachVertexShader(vertex);
		attachFragmentShader(fragment);
	}

	public void getAttribs(List<Tup2<Integer,String>> attribs)
	{
		attribs.add(new Tup2<Integer,String>(0,"vertexPos"));
	}
	
	@Override
	public void cleanup()
	{
		super.cleanup();
		
		this.vertex.clear();
		this.fragment.clear();
	}
}
