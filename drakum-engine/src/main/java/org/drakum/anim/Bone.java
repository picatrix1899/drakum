package org.drakum.anim;

import org.joml.Matrix4f;

public class Bone
{
	public final String name;
	public final int index;
	public final Matrix4f offsetMatrix;
	public final Node node; // Verknüpfung zur Hierarchie

	// Laufzeitdaten
	public final Matrix4f localTransform = new Matrix4f();
	public final Matrix4f globalTransform = new Matrix4f();
	public final Matrix4f finalMatrix = new Matrix4f();

	public Bone(String name, int index, Matrix4f offsetMatrix, Node node)
	{
		this.name = name;
		this.index = index;
		this.offsetMatrix = offsetMatrix;
		this.node = node;
	}
}
