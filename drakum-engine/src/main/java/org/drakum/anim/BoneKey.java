package org.drakum.anim;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BoneKey
{
	public final float time;
	public final Vector3f position;
	public final Quaternionf rotation;
	public final Vector3f scale;

	public BoneKey(float time, Vector3f position, Quaternionf rotation, Vector3f scale)
	{
		this.time = time;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}
}
