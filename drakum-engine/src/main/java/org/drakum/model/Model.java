package org.drakum.model;

import static org.lwjgl.opengl.GL46C.*;

import java.util.List;

import org.drakum.Material;

public class Model
{
	private List<ConstMesh> meshes;
	private List<Material> materials;
	
	public Model(List<ConstMesh> meshes, List<Material> materials)
	{
		this.meshes = meshes;
	}
	
	public void bind()
	{
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
	}
	
	public List<ConstMesh> meshes()
	{
		return this.meshes;
	}
	
	public List<Material> materials()
	{
		return this.materials;
	}
	
	public void releaseResources()
	{
		for(ConstMesh mesh : this.meshes)
		{
			mesh.releaseResources();
		}
	}
}
