package org.drakum.anim;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIBone;

public class Node
{
	public final String name;
	public final Matrix4f transform;
	public final List<Node> children = new ArrayList<>();
	public AIBone bone; // null, wenn kein Bone

	public Node(String name, Matrix4f transform)
	{
		this.name = name;
		this.transform = transform;
	}
}
