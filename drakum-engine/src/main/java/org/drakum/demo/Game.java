package org.drakum.demo;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL46C.*;

import org.barghos.glfw.window.GlfwWindow;
import org.barghos.impl.math.vector.Vec3F;
import org.drakum.Camera;
import org.drakum.Engine;
import org.drakum.IEngineRoutine;
import org.drakum.InputKeyboard;
import org.drakum.InputMouse;
import org.drakum.Mesh;
import org.drakum.OBJFile;
import org.drakum.RawModel;
import org.drakum.Shader;
import org.drakum.SimpleEngineLoop;
import org.drakum.Texture;
import org.drakum.TextureData;
import org.drakum.TextureLoader;
import org.drakum.TextureUtils;
import org.drakum.Viewport3F;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;

public class Game implements IEngineRoutine
{
	public static Engine engine;
	
	private Shader shader;

	private GlfwWindow window;
	
	private RawModel rawModel;
	
	private Camera camera;
	
	private Texture texture;
	
	private InputKeyboard inputKeyboard;
	private InputMouse inputMouse;
	
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
		GlfwWindow.Settings windowSettings = new GlfwWindow.Settings();
		windowSettings.title = "Drakum Demo";
		windowSettings.isResizable = true;
		windowSettings.windowWidth = 800;
		windowSettings.windowHeight = 600;
		
		window = GlfwWindow.create(windowSettings);
		
		this.inputKeyboard = new InputKeyboard();
		
		glfwSetKeyCallback(window.handle(), (_, key, scancode, action, _) -> {
			int keyboardKey = key != GLFW_KEY_UNKNOWN ? key : 1000 + scancode;

			InputKeyboard.Action keyboardAction = switch(action) {
				case GLFW_PRESS -> InputKeyboard.Action.PRESSED;
				case GLFW_REPEAT -> InputKeyboard.Action.REPEATED;
				default -> InputKeyboard.Action.RELEASED;
			};
			
			this.inputKeyboard.sendKeyAction(keyboardKey, keyboardAction);
		});
		
		glfwSetCharCallback(window.handle(), (_, codepoint) -> {
			if(!this.inputKeyboard.trackCharacter()) return;
			
			this.inputKeyboard.sendCharEntered(new String(Character.toChars(codepoint)));
		});
		
		this.inputKeyboard.trackCharacter(true);
		
		this.inputMouse = new InputMouse();
		
		glfwSetMouseButtonCallback(window.handle(), (_, button, action, _) -> {
			this.inputMouse.sendButtonAction(button, action);
		});
		
		window.makeContextCurrent();
		
		GL.createCapabilities();
		
		if (GL.getCapabilities().GL_KHR_debug) {
		    glEnable(GL_DEBUG_OUTPUT);
		    glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);

		    GLDebugMessageCallback callback = GLDebugMessageCallback.create((_, _, _, severity, length, message, _) -> {
		        String msg = GLDebugMessageCallback.getMessage(length, message);

		        String sev = switch(severity) {
		        	case GL_DEBUG_SEVERITY_NOTIFICATION -> "Info";
		        	case GL_DEBUG_SEVERITY_LOW -> "Low";
		        	case GL_DEBUG_SEVERITY_MEDIUM -> "Medium";
		        	case GL_DEBUG_SEVERITY_HIGH -> "High";
		        	default -> "";
		        };
		        
		        System.err.println("[GL DEBUG " + sev + "] " + msg);
		    });

		    glDebugMessageCallback(callback, 0);
		}
		
		window.onCloseCallback(this::stop);
		window.onFramebufferResizeCallback((_, _, w, h) -> {
			glViewport(0, 0, w, h);
			camera.viewport(new Viewport3F(0, 0, 0.1f, w, h, 1000));
		});
		
		shader = new Shader("/resources/testShader.vs", "/resources/testShader.fs");
		
		camera = new Camera(new Vec3F(0, 1.9f, 2), new Viewport3F(0, 0, 0.1f, 800, 600, 1000));
		
		OBJFile obj = new OBJFile();
		obj.load("/res/models/crate_resized_meter.obj");
		
		Mesh mesh = new Mesh();
		mesh.loadFromObj(obj);
		
		rawModel = new RawModel(mesh.vao, mesh.getVertexCount());
		
		TextureData textureData = TextureLoader.loadTexture("/res/materials/crate.png");
		this.texture = TextureUtils.genTexture(textureData);
		
		glCullFace(GL_BACK);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		
		window.show();
	}
	
	@Override
	public void preTick()
	{
		inputKeyboard.preTick();
		
		glfwPollEvents();
	}
	
	@Override
	public void tick()
	{
		if(inputKeyboard.isKeyHeld(GLFW_KEY_ESCAPE))
		{
			stop();
		}
		
		Vec3F velocity = new Vec3F();
		
		if(inputKeyboard.isKeyHeld(GLFW_KEY_W))
		{
			velocity.add(this.camera.forward().nrm());
		}
		
		if(inputKeyboard.isKeyHeld(GLFW_KEY_A))
		{
			velocity.add(this.camera.right().neg().nrm());
		}
		
		if(inputKeyboard.isKeyHeld(GLFW_KEY_S))
		{
			velocity.add(this.camera.forward().neg().nrm());
		}
		
		if(inputKeyboard.isKeyHeld(GLFW_KEY_D))
		{
			velocity.add(this.camera.right().nrm());
		}
		
		velocity.nrm();
		
		velocity.mul(0.1f);
		
		camera.pos.add(velocity);
		
		double[] adx = new double[1]; 
		double[] ady = new double[1]; 
		glfwGetCursorPos(this.window.handle(), adx, ady);
		
		double cx =  this.window.windowWidth() * 0.5;
		double cy =  this.window.windowHeight() * 0.5;
		
		double dx = adx[0];
		double dy = ady[0];
		
		double fx = dx - cx;
		double fy = dy - cy;
		
		fx *= -0.002;
		fy *= -0.002;
		
		glfwSetCursorPos(this.window.handle(), cx, cy);
		
		this.camera.yaw += fx;
		this.camera.pitch += fy;
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
		shader.start();
		shader.setProj(camera.projectionMatrix());
		shader.setView(camera.viewMatrix());
		shader.setTexture(texture);
		
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
		
		this.texture.cleanup();
		
		shader.releaseResources();
		
		window.releaseResources();
	}
}
