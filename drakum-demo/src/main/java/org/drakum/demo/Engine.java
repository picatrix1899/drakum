package org.drakum.demo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;

public class Engine
{
	private RendererMaster masterRenderer;
	
	private static boolean isRunning = true;
	
	public void start()
	{
		init();

		run();

		close();
	}

	public void init()
	{
		try
		{
			URL url = Engine.class.getResource("/settings.json");
			Path path;
		
			path = Paths.get(url.toURI());
		
			InputStream stream = Files.newInputStream(path, StandardOpenOption.READ);
			
			InputStreamReader isr = new InputStreamReader(stream);
			
			Gson gson = new Gson();
			AppSettings appSettings = gson.fromJson(isr, AppSettings.class);
			
			this.masterRenderer = new RendererMaster();
			this.masterRenderer.init(appSettings);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		while (Engine.isRunning)
		{
			update();
			render();
		}
	}

	public void update()
	{
		this.masterRenderer.update();
	}

	public void render()
	{
		this.masterRenderer.render();
	}

	public void close()
	{
		this.masterRenderer.close();
	}
	
	public static void stop()
	{
		isRunning = false;
	}
}
