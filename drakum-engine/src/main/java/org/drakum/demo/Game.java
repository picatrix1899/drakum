package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL46C.*;

import java.io.File;

import org.barghos.math.matrix.Mat4F;
import org.barghos.util.math.MathUtils;
import org.drakum.Engine;
import org.drakum.IEngineRoutine;
import org.drakum.Mesh;
import org.drakum.OBJFile;
import org.drakum.RawModel;
import org.drakum.Shader;
import org.drakum.SimpleEngineLoop;
import org.drakum.Window;

public class Game implements IEngineRoutine
{
	private Shader shader;

	private Window window;
	
	private RawModel rawModel;
	
	public static Engine engine;
	
	public Game()
	{
		Engine engine = new Engine();
		engine.setLoop(new SimpleEngineLoop());
		engine.setRoutine(this);
		
		Game.engine = engine;
	}
	
	public void start()
	{
		engine.start();
	}
	
	public void stop()
	{
		engine.stop();
	}
	
	@Override
	public void preInit()
	{
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		window = new Window(800, 600, "Test");
		
		shader = new Shader();
		
		OBJFile obj = new OBJFile();
		obj.load(new File("res/models/dragon.obj"));
		Mesh mesh = new Mesh();
		mesh.loadFromObj(obj);
		rawModel = new RawModel(mesh.vao, mesh.getVertexCount());
		
		window.show();
	}
	
	
	@Override
	public void preTick()
	{
		glfwPollEvents();
		
		if(window.shouldClose()) stop();
	}
	
	@Override
	public void preRender()
	{
		glClearColor(0, 0, 0, 1);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	@Override
	public void render()
	{
		Mat4F proj = new Mat4F().setPerspective(70.0f * MathUtils.DEG_TO_RADf, 800.0f / 600.0f, 0.1f, 1000.0f);
		Mat4F view = Mat4F.translation3(0, 0, -10); // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
		
		Mat4F m = proj.mulN(view);
		
		shader.start();
		shader.setProj(m);
		
		this.rawModel.bind();
		this.rawModel.draw();
	}
	
	@Override
	public void postRender()
	{
		window.swapBuffers();
	}
	
	public void releaseResources()
	{
		this.rawModel.getVAO().releaseResources();
		
		shader.releaseResources();
		
		window.releaseResources();
	}
}
