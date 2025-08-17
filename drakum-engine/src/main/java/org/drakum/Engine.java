package org.drakum;

import static org.lwjgl.opengl.GL46C.*;

import java.io.File;

import org.barghos.math.matrix.Mat4F;
import org.barghos.util.math.MathUtils;
import org.drakum.hid.HidManager;
import org.drakum.old.Colored_DBGShader;
import org.drakum.old.Mesh;
import org.drakum.old.OBJFile;
import org.drakum.rhi.RenderApi;
import org.drakum.rhi.ogl.OglRhiApi;
import org.drakum.window.Window;
import org.drakum.window.WindowEventHandler;
import org.drakum.window.WindowFactory;
import org.drakum.window.WindowState;

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
		RenderApi.api(new OglRhiApi());
		
		WindowState windowState = new WindowState();
		windowState.width = 800;
		windowState.height = 600;
		windowState.title = "Drakum Demo";
		
		WindowEventHandler windowEventHandler = new WindowEventHandler();
		windowEventHandler.callbackClose = () -> stop();
		
		WindowFactory.CreateSettings windowCreateSettings = new WindowFactory.CreateSettings();
		windowCreateSettings.state = windowState;
		windowCreateSettings.isResizable = true;
		windowCreateSettings.windowEventHandler = windowEventHandler;
		
		Window window = WindowFactory.create(windowCreateSettings);
		this.window = window;
		
		this.window.makeCurrent();
		
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
		
		WindowFactory.destroy(this.window);
		
		RenderApi.freeResources();
	}
	
	public void cycle()
	{
		update();
		render();
	}
	
	public void update()
	{
		HidManager.update();
	}
	
	public void render()
	{
		this.window.makeCurrent();
		this.window.beginFrame();
		
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
		
		this.window.endFrame();
	}
}
