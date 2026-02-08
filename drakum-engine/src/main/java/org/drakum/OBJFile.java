 package org.drakum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;

import org.barghos.api.math.vector.floats.BaseVecOpsI2F;
import org.barghos.api.math.vector.floats.BaseVecOpsI3F;
import org.barghos.impl.math.vector.Vec2F;
import org.barghos.impl.math.vector.Vec3F;

public class OBJFile
{
	private ArrayList<Vec2F> uvs = new ArrayList<>();
	private ArrayList<Vec3F> normals = new ArrayList<>();
	private ArrayList<Vec3F> pos = new ArrayList<>();
	public ArrayList<Integer> indices = new ArrayList<>();
	public ArrayList<Vertex> vertices = new ArrayList<>();
	
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
					
					calculateTangents(vA, vB, vC);	
					
					vertices.add(vA);
					vertices.add(vB);
					vertices.add(vC);
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

		this.indices.add(this.indices.size());
		
		return v;
	}
	
	private void calculateTangents(Vertex a, Vertex b, Vertex c)
	{
		Vec3F deltaPos1 = BaseVecOpsI3F.sub(b.pos, a.pos, new Vec3F());
		Vec3F deltaPos2 = BaseVecOpsI3F.sub(c.pos, a.pos, new Vec3F());
		
		Vec2F uv0 = a.uv;
		Vec2F uv1 = b.uv;
		Vec2F uv2 = c.uv;
		
		Vec2F deltaUv1 = BaseVecOpsI2F.sub(uv1, uv0, new Vec2F());
		Vec2F deltaUv2 = BaseVecOpsI2F.sub(uv2, uv0, new Vec2F());

		float r = 1.0f / (deltaUv1.x() * deltaUv2.y() - deltaUv1.y() * deltaUv2.x());
		BaseVecOpsI3F.mul(deltaPos1, deltaUv2.y(), deltaPos1);
		BaseVecOpsI3F.mul(deltaPos2, deltaUv1.y(), deltaPos2);
		
		Vec3F tangent = BaseVecOpsI3F.sub(deltaPos1, deltaPos2, new Vec3F());
		BaseVecOpsI3F.mul(tangent, r, tangent);
		
		BaseVecOpsI3F.add(a.tangent, tangent, a.tangent);
		BaseVecOpsI3F.add(b.tangent, tangent, b.tangent);
		BaseVecOpsI3F.add(c.tangent, tangent, c.tangent);
	}
	
}
