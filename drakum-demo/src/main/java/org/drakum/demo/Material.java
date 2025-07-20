package org.drakum.demo;

import org.drakum.demo.vkn.VknSampler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;

public class Material
{
	public Long2ObjectMap<Texture> textures = new Long2ObjectRBTreeMap<Texture>();
	public Long2ObjectMap<VknSampler> samplers = new Long2ObjectRBTreeMap<VknSampler>();
	
	public long descSet;
	
	public MaterialType type;
}
