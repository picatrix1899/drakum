package org.drakum.anim;

import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Animator
{
	private final Node root;
	private final Animation animation;
	private final Map<String, Bone> bones;
	private float time = 0f;

	public Animator(Node root, Animation animation, Map<String, Bone> bones)
	{
		this.root = root;
		this.animation = animation;
		this.bones = bones;
	}

	public void update(float deltaTime)
	{
		// Zeit voranschreiten
		time += deltaTime * animation.ticksPerSecond;
		time %= animation.duration; // Loop

		updateNode(root, new Matrix4f().identity());
	}

	private void updateNode(Node node, Matrix4f parentTransform)
	{
		Matrix4f localTransform = node.transform;

		BoneTrack track = findTrack(node.name);
		if (track != null) localTransform = interpolate(track, time);

		Matrix4f globalTransform = new Matrix4f(parentTransform).mul(localTransform);

		Bone bone = bones.get(node.name);
		if (bone != null)
		{
			bone.localTransform.set(localTransform);
			bone.globalTransform.set(globalTransform);
			bone.finalMatrix.set(globalTransform).mul(bone.offsetMatrix);
		}
		
		for (Node child : node.children)
		{
			updateNode(child, globalTransform);
		}
	}

	private BoneTrack findTrack(String name)
	{
		for (BoneTrack t : animation.tracks)
		{
			if (t.boneName.equals(name)) return t;
		}
		
		return null;
	}

	private Matrix4f interpolate(BoneTrack track, float time)
	{
		if (track.keys.isEmpty()) return new Matrix4f().identity();

		BoneKey before = track.keys.get(0);
		BoneKey after = track.keys.get(track.keys.size() - 1);

		for (int i = 0; i < track.keys.size() - 1; i++)
		{
			BoneKey a = track.keys.get(i);
			BoneKey b = track.keys.get(i + 1);
			
			if (time >= a.time && time < b.time)
			{
				before = a;
				after = b;
				break;
			}
		}

		float factor = (time - before.time) / Math.max(1e-6f, after.time - before.time);

		Vector3f pos = new Vector3f(before.position).lerp(after.position, factor);
		Quaternionf rot = new Quaternionf(before.rotation).slerp(after.rotation, factor);
		Vector3f sca = new Vector3f(before.scale).lerp(after.scale, factor);

		Matrix4f result = new Matrix4f().translationRotateScale(pos, rot, sca);

		return result;
	}
}
