package org.drakum;

import java.util.ArrayList;
import java.util.List;

import org.barghos.impl.math.shapes.Triangle3F;
import org.barghos.impl.math.vector.Vec2F;
import org.barghos.impl.math.vector.Vec3F;
import org.barghos.impl.core.collection.ArrayUtils;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh
{
	public List<Triangle3F> triangles = new ArrayList<>();
	public List<TriangleData> triangleData = new ArrayList<>();
	public List<Integer> indices = new ArrayList<>();

	public Vao vao;

	public Vao getVAO()
	{
		return this.vao;
	}

	public int getVertexCount()
	{
		return this.triangles.size() * 3;
	}

	public int getTriangleCount()
	{
		return this.triangles.size();
	}

	public void loadFromObj(OBJFile obj)
	{
		this.indices = obj.indices;
		this.triangles = obj.triangles;
		this.triangleData = obj.data;

		int triangleCount = this.triangles.size();
		int verticesCount = triangleCount * 3;

		Vec3F[] pos = new Vec3F[verticesCount];
		Vec2F[] uvs = new Vec2F[verticesCount];
		Vec3F[] nrm = new Vec3F[verticesCount];
		Vec3F[] tng = new Vec3F[verticesCount];

		int[] indices = new int[this.indices.size()];

		Triangle3F tr;
		TriangleData td;

		for (int i = 0; i < triangleCount; i++)
		{
			tr = this.triangles.get(i);
			td = this.triangleData.get(i);

			pos[i * 3] = new Vec3F(tr.a);
			pos[i * 3 + 1] = new Vec3F(tr.b);
			pos[i * 3 + 2] = new Vec3F(tr.c);

			uvs[i * 3] = td.uvA.mulN(1, -1);
			uvs[i * 3 + 1] = td.uvB.mulN(1, -1);
			uvs[i * 3 + 2] = td.uvC.mulN(1, -1);

			nrm[i * 3] = td.normalA;
			nrm[i * 3 + 1] = td.normalB;
			nrm[i * 3 + 2] = td.normalC;

			tng[i * 3] = td.tangentA;
			tng[i * 3 + 1] = td.tangentB;
			tng[i * 3 + 2] = td.tangentC;

		}

		indices = ArrayUtils.convertToPrimitive(this.indices.toArray(new Integer[this.indices.size()]));

		this.vao = new Vao();
		this.vao.storeIndices(indices, GL_STATIC_DRAW);

		this.vao.storeData(0, pos, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(1, uvs, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(2, nrm, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(3, tng, 0, 0, GL_STATIC_DRAW);
	}
}
