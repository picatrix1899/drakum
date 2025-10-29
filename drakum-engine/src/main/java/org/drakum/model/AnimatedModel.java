package org.drakum.model;

import java.util.List;
import java.util.Map;

import org.drakum.anim.Animation;
import org.drakum.anim.Animator;
import org.drakum.anim.Bone;
import org.drakum.anim.Node;

public class AnimatedModel
{
	public final Node root;
	public final List<Bone> bones;
	public final Map<String, Bone> boneByName;
	public final List<Animation> animations;
	public final List<SkinnedMesh> meshes;

	public AnimatedModel(Node root, List<Bone> bones, Map<String, Bone> boneByName, List<Animation> animations, List<SkinnedMesh> meshes)
	{
		this.root = root;
		this.bones = bones;
		this.boneByName = boneByName;
		this.animations = animations;
		this.meshes = meshes;
	}

	public Animator createAnimator(int animationIndex)
	{
		return new Animator(root, animations.get(animationIndex), boneByName);
	}
	
	public void releaseResources()
	{
		for(SkinnedMesh mesh : this.meshes)
		{
			mesh.releaseResources();
		}
	}
}
