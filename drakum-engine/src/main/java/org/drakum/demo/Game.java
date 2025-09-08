package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL46C.*;

import java.io.File;

import org.barghos.math.matrix.Mat4F;
import org.barghos.util.math.MathUtils;
import org.lwjgl.glfw.GLFWErrorCallback;

public class Game
{
	private boolean isRunning;

	private Shader shader;

	private Window window;
	
	private RawModel rawModel;
	
	public void start()
	{
		this.isRunning = true;
		
		run();
	}
	
	public void stop()
	{
		this.isRunning = false;
	}
	
	public void init()
	{
		glfwInit();
		
		glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		window = new Window(800, 600, "Test");
		
		shader = new Shader();
		
		OBJFile obj = new OBJFile();
		obj.load(new File("res/models/dragon.obj"));
		Mesh mesh = new Mesh().loadFromObj(obj);
		rawModel = new RawModel(mesh.vao, mesh.getVertexCount());
		
		window.show();
	}
	
	public void run()
	{
		init();
		
		while(this.isRunning)
		{
			cycle();
		}
		
		releaseResources();
	}

	public void cycle()
	{
		tick();
		render();
	}
	
	public void tick()
	{
		glfwPollEvents();
		
		if(window.shouldClose()) stop();
	}
	
	public void render()
	{
		glClearColor(0, 0, 0, 1);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		Mat4F proj = new Mat4F().setPerspective(70.0f * MathUtils.DEG_TO_RADf, 800.0f / 600.0f, 0.1f, 1000.0f);
		Mat4F view = Mat4F.translation3(0, 0, -10); // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
		
		Mat4F m = proj.mulN(view);
		
		shader.start();
		shader.setProj(m);
		
		this.rawModel.bind();
		this.rawModel.draw();
		
		window.swapBuffers();
	}
	
	public void releaseResources()
	{
		this.rawModel.getVAO().releaseResources();
		
		shader.releaseResources();
		
		window.releaseResources();
		
		glfwTerminate();
	}
}
