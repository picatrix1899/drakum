package org.drakum;

import org.barghos.impl.math.vector.Vec2F;
import org.barghos.impl.math.vector.Vec3F;

public class Vertex
{
	public Vec3F pos;
	public Vec2F uv;
	public Vec3F normal;
	public Vec3F tangent = new Vec3F();
	public int index;
}
