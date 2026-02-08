package org.drakum.demo;

import java.util.ArrayList;
import java.util.List;

import org.barghos.api.math.quaternion.DefaultQuatsIF;
import org.barghos.core.math.MathUtils;
import org.barghos.glfw.window.GlfwWindow;
import org.barghos.hid.HidManager;
import org.barghos.impl.math.quaternion.QuatF;
import org.drakum.Material;
import org.drakum.OBJFile;
import org.drakum.Shader;
import org.drakum.Texture;
import org.drakum.TextureData;
import org.drakum.TextureLoader;
import org.drakum.TextureUtils;
import org.drakum.anim.Animator;
import org.drakum.anim.AssimpLoader;
import org.drakum.anim.EmKp;
import org.drakum.boilerplate.FFMGL;
import org.drakum.engine.Engine;
import org.drakum.engine.FixedTimestepEngineLoop;
import org.drakum.engine.IEngineRoutine;
import org.drakum.entity.Entity;
import org.drakum.entity.EntityTemplateStaticModel;
import org.drakum.entity.IEntityTemplate;
import org.drakum.entity.ITexturedModelProvider;
import org.drakum.hid.GlfwHidPhantomDevice;
import org.drakum.hid.GlfwKeyboardHidDevice;
import org.drakum.hid.HidKeys;
import org.drakum.input.InputKeyboard;
import org.drakum.model.AnimatedModel;
import org.drakum.model.ConstMesh;
import org.drakum.model.RawModel;
import org.drakum.model.SkinnedMesh;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43C.*;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class Game implements IEngineRoutine
{
	public static Engine engine;

	private Shader shader;

	private RawModel rawModel;

	private Material material;
	
	private Texture texture;

	private InputKeyboard inputKeyboard;

	private AnimatedModel animModel;
	private Animator animator;
	private EmKp emkp;
	private Shader shader2;

	private Entity entity;
	private Entity entity2;
	private EntityTemplateStaticModel entityTemplate;
	
	private Player player;
	
	private List<Entity> world = new ArrayList<>();
	
	public static int UPS = 40;
	
	private HidManager hidManager;
	
	private GuiRenderer guiRenderer;
	
	public Game()
	{
		Engine engine = new Engine();
		engine.setLoop(new FixedTimestepEngineLoop(UPS));
		engine.setRoutine(this);
		engine.setDebug(true);
		
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
	public void earlyInit()
	{
		GlfwWindow.Settings windowSettings = new GlfwWindow.Settings();
		windowSettings.title = "Drakum Demo";
		windowSettings.isResizable = true;
		windowSettings.windowWidth = 800;
		windowSettings.windowHeight = 600;

		GlfwWindow window = GlfwWindow.create(windowSettings);
		
		engine.setWindow(window);
		
		this.hidManager = new HidManager();
		
		GlfwKeyboardHidDevice keyboardDevice = new GlfwKeyboardHidDevice();
		keyboardDevice.bindWindow(window.handle());
		
		GlfwHidPhantomDevice glfwPhantomDevice = new GlfwHidPhantomDevice();
		
		this.hidManager.registerPhantom(glfwPhantomDevice);
		this.hidManager.register(keyboardDevice);
		
		this.inputKeyboard = new InputKeyboard();
		this.inputKeyboard.hidManager = this.hidManager;
		
		this.inputKeyboard.addKey(HidKeys.EXIT_GAME);
		this.inputKeyboard.addKey(HidKeys.FORWARD);
		this.inputKeyboard.addKey(HidKeys.LEFT);
		this.inputKeyboard.addKey(HidKeys.BACKWARD);
		this.inputKeyboard.addKey(HidKeys.RIGHT);
		
		glfwSetCharCallback(window.handle(), (_, codepoint) -> {
			if(!this.inputKeyboard.trackCharacter())
			{
				return;
			}

			this.inputKeyboard.sendCharEntered(new String(Character.toChars(codepoint)));
		});

		this.inputKeyboard.trackCharacter(true);
		
		window.onCloseCallback(this::stop);
		window.onFramebufferResizeCallback((_, _, w, h) -> {
			glViewport(0, 0, w, h);
			player.framebufferResize(w, h);
		});

		this.guiRenderer = new GuiRenderer();
		this.guiRenderer.init();
		
		shader = new TestShader1();

		OBJFile obj = new OBJFile();
		obj.load("/res/models/crate_resized_meter.obj");

		ConstMesh mesh = new ConstMesh(obj);

		rawModel = new RawModel(List.of(mesh));
		
		TextureData textureData = TextureLoader.loadTexture("/res/materials/crate.png");
		this.texture = TextureUtils.genTexture(textureData);

		this.material = new Material();
		this.material.albedo = this.texture;
		this.material.shader = this.shader;
		
		this.entityTemplate = new EntityTemplateStaticModel();
		this.entityTemplate.model = rawModel;
		this.entityTemplate.texture = texture;
		
		animModel = AssimpLoader.load("/res/Only_Spider_with_Animations_Export.dae");
		animator = animModel.createAnimator(0);
		emkp = new EmKp();
		emkp.createSSBO(animModel.bones.size());
		
		shader2 = new TestShader2();

		entity = this.entityTemplate.createEntity();
		entity.localTransform.setScale(1, 1, 1);
		entity.localTransform.setRot(DefaultQuatsIF.fromAxisAngleRad(0, 1, 0, 20 * MathUtils.DEG_TO_RADf, new QuatF()));
		entity.localTransform.setPos(-5, 0, 0);
		
		this.world.add(entity);
		
		entity2 = this.entityTemplate.createEntity();
		entity2.localTransform.setScale(0.5f, 0.5f, 0.5f);
		
		this.world.add(entity2);
		
		this.player = new Player(inputKeyboard, window);
		
		glCullFace(GL_BACK);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);

		window.show();
	}
	
	@Override
	public void earlyUpdate()
	{
		this.player.swapTransforms();
		
		hidManager.poll();
		inputKeyboard.update();
	}

	@Override
	public void update()
	{
		this.player.update();
	}

	float lastAlpha = 0;
	
	@Override
	public void earlyRender(float alpha)
	{
		glClearColor(0, 0, 0, 1);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		float dAlpha = alpha - lastAlpha;
		if(dAlpha < 0) dAlpha += 1;
		
		lastAlpha = alpha;
		
		this.animator.update(dAlpha * (1.0f / UPS));

		emkp.updateSSBO(this.animModel.bones);
	}

	@Override
	public void render(float alpha)
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
		Camera camera = this.player.getCamera();
		
		camera.projection.uploadToShader(shader);
		shader.setMat4f("m_view", player.viewMatrix(alpha));
		
		FFMGL.glEnableVertexAttribArray(0);
		FFMGL.glEnableVertexAttribArray(1);
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
		shader2.setMat4f("m_view", player.viewMatrix(alpha));

		for (SkinnedMesh mesh : animModel.meshes)
			mesh.draw();
		
		this.guiRenderer.render();
	}

	@Override
	public void lateRender(float alpha)
	{
		engine.window().swapBuffers();
	}

	@Override
	public void releaseResources()
	{
		this.rawModel.releaseResources();	
		this.animModel.releaseResources();
		
		this.texture.cleanup();

		shader.releaseResources();
		
		shader2.releaseResources();
		
		this.guiRenderer.releaseResources();
	}
}
