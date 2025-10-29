package org.drakum.entity;

import org.drakum.Texture;
import org.drakum.model.RawModel;

public interface ITexturedModelProvider
{
	public RawModel getModel();
	public Texture getTexture();
}
