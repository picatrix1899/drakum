package org.drakum.old;

import java.io.IOException;
import java.net.URL;

import org.drakum.demo.App;

public class ShaderPartUtils
{
	public static URL toUrl(String path)
	{
		URL url = null;
		
		try
		{
			url = App.class.getResource(path);
			
			if(url == null) throw new IOException();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return url;
	}
}
