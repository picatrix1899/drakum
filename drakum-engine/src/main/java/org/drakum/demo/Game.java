package org.drakum.demo;

import java.util.ArrayList;
import java.util.List;

import org.barghos.api.core.math.MathUtils;
import org.barghos.glfw.window.GlfwWindow;
import org.barghos.impl.math.vector.Vec3F;
import org.drakum.Camera;
import org.drakum.Engine;
import org.drakum.IEngineRoutine;
import org.drakum.InputKeyboard;
import org.drakum.InputMouse;
import org.drakum.OBJFile;
import org.drakum.Shader;
import org.drakum.SimpleEngineLoop;
import org.drakum.Texture;
import org.drakum.TextureData;
import org.drakum.TextureLoader;
import org.drakum.TextureUtils;
import org.drakum.anim.Animator;
import org.drakum.anim.AssimpLoader;
import org.drakum.anim.EmKp;
import org.drakum.boilerplate.FFMGL;
import org.drakum.entity.Entity;
import org.drakum.entity.EntityTemplateStaticModel;
import org.drakum.entity.IEntityTemplate;
import org.drakum.entity.ITexturedModelProvider;
import org.drakum.model.AnimatedModel;
import org.drakum.model.ConstMesh;
import org.drakum.model.RawModel;
import org.drakum.model.SkinnedMesh;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL43C.*;
import org.lwjgl.opengl.GLDebugMessageCallback;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

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

	private AnimatedModel animModel;
	private Animator animator;
	private EmKp emkp;
	private Shader shader2;

	private Entity entity;
	private Entity entity2;
	private EntityTemplateStaticModel entityTemplate;
	
	private List<Entity> world = new ArrayList<>();
	
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

			int keyboardAction = mapKeyAction(action);

			this.inputKeyboard.sendKeyAction(keyboardKey, keyboardAction);
		});
		
		glfwSetCharCallback(window.handle(), (_, codepoint) -> {
			if(!this.inputKeyboard.trackCharacter())
			{
				return;
			}

			this.inputKeyboard.sendCharEntered(new String(Character.toChars(codepoint)));
		});

		this.inputKeyboard.trackCharacter(true);

		this.inputMouse = new InputMouse();

		glfwSetMouseButtonCallback(window.handle(), (_, button, action, _) -> {
			this.inputMouse.sendButtonAction(button, action);
		});
		
		window.makeContextCurrent();

		createCapabilities();

		if (getCapabilities().GL_KHR_debug) {
			glEnable(GL_DEBUG_OUTPUT);
			glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);

		    GLDebugMessageCallback callback = GLDebugMessageCallback.create((_, _, _, severity, length, message, _) -> {
		        String msg = GLDebugMessageCallback.getMessage(length, message);

		        String sev = mapSeverity(severity);

		        System.err.println("[GL DEBUG " + sev + "] " + msg);
		    });
		    
		    glDebugMessageCallback(callback, 0);
		}
		
		window.onCloseCallback(this::stop);
		window.onFramebufferResizeCallback((_, _, w, h) -> {
			glViewport(0, 0, w, h);
			camera.projection.set(70.0f * MathUtils.DEG_TO_RADf, w / h, 0.1f, 1000);
		});
		
		shader = new TestShader1();

		camera = new Camera(new Vec3F(0, 1.9f, 2));
		camera.projection.set(70.0f * MathUtils.DEG_TO_RADf, (float)this.window.windowAspectRatio(), 0.1f, 1000);

		OBJFile obj = new OBJFile();
		obj.load("/res/models/crate_resized_meter.obj");

		ConstMesh mesh = new ConstMesh(obj);

		rawModel = new RawModel(List.of(mesh));
		
		TextureData textureData = TextureLoader.loadTexture("/res/materials/crate.png");
		this.texture = TextureUtils.genTexture(textureData);

		this.entityTemplate = new EntityTemplateStaticModel();
		this.entityTemplate.model = rawModel;
		this.entityTemplate.texture = texture;
		
		animModel = AssimpLoader.load("/res/Only_Spider_with_Animations_Export.dae");
		animator = animModel.createAnimator(0);
		emkp = new EmKp();
		emkp.createSSBO(animModel.bones.size());
		
		shader2 = new TestShader2();

		entity = this.entityTemplate.createEntity();
		entity.transform.scale.set(1, 1, 1);
		entity.transform.rot.setFromAxisAngle(0, 1, 0, 20 * MathUtils.DEG_TO_RADf);
		entity.transform.pos.set(-5, 0, 0);
		
		this.world.add(entity);
		
		entity2 = this.entityTemplate.createEntity();
		entity2.transform.scale.set(0.5f, 0.5f, 0.5f);
		
		this.world.add(entity2);
		
		glCullFace(GL_BACK);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);

		window.show();
		
		currentTime = System.nanoTime();
	}

	private int mapKeyAction(int action)
	{
		return switch(action) {
		case GLFW_PRESS -> InputKeyboard.ACTION_PRESSED;
		case GLFW_REPEAT -> InputKeyboard.ACTION_REPEATED;
		default -> InputKeyboard.ACTION_RELEASED;
	};
	}
	
	private String mapSeverity(int severity)
	{
		return switch(severity) {
    	case GL_DEBUG_SEVERITY_NOTIFICATION -> "Info";
    	case GL_DEBUG_SEVERITY_LOW -> "Low";
    	case GL_DEBUG_SEVERITY_MEDIUM -> "Medium";
    	case GL_DEBUG_SEVERITY_HIGH -> "High";
    	default -> "";
    };
	}
	
	@Override
	public void preTick()
	{
		lastTime = currentTime;
		currentTime = System.nanoTime();
		delta = currentTime - lastTime;

		inputKeyboard.preUpdate();

		glfwPollEvents();
	}

	public long lastTime;
	public long currentTime;
	public float delta;

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
			velocity.add(this.camera.forward().normalize());
		}

		if(inputKeyboard.isKeyHeld(GLFW_KEY_A))
		{
			velocity.add(this.camera.right().negate().normalize());
		}

		if(inputKeyboard.isKeyHeld(GLFW_KEY_S))
		{
			velocity.add(this.camera.forward().negate().normalize());
		}

		if(inputKeyboard.isKeyHeld(GLFW_KEY_D))
		{
			velocity.add(this.camera.right().normalize());
		}

		velocity.normalize();

		velocity.mul(0.1f);

		camera.move(velocity);

		double[] adx = new double[1];
		double[] ady = new double[1];
		glfwGetCursorPos(this.window.handle(), adx, ady);

		float cx =  this.window.windowWidth() * 0.5f;
		float cy =  this.window.windowHeight() * 0.5f;

		float dx = (float)adx[0];
		float dy = (float)ady[0];

		float fx = dx - cx;
		float fy = dy - cy;

		fx *= -0.002f;
		fy *= -0.002f;

		glfwSetCursorPos(this.window.handle(), cx, cy);

		this.camera.rotate(fy, fx, 0.0f);

		this.animator.update(delta / 1000000000l);

		emkp.updateSSBO(this.animModel.bones);
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
		Long2ObjectMap<List<Entity>> entityTemplateMapping = new Long2ObjectOpenHashMap<>();
		
		for(Entity entity : this.world)
		{
			if(!entityTemplateMapping.containsKey(entity.template))
			{
				entityTemplateMapping.put(entity.template, new ArrayList<Entity>());
			}
			
			entityTemplateMapping.get(entity.template).add(entity);
		}
		
		Long2ObjectMap<IEntityTemplate> entityTemplates = new Long2ObjectOpenHashMap<>();
		entityTemplates.put(this.entityTemplate.id, this.entityTemplate);
		
		shader.start();
		camera.projection.uploadToShader(shader);
		shader.setMat4f("m_view", camera.viewMatrix());
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		FFMGL.glDisableVertexAttribArray(5);
		FFMGL.glDisableVertexAttribArray(6);	
		
		for(IEntityTemplate template : entityTemplates.values())
		{
			ITexturedModelProvider modelProvider = (ITexturedModelProvider)template;
			
			RawModel model = modelProvider.getModel();
			Texture texture = modelProvider.getTexture();
			List<Entity> templateEntities = entityTemplateMapping.get(template.getId());
			
			shader.setTexture("albedo", texture);
			
			for(ConstMesh mesh : model.meshes())
			{
				for(Entity entity : templateEntities)
				{
					shader.setMat4f("m_model", entity.modelMatrix());
					
					mesh.draw();
				}
			}
		}
		
		FFMGL.glEnableVertexAttribArray(0);
		FFMGL.glEnableVertexAttribArray(1);
		FFMGL.glEnableVertexAttribArray(5);
		FFMGL.glEnableVertexAttribArray(6);
		
		shader2.start();
		camera.projection.uploadToShader(shader2);
		shader2.setMat4f("m_view", camera.viewMatrix());

		for (SkinnedMesh mesh : animModel.meshes)
			mesh.draw();
	}

	@Override
	public void postRender()
	{
		window.swapBuffers();
	}

	@Override
	public void releaseResources()
	{
		this.rawModel.releaseResources();	
		this.animModel.releaseResources();
		
		this.texture.cleanup();

		shader.releaseResources();

		window.releaseResources();
	}
}
