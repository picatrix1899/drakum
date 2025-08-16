package org.drakum.demo.top;

import java.util.ArrayList;
import java.util.List;

import org.barghos.math.vector.Vec3F;

public class Mesh
{
	
	public List<Vertex> vertices = new ArrayList<>();
	public List<Integer> indices = new ArrayList<>();
	public int indicesCount;
	
	public static class Vertex
	{
		public Vec3F pos;
	}
}
