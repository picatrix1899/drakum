package org.drakum.demo;

import static org.lwjgl.opengl.GL46C.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.barghos.api.math.matrix.IMat4RF;
import org.drakum.Shader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader2
{
	private int vertexShader;
	private int fragmentShader;
	private int shaderProgram;
	
	private int matProj;
	private int matView;
	
	public Shader2(String vertexFile, String fragmentFile)
	{
		this.vertexShader = loadShader(vertexFile, GL_VERTEX_SHADER);
		
		this.fragmentShader = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
		
		this.shaderProgram = glCreateProgram();
		
		glAttachShader(this.shaderProgram, this.vertexShader);
		glAttachShader(this.shaderProgram, this.fragmentShader);
		
		glLinkProgram(this.shaderProgram);
		
		matProj = glGetUniformLocation(this.shaderProgram, "m_proj");
		matView = glGetUniformLocation(this.shaderProgram, "m_view");
	}
	
	public void start()
	{
		glUseProgram(this.shaderProgram);
	}
	
	public void setProj(IMat4RF m)
	{
		glUniformMatrix4fv(this.matProj, false, m.toArray());
	}
	
	public void setView(IMat4RF m)
	{
		glUniformMatrix4fv(this.matView, false, m.toArray());
	}
	
	public void releaseResources()
	{
		glDeleteProgram(this.shaderProgram);
		
		glDeleteShader(this.vertexShader);
		glDeleteShader(this.fragmentShader);
	}
	
	private int loadShader(String file, int type)
	{
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, readShader(file));
		GL20.glCompileShader(shaderID);
		
		if(GL20.glGetShaderi(shaderID,GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.out.println("Could not compile shader. " + file);
			System.exit(-1);
		}
		
		return shaderID;
		
	}
	
	private String readShader(String file)
	{
		URL url = Shader.class.getResource(file);
		
		StringBuilder shaderSource = new StringBuilder();
		
		String line;
		
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			
			while((line = reader.readLine()) != null)
			{
				shaderSource.append(line).append("\n");					
			}
			
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return shaderSource.toString();
	}
}
