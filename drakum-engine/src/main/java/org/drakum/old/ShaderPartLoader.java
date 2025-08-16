package org.drakum.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class ShaderPartLoader
{
	public static ShaderPartData readShader(InputStream stream)
	{
		StringBuilder shaderSource = new StringBuilder();
		
		String line = "";
		String dest = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		URL url;

		try
		{
			while((line = reader.readLine()) != null)
			{
				shaderSource.append(line).append("\n");					
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
				stream.close();	
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return new ShaderPartData(shaderSource.toString());
	}
}
