package org.drakum.model;

import static org.lwjgl.opengl.GL46C.*;

import java.util.List;

public class RawModel
{
	private List<ConstMesh> meshes;

	public RawModel(List<ConstMesh> meshes)
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
	
	public void releaseResources()
	{
		for(ConstMesh mesh : this.meshes)
		{
			mesh.releaseResources();
		}
	}
}
