package org.drakum;

import static org.lwjgl.opengl.GL46C.*;

import java.io.File;

import org.barghos.math.matrix.Mat4F;
import org.barghos.util.math.MathUtils;
import org.drakum.old.Colored_DBGShader;
import org.drakum.old.Mesh;
import org.drakum.old.OBJFile;

import static org.lwjgl.glfw.GLFW.*;

public class Engine
{
	private boolean isRunning;
	
	private Window window;
	
	private Colored_DBGShader shader;

	private Mesh mesh;
	
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
		if(!glfwInit())
		{
			throw new Error("Cannot init glfw");
		}
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		
		WindowState windowState = new WindowState();
		windowState.width = 800;
		windowState.height = 600;
		windowState.title = "Drakum Demo";
		
		Window.Settings windowSettings = new Window.Settings();
		windowSettings.state = windowState;
		windowSettings.isResizable = true;
		windowSettings.closeRequestedEventCallback = (_) -> stop();
		
		Window window = new Window(windowSettings);
		this.window = window;
		
		glViewport(0, 0, 800,  600);
		
		Colored_DBGShader shader = new Colored_DBGShader();
		
		OBJFile objFile = new OBJFile();
		objFile.load(new File("res/models/dragon.obj"));
		
		Mesh m = new Mesh().loadFromObj(objFile);
		
		this.mesh = m;
		
		this.shader = shader;
		
		this.window.show();
	}
	
	public void run()
	{
		init();
		
		while(this.isRunning)
		{
			cycle();
		}
		
		cleanup();
	}
	
	public void cleanup()
	{
		this.mesh.vao.clear();
		
		this.shader.cleanup();
		
		this.window.destroyResource();
		
		glfwTerminate();
	}
	
	public void cycle()
	{
		update();
		render();
	}
	
	public void update()
	{
		glfwPollEvents();
	}
	
	public void render()
	{
		this.window.beginFrame(0);
		
		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT);
		
		Mat4F proj = new Mat4F().setPerspective(MathUtils.DEG_TO_RADf * 70.0f, 800.0f / 600.0f, 0.01f, 10000f);

		shader.loadProjectionMatrix(proj);
		
		Mat4F m = new Mat4F().setIdentity();
		m.setTranslation3(0.0f, 0.0f, -10f);
		
		shader.loadModelMatrix(m);
		shader.use();
		
		glBindVertexArray(this.mesh.vao.getID());
		glEnableVertexAttribArray(0);
		
		glDrawElements(GL_TRIANGLES, this.mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
		
		glBindVertexArray(0);
		
		shader.stop();
		
		this.window.endFrame(0);
	}
}
