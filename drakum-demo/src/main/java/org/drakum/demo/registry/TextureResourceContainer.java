package org.drakum.demo.registry;

public class TextureResourceContainer implements IResourceContainer
{
	public MemoryResourceContainer memory;
	public ImageResourceContainer image;
	public ImageViewResourceContainer imageView;
	
	@Override
	public void close()
	{
		imageView.close();
		image.close();
		memory.close();
	}

}
