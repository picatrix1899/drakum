package org.drakum.demo;

import java.util.ArrayList;
import java.util.List;

import org.barghos.api.math.matrix.MatOpsI4F;
import org.barghos.api.math.vector.floats.BaseVecOpsI3F;
import org.barghos.api.math.vector.floats.BaseVecOpsI4F;
import org.barghos.api.math.vector.floats.Mat4VecOpsI4F;
import org.barghos.api.math.vector.floats.VecOpsI3F;
import org.barghos.glfw.window.GlfwWindow;
import org.barghos.hid.HidInputKey;
import org.barghos.hid.HidManager;
import org.barghos.impl.core.Debug;
import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.quaternion.DefaultQuatsF;
import org.barghos.impl.math.vector.Vec3F;
import org.barghos.impl.math.vector.Vec4F;
import org.drakum.GlUtils;
import org.drakum.Material;
import org.drakum.OBJFile;
import org.drakum.Shader;
import org.drakum.Texture;
import org.drakum.TextureData;
import org.drakum.TextureLoader;
import org.drakum.TextureUtils;
import org.drakum.advInput.AdvancedInputManager;
import org.drakum.advInput.HighCondition;
import org.drakum.advInput.InputAction;
import org.drakum.advInput.RaiseCondition;
import org.drakum.anim.Animator;
import org.drakum.anim.AssimpLoader;
import org.drakum.anim.EmKp;
import org.drakum.engine.Engine;
import org.drakum.engine.FixedTimestepEngineLoop;
import org.drakum.engine.IEngineRoutine;
import org.drakum.entity.Entity;
import org.drakum.entity.EntityTemplateStaticModel;
import org.drakum.entity.IEntityTemplate;
import org.drakum.entity.ITexturedModelProvider;
import org.drakum.hid.CallbackHidPhantomDevice;
import org.drakum.hid.GlfwHidPhantomDevice;
import org.drakum.hid.GlfwKeyboardHidDevice;
import org.drakum.hid.GlfwMouseHidDevice;
import org.drakum.hid.HidKeys;
import org.drakum.input.InputKeyboard;
import org.drakum.input.InputMouse;
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
	
	private Texture texture;

	private InputKeyboard inputKeyboard;
	private InputMouse inputMouse;

	private AnimatedModel animModel;
	private Animator animator;
	private EmKp emkp;
	private Shader shader2;

	private Entity entity;
	private Entity entity2;
	
	private Player player;
	
	public static int UPS = 20;
	
	private HidManager hidManager;
	
	private AdvancedInputManager advInputManager;
	
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
		windowSettings.msaaSamples = 16;
		
		GlfwWindow window = GlfwWindow.create(windowSettings);
		
		engine.setWindow(window);
		
		this.hidManager = new HidManager();
		
		GlfwHidPhantomDevice glfwPhantomDevice = new GlfwHidPhantomDevice();
		CallbackHidPhantomDevice windowCursorPhantomDevice = new CallbackHidPhantomDevice("windowCursor", window::updateCursorPos);
		
		
		GlfwKeyboardHidDevice keyboardDevice = new GlfwKeyboardHidDevice();
		glfwSetKeyCallback(window.handle(), (_, key, scancode, action, _) -> keyboardDevice.onKey(key, scancode, action));
		
		GlfwMouseHidDevice mouseDevice = new GlfwMouseHidDevice();
		glfwSetMouseButtonCallback(window.handle(), (_, key, action, _) -> mouseDevice.onButton(key, action));
		window.onCursorPosChangeCallback(((_, _, _, _, deltaX, deltaY) -> mouseDevice.onMouseMove(deltaX, deltaY)));
		
		this.hidManager.registerPhantom(glfwPhantomDevice);
		this.hidManager.registerPhantom(windowCursorPhantomDevice);
		this.hidManager.register(keyboardDevice);
		this.hidManager.register(mouseDevice);
		
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
		
		this.inputMouse = new InputMouse();
		this.inputMouse.hidManager = this.hidManager;
		
		this.inputMouse.addButton(HidKeys.TOOGLE_LOOK);
		this.inputMouse.addButton(HidKeys.INTERACT);
		
		advInputManager = new AdvancedInputManager();
		advInputManager.hidManager = this.hidManager;
		
		InputAction forwardAction = new InputAction();
		forwardAction.condition = new HighCondition(HidKeys.FORWARD);
		forwardAction.callback = () -> this.player.moveForward();
		
		advInputManager.actions.add(forwardAction);
		
		window.onCloseCallback(this::stop);
		window.onFramebufferResizeCallback((_, _, w, h) -> {
			glViewport(0, 0, w, h);
			player.framebufferResize(w, h);
		});
		
		this.guiRenderer = new GuiRenderer();
		this.guiRenderer.init();
		
		shader = new TestShader1();
		
		OBJFile obj = OBJFile.load("/res/models/crate_resized_meter.obj");

		ConstMesh mesh = new ConstMesh(obj);

		rawModel = new RawModel(List.of(mesh));
		
		TextureData textureData = TextureLoader.loadTexture("/res/materials/crate.png");
		this.texture = TextureUtils.genTexture(textureData);
		
		animModel = AssimpLoader.load("/res/Only_Spider_with_Animations_Export.dae");
		animator = animModel.createAnimator(0);
		emkp = new EmKp();
		emkp.createSSBO(animModel.bones.size());
		
		shader2 = new TestShader2();

		entity = new Entity(0, 0);
		entity.localTransform.setScale(1, 1, 1);
		entity.localTransform.setPos(-5, 0, 0);
		
		min.set(-0.5f - 5, -0.5f, -0.5f);
		max.set(0.5f - 5, 0.5f, 0.5f);
		
		this.player = new Player(inputKeyboard, window);
		
		glCullFace(GL_BACK);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		
		glfwSwapInterval(0);
		
		window.show();
	}
	
	private Vec3F min = new Vec3F();
	private Vec3F max = new Vec3F();
	
	@Override
	public void earlyUpdate()
	{
		Game.engine.window().updateWindowState();
		
		this.player.swapTransforms();
		
		hidManager.poll();
		inputKeyboard.update();
		inputMouse.update();
		
		advInputManager.update();
	}

	private boolean cursorDisabled;
	
	@Override
	public void update()
	{
		if(this.inputMouse.isKeyHeld(HidKeys.TOOGLE_LOOK))
		{
			if(!cursorDisabled)
			{
				Game.engine.window().disabledCursor();
				Game.engine.window().rawMouseInput(true);
				this.player.setLook(true);
				cursorDisabled = true;
			}
			
		}
		else
		{
			if(cursorDisabled)
			{
				Game.engine.window().normalCursor();
				Game.engine.window().rawMouseInput(false);
				this.player.setLook(false);
				cursorDisabled = false;
			}
			
		}
		
		this.player.update();
		
//		if(this.inputMouse.isKeyPressed(HidKeys.INTERACT))
//		{
//			picking();
//		}
//		
//		if(this.pickedEntity != null && this.inputMouse.isKeyHeld(HidKeys.INTERACT))
//		{
//			GlfwWindow window = Game.engine.window();
//			
//			double dx = window.cursorPosX() - window.lastCursorPosX();
//			
//			if(dx != 0) {
//				float factored = (float)dx * 0.01f;
//				
//				this.pickedEntity.localTransform.posX += factored;
//				this.min.x += factored;
//				this.max.x += factored;
//			}
//		}
//		
//		if(this.inputMouse.isKeyReleased(HidKeys.INTERACT))
//		{
//			this.pickedEntity = null;
//		}
	}
	
	float lastAlpha = 0;
	
	@Override
	public void earlyRender(float alpha)
	{
		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
//		float dAlpha = alpha - lastAlpha;
//		if(dAlpha < 0) dAlpha += 1;
//		
//		lastAlpha = alpha;
//		
//		this.animator.update(dAlpha * (1.0f / UPS));
//
//		emkp.updateSSBO(this.animModel.bones);
	}

	@Override
	public void render(float alpha)
	{
		shader.start();
		Camera camera = this.player.getCamera();
		
		Mat4F view = player.viewMatrix(alpha);
		
		camera.projection.uploadToShader(shader);
		shader.setMat4f("m_view", view);
		
	//	GlUtils.disableVertexAttribArray(5, 6);

		RawModel model = this.rawModel;
		Texture texture = this.texture;
		Entity entity = this.entity;
		
		shader.setTexture("albedo", texture);
		
		for(ConstMesh mesh : model.meshes())
		{
			mesh.vao.bind();
			GlUtils.enableVertexAttribArray(0, 1);
			
			shader.setMat4f("m_model", entity.modelMatrix(alpha));
			
			mesh.draw();
		}
//		
//		shader2.start();
//		camera.projection.uploadToShader(shader2);
//		shader2.setMat4f("m_view", view);
//
//		for (SkinnedMesh mesh : animModel.meshes)
//			mesh.draw();
//		
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
	
	public void picking()
	{
		Camera camera = this.player.getCamera();
		Mat4F projectionMatrix = camera.projection.proj;
		Mat4F viewMatrix = player.viewMatrix(1);
		
		GlfwWindow window = Game.engine.window();
		
		float ww = window.windowWidthf();
		float wh = window.windowHeightf();
		float fw = window.framebufferWidthf();
		float fh = window.framebufferHeightf();
		float mx = (float)window.cursorPosX();
		float my = (float)window.cursorPosY();
		
		// Inputs: mx,my from glfwGetCursorPos (window coords)
		// Mat4f: proj, view

		// 1) Convert to framebuffer coords (HiDPI-safe)

		float sx = fw / ww;
		float sy = fh / wh;

		float mxFb = mx * sx;
		float myFb = my * sy;

		// 2) Screen -> NDC
		float xNdc = (2.0f * mxFb) / fw - 1.0f;
		float yNdc = 1.0f - (2.0f * myFb) / fh;

		// 3) Unproject two points
		
		Mat4F invVPMatrix = MatOpsI4F.mul(viewMatrix, projectionMatrix, new Mat4F()); 
		MatOpsI4F.invert(invVPMatrix, invVPMatrix);
		
		Vec4F nearClipVector = Mat4VecOpsI4F.transform(xNdc, yNdc, -1.0f, 1.0f, invVPMatrix, new Vec4F());
		Vec4F farClipVector = Mat4VecOpsI4F.transform(xNdc, yNdc, 1.0f, 1.0f, invVPMatrix, new Vec4F());
		
		BaseVecOpsI4F.div(nearClipVector, nearClipVector.w, nearClipVector);
		BaseVecOpsI4F.div(farClipVector, farClipVector.w, farClipVector);

		Vec3F rayOrigin = new Vec3F(nearClipVector.x, nearClipVector.y, nearClipVector.z);
		Vec3F rayDir = new Vec3F(farClipVector.x, farClipVector.y, farClipVector.z);
				
		BaseVecOpsI3F.sub(farClipVector.x, farClipVector.y, farClipVector.z, rayOrigin, rayDir);
		VecOpsI3F.normalize(rayDir, rayDir);
		
		float res = rayAabb(rayOrigin, rayDir, min, max);
		
		if(Float.isFinite(res))
		{
			pickedEntity = this.entity;
		}
	}
	
	private Entity pickedEntity;
	
	// Returns tHit or Float.POSITIVE_INFINITY if no hit
	static float rayAabb(Vec3F ro, Vec3F rd, Vec3F bmin, Vec3F bmax)
	{
	    float tmin = 0.0f;
	    float tmax = Float.POSITIVE_INFINITY;

	    // X
	    float invDx = 1.0f / rd.x;
	    float tx1 = (bmin.x - ro.x) * invDx;
	    float tx2 = (bmax.x - ro.x) * invDx;
	    float t1x = Math.min(tx1, tx2);
	    float t2x = Math.max(tx1, tx2);
	    tmin = Math.max(tmin, t1x);
	    tmax = Math.min(tmax, t2x);
	    if (tmax < tmin) return Float.POSITIVE_INFINITY;

	    // Y
	    float invDy = 1.0f / rd.y;
	    float ty1 = (bmin.y - ro.y) * invDy;
	    float ty2 = (bmax.y - ro.y) * invDy;
	    float t1y = Math.min(ty1, ty2);
	    float t2y = Math.max(ty1, ty2);
	    tmin = Math.max(tmin, t1y);
	    tmax = Math.min(tmax, t2y);
	    if (tmax < tmin) return Float.POSITIVE_INFINITY;

	    // Z
	    float invDz = 1.0f / rd.z;
	    float tz1 = (bmin.z - ro.z) * invDz;
	    float tz2 = (bmax.z - ro.z) * invDz;
	    float t1z = Math.min(tz1, tz2);
	    float t2z = Math.max(tz1, tz2);
	    tmin = Math.max(tmin, t1z);
	    tmax = Math.min(tmax, t2z);
	    if (tmax < tmin) return Float.POSITIVE_INFINITY;

	    return tmin; // first hit along ray
	}
}
