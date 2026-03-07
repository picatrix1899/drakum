package org.drakum.demo;

import org.barghos.api.math.matrix.DefaultMatsI4F;
import org.barghos.api.math.matrix.MatOpsI4F;
import org.barghos.api.math.quaternion.DefaultQuatsIF;
import org.barghos.api.math.transform.LerpTransformOpsI3F;
import org.barghos.api.math.vector.floats.BaseVecOpsI3F;
import org.barghos.api.math.vector.floats.QuatVecOpsI3F;
import org.barghos.api.math.vector.floats.VecOpsI3F;
import org.barghos.core.math.MathUtils;
import org.barghos.glfw.window.GlfwWindow;
import org.barghos.hid.HidInputState;
import org.barghos.impl.math.matrix.Mat4F;
import org.barghos.impl.math.quaternion.QuatF;
import org.barghos.impl.math.transform.Transform3F;
import org.barghos.impl.math.vector.Vec3F;
import org.drakum.hid.HidKeys;
import org.drakum.input.InputKeyboard;

public class Player implements IActorComponent
{
	private Camera camera;
	
	private InputKeyboard inputKeyboard;
	private GlfwWindow window;
	
	public float pitch;
	public float yaw;
	
	public Transform3F previousLocalTransform = new Transform3F();
	public Transform3F localTransform = new Transform3F();
	
	private boolean lookEnabled;
	
	public Player(InputKeyboard inputKeyboard, GlfwWindow window)
	{
		this.inputKeyboard = inputKeyboard;
		this.window = window;
		
		this.camera = new Camera();
		this.camera.localTransform.setPos(0, 1.9f, 0);
		this.camera.projection.set(70.0f, this.window.framebufferAspectRatiof(), 0.1f, 1000);
		this.camera.parent = this;
	}
	
	public void framebufferResize(float width, float height)
	{
		camera.projection.set(70.0f, width / height, 0.1f, 1000);
	}
	
	public void update()
	{
		if(inputKeyboard.isKeyHeld(HidKeys.EXIT_GAME))
		{
			Game.engine.stop();
		}

		Vec3F velocity = new Vec3F();

//		if(inputKeyboard.isKeyHeld(HidKeys.FORWARD))
//		{
//			BaseVecOpsI3F.add(velocity, forward(), velocity);
//		}

		if(inputKeyboard.isKeyHeld(HidKeys.LEFT))
		{
			BaseVecOpsI3F.sub(velocity, right(), velocity);
		}

		if(inputKeyboard.isKeyHeld(HidKeys.BACKWARD))
		{
			BaseVecOpsI3F.sub(velocity, forward(), velocity);
		}

		if(inputKeyboard.isKeyHeld(HidKeys.RIGHT))
		{
			BaseVecOpsI3F.add(velocity, right(), velocity);
		}

		float updateTime = 1.0f / Game.UPS;
		
		if(velocity.x != 0 || velocity.y != 0 || velocity.z != 0)
		{
			VecOpsI3F.normalize(velocity, velocity);
			
			BaseVecOpsI3F.mul(velocity, updateTime * 2f, velocity);

			this.localTransform.posX += velocity.x;
			this.localTransform.posY += velocity.y;
			this.localTransform.posZ += velocity.z;
		}
		
		if(this.lookEnabled)
		{
			HidInputState stateX = this.inputKeyboard.hidManager.getState(HidKeys.LOOK_X);
			if(stateX != null && stateX.value != 0.0f)
			{
				this.yaw += -stateX.value; // * 0.001f;
				this.yaw = this.yaw % (MathUtils.DEG_TO_RADf * 360);
			}
			
			HidInputState stateY = this.inputKeyboard.hidManager.getState(HidKeys.LOOK_Y);
			if(stateY != null && stateX.value != 0.0f)
			{
				this.pitch += -stateY.value; //* 0.001f;
				this.pitch = this.pitch % (MathUtils.DEG_TO_RADf * 360);
			}
			
			QuatF yawRot = DefaultQuatsIF.fromAxisAngleRad(0, 1, 0, this.yaw, new QuatF());
			
			this.localTransform.setRot(yawRot);

			QuatF pitchRot = DefaultQuatsIF.fromAxisAngleRad(1, 0, 0, this.pitch, new QuatF());
			
			this.camera.localTransform.setRot(pitchRot);
		}
	}
	
	public void moveForward()
	{
		Vec3F velocity = new Vec3F();
		
		BaseVecOpsI3F.add(velocity, forward(), velocity);
		
		float updateTime = 1.0f / Game.UPS;
		
		if(velocity.x != 0 || velocity.y != 0 || velocity.z != 0)
		{
			VecOpsI3F.normalize(velocity, velocity);
			
			BaseVecOpsI3F.mul(velocity, updateTime * 2f, velocity);

			this.localTransform.posX += velocity.x;
			this.localTransform.posY += velocity.y;
			this.localTransform.posZ += velocity.z;
		}
	}
	
	public void setLook(boolean enabled)
	{
		lookEnabled = enabled;
	}
	
	public Camera getCamera()
	{
		return this.camera;
	}
	
	public Vec3F forward()
	{
		QuatF qYaw = DefaultQuatsIF.fromAxisAngleRad(0, 1, 0, this.yaw, new QuatF());

		Vec3F forward = QuatVecOpsI3F.transform(0, 0, -1, qYaw, new Vec3F());
		VecOpsI3F.normalize(forward, forward);
		
		return forward;
	}
	
	public Vec3F right()
	{
		QuatF qYaw = DefaultQuatsIF.fromAxisAngleRad(0, 1, 0, this.yaw, new QuatF());

		Vec3F forward = QuatVecOpsI3F.transform(0, 0, -1, qYaw, new Vec3F());
		VecOpsI3F.normalize(forward, forward);
		
		Vec3F right = VecOpsI3F.cross(forward, 0, 1, 0, new Vec3F());
		VecOpsI3F.normalize(right, right);
		
		return right;
	}

	public Mat4F viewMatrix(float alpha)
	{
		Transform3F cameraWorldTransform = this.camera.worldTransform(alpha);

		QuatF rotQ = cameraWorldTransform.getRot(new QuatF());
		
		Mat4F rot = DefaultMatsI4F.rotationQuat(rotQ, new Mat4F());
		
		Vec3F pos = cameraWorldTransform.getPos(new Vec3F());
		
		Vec3F t = BaseVecOpsI3F.negate(pos, new Vec3F());
		
		Mat4F view = DefaultMatsI4F.translationXYZ(t, new Mat4F());
		
		Mat4F trRot = MatOpsI4F.transpose(rot, new Mat4F()); 
		
		MatOpsI4F.mul(view, trRot, view);
		
		return view; // Worldspace = righthanded(forward=-z); NDC = lefthanded(up=-y)
	}

	public void swapTransforms()
	{
		this.previousLocalTransform.set(this.localTransform);
		
		this.camera.swapTransforms();
	}
	
	@Override
	public Transform3F localTransform(float alpha)
	{
		return this.localTransform;
	}

	@Override
	public Transform3F worldTransform(float alpha)
	{
		Transform3F local = LerpTransformOpsI3F.lerp(alpha, previousLocalTransform, localTransform, new Transform3F());
		
		return local;
	}
}
