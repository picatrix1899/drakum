 package org.drakum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;

import org.barghos.impl.math.shapes.Triangle3F;
import org.barghos.impl.math.vector.Vec2F;
import org.barghos.impl.math.vector.Vec3F;


public class OBJFile
{
	public ArrayList<Triangle3F> triangles = new ArrayList<>();
	public ArrayList<TriangleData> data = new ArrayList<>();

	private ArrayList<Vec2F> uvs = new ArrayList<>();
	private ArrayList<Vec3F> normals = new ArrayList<>();
	private ArrayList<Vec3F> pos = new ArrayList<>();
	public ArrayList<Integer> indices = new ArrayList<>();
	
	public Vec3F min = new Vec3F(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	public Vec3F max = new Vec3F(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	
	public void load(String file)
	{	
		try
		{
			URL url = OBJFile.class.getResource(file);
			
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(url.toURI())));
			String line = "";
			String[] parts;

			Vec3F position;
			
			while((line = reader.readLine()) != null)
			{
				if(line.startsWith("v "))
				{
					parts = line.split(" ");
					
					position = new Vec3F(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
					
					this.pos.add(position);
				}
				else if(line.startsWith("vt "))
				{
					parts = line.split(" ");
					uvs.add(new Vec2F(Float.parseFloat(parts[1]),Float.parseFloat(parts[2])));
				}
				else if(line.startsWith("vn "))
				{
					parts = line.split(" ");
					normals.add(new Vec3F(Float.parseFloat(parts[1]),Float.parseFloat(parts[2]),Float.parseFloat(parts[3])));
				}
				else if(line.startsWith("f "))
				{
					parts = line.split(" ");
					
					
					Vertex vA = processVertex(parts[1]);
					Vertex vB = processVertex(parts[2]);
					Vertex vC = processVertex(parts[3]);

//					calculateTangents(vA, vB, vC);	
					
					Triangle3F tr = new Triangle3F();
					tr.a = vA.pos;
					tr.b = vB.pos;
					tr.c = vC.pos;
					
					TriangleData d = new TriangleData();
					d.normalA = vA.normal;
					d.normalB = vB.normal;
					d.normalC = vC.normal;
					
					d.uvA = vA.uv;
					d.uvB = vB.uv;
					d.uvC = vC.uv;
					
					d.tangentA = vA.tangent;
					d.tangentB = vB.tangent;
					d.tangentC = vC.tangent;

					this.triangles.add(tr);
					this.data.add(d);
				}
			}
			
			reader.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Vertex processVertex(String line)
	{
		String[] vertex = line.split("/");
		
		int posIndex =	Integer.parseInt(vertex[0]) - 1;
		int textureIndex =	Integer.parseInt(vertex[1]) - 1;
		int normalIndex =	Integer.parseInt(vertex[2]) - 1;
		
		Vertex v;
		
		v = new Vertex();
		
		v.pos = this.pos.get(posIndex);
		v.uv = this.uvs.get(textureIndex);
		v.normal = this.normals.get(normalIndex);
		v.tangent = new Vec3F();

//		if(v.pos.x() < min.x()) min.x(v.pos.x());
//		if(v.pos.y() < min.y()) min.y(v.pos.y());
//		if(v.pos.z() < min.z()) min.z(v.pos.z());
//
//		if(v.pos.x() > max.x()) max.x(v.pos.x());
//		if(v.pos.y() > max.y()) max.y(v.pos.y());
//		if(v.pos.z() > max.z()) max.z(v.pos.z());

		this.indices.add(this.indices.size());
		
		return v;
	}
	
//	private void calculateTangents(Vertex a, Vertex b, Vertex c)
//	{
//		Vec3F deltaPos1 = a.pos.vecToN(b.pos);
//		Vec3F deltaPos2 = a.pos.vecToN(c.pos);
//		
//		Vec2F uv0 = a.uv;
//		Vec2F uv1 = b.uv;
//		Vec2F uv2 = c.uv;
//		
//		Vec2F deltaUv1 = uv1.subN(uv0);
//		Vec2F deltaUv2 = uv2.subN(uv0);
//
//		float r = 1.0f / (deltaUv1.x() * deltaUv2.y() - deltaUv1.y() * deltaUv2.x());
//		deltaPos1.mul(deltaUv2.y());
//		deltaPos2.mul(deltaUv1.y());
//		Vec3F tangent = deltaPos1.subN(deltaPos2);
//		tangent.mul(r);
//		
//		a.tangent.add(tangent);
//		b.tangent.add(tangent);
//		c.tangent.add(tangent);
//	}
	
}
