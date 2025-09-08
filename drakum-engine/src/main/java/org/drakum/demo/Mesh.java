package org.drakum.demo;

import java.util.ArrayList;

import org.barghos.math.shapes.Triangle3F;
import org.barghos.math.vector.Vec2F;
import org.barghos.math.vector.Vec3F;
import org.barghos.util.collection.ArrayUtils;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh
{
	public ArrayList<Triangle3F> triangles = new ArrayList<>();
	public ArrayList<TriangleData> triangleData = new ArrayList<>();
	public ArrayList<Integer> indices = new ArrayList<>();

	private float yc = 0.0f;

	private float downscale = 1.0f;

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

	public float getScale()
	{
		return this.downscale;
	}

	private void calculateAABB(Vec3F[] p)
	{

		Vec3F min_point = new Vec3F(1000.0f, 1000.0f, 1000.0f);
		Vec3F max_point = new Vec3F(0.0f, 0.0f, 0.0f);

		for (Vec3F pos : p)
		{
			if (pos.x() < min_point.x())
			{
				min_point.x(pos.x());
			}

			if (pos.y() < min_point.y())
			{
				min_point.y(pos.y());
			}

			if (pos.z() < min_point.z())
			{
				min_point.z(pos.z());
			}

			if (pos.x() > max_point.x())
			{
				max_point.x(pos.x());
			}

			if (pos.y() > max_point.y())
			{
				max_point.y(pos.y());
			}

			if (pos.z() > max_point.z())
			{
				max_point.z(pos.z());
			}
		}

		setAABB(min_point, max_point);
	}

	public Mesh setAABB(Vec3F min, Vec3F max)
	{

		if (min.y() != 0)
		{
			this.yc = -min.y();
		}

		if (max.y() - min.y() != 10.0f)
		{
			float d = 10.0f - (max.y() - min.y());

			float o = 1.0f / (max.y() - min.y());

			float r = 0.0f;

			r = o * d;

			downscale = 1.0f + (r);
		}

		return this;
	}

	public float getYCorrection()
	{
		return this.yc;
	}

	public Mesh loadFromObj(OBJFile obj)
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

		calculateAABB(pos);

		indices = ArrayUtils.convertToPrimitive(this.indices.toArray(new Integer[this.indices.size()]));

		return loadToVAO0(pos, uvs, nrm, tng, indices);

	}

	private Mesh loadToVAO0(Vec3F[] positions, Vec2F[] texCoords, Vec3F[] normals, Vec3F[] tangents, int[] indices)
	{
		this.vao = new Vao();
		this.vao.storeIndices(indices, GL_STATIC_DRAW);

		this.vao.storeData(0, positions, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(1, texCoords, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(2, normals, 0, 0, GL_STATIC_DRAW);
		this.vao.storeData(3, tangents, 0, 0, GL_STATIC_DRAW);

		return this;
	}
}
