package org.drakum.demo;

import static org.lwjgl.vulkan.VK14.*;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

import org.drakum.demo.vkn.CommonRenderContext;
import org.drakum.demo.vkn.VknImage2D;
import org.drakum.demo.vkn.VknImageView2D;
import org.drakum.demo.vkn.VknMemory;
import org.drakum.demo.vkn.VknStagingBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class TextureLoader
{
	public static Texture createTexture(String file)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			BufferedImage img = null;
			try
			{
				URL url = Engine.class.getResource(file);
				Path path = Paths.get(url.toURI());
				InputStream stream = Files.newInputStream(path, StandardOpenOption.READ);
				
				img = ImageIO.read(stream);
				
				stream.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				throw new Error();
			}
			
			int width = img.getWidth();
			int height = img.getHeight();
			
			int format = VK_FORMAT_R8G8B8A8_SRGB;
			
			byte[] pixeldata = loadTextureData(img);
		
			VknImage2D image = new VknImage2D(new VknImage2D.Settings(CommonRenderContext.context).format(format).size(width, height).usageTransferDst().usageSampled());
			VknMemory textureMemory = image.allocateAndBindMemory(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

			try(VknStagingBuffer stagingBuffer = new VknStagingBuffer(new VknStagingBuffer.Settings(CommonRenderContext.context).size(pixeldata.length)))
			{
				stagingBuffer.store(pixeldata);
				stagingBuffer.transferToImage(image.handle().handle(), width, height, 1, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_ACCESS_SHADER_READ_BIT, VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
			}
			
			VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc(stack);
			imageViewCreateInfo.sType$Default();
			imageViewCreateInfo.image(image.handle().handle());
			imageViewCreateInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
			imageViewCreateInfo.format(format);
			imageViewCreateInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY).g(VK_COMPONENT_SWIZZLE_IDENTITY).b(VK_COMPONENT_SWIZZLE_IDENTITY).a(VK_COMPONENT_SWIZZLE_IDENTITY);
			imageViewCreateInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
			
			VknImageView2D textureView = image.createView();
			
			Texture texture = new Texture();
			texture.width = width;
			texture.height = height;
			texture.texture = image;
			texture.textureMemory = textureMemory;
			texture.textureView = textureView;
			texture.format = format;
			
			return texture;
		}
	}
	
	private static byte[] loadTextureData(BufferedImage image)
	{
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		byte[] data = new byte[image.getWidth() * image.getHeight() * 4];
		
		for(int y = 0; y < image.getHeight(); y++)
		{
			for(int x = 0; x < image.getWidth(); x++)
			{
				int pixelIndex = y * image.getWidth() + x;
				
				int pixel = pixels[pixelIndex];
				
				data[pixelIndex * 4  + 0] = (byte) ((pixel >> 16) & 0xFF); // Red component
				data[pixelIndex * 4  + 1] = (byte) ((pixel >> 8) & 0xFF); // Green component
				data[pixelIndex * 4  + 2] = (byte) (pixel & 0xFF); // Blue component
				data[pixelIndex * 4  + 3] = (byte) ((pixel >> 24) & 0xFF); // Alpha component
			}
		}

		return data;
	}
}
