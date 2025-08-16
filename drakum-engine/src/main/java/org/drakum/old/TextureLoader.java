package org.drakum.old;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class TextureLoader
{
	public static TextureData loadTexture(String fileName)
	{
		return loadTexture(new File(fileName));
	}
	
	public static TextureData loadTexture(File img)
	{
		try
		{
			BufferedImage image = ImageIO.read(img);

			int[] pixels = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());


			byte[] arr = new byte[image.getWidth() * image.getHeight() * 4];
			
			for(int y = 0; y < image.getHeight(); y++)
			{
				for(int x = 0; x < image.getWidth(); x++)
				{
					int pixel = pixels[y * image.getWidth() + x];
					
					int pos = y * (image.getWidth() * 4) + (x * 4);
					arr[pos + 0] = ((byte) ((pixel >> 16) & 0xFF));     // Red component
					arr[pos + 1] = ((byte) ((pixel >> 8) & 0xFF));      // Green component
					arr[pos + 2] = ((byte) (pixel & 0xFF));               // Blue component
					arr[pos + 3] = ((byte) ((pixel >> 24) & 0xFF));    // Alpha component
				}
			}

			return new TextureData(arr, image.getWidth(), image.getHeight());
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	

}
