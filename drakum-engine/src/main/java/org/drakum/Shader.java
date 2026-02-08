package org.drakum;
import static org.lwjgl.opengl.GL46C.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.barghos.api.math.matrix.IMat4RF;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

public class Shader
{
	private final int shaderProgram;
	private final Int2IntMap shaders = new Int2IntArrayMap();
	
	public Shader()
	{
		this.shaderProgram = glCreateProgram();
	}
	
	public void vertexShader(String file)
	{
		if(this.shaders.containsKey(GL_VERTEX_SHADER)) throw new RuntimeException("Shader slot already occupied.");
		
		int shader = loadShader(file, GL_VERTEX_SHADER);
		
		glAttachShader(this.shaderProgram, shader);
		
		this.shaders.put(GL_VERTEX_SHADER, shader);
	}
	
	public void fragmentShader(String file)
	{
		if(this.shaders.containsKey(GL_FRAGMENT_SHADER)) throw new RuntimeException("Shader slot already occupied.");
		
		int shader = loadShader(file, GL_FRAGMENT_SHADER);
		
		glAttachShader(this.shaderProgram, shader);
		
		this.shaders.put(GL_FRAGMENT_SHADER, shader);
	}
	
	public void link()
	{
		glLinkProgram(this.shaderProgram);
	}
	
	public void start()
	{
		glUseProgram(this.shaderProgram);
	}
	
	public void stop()
	{
		glUseProgram(0);
	}
	
	public void setMat4f(String name, IMat4RF m)
	{
		int location = glGetUniformLocation(this.shaderProgram, name);
		glUniformMatrix4fv(location, false, m.toArray());
	}
	
	public void setTexture(String name, Texture t)
	{
		int location = glGetUniformLocation(this.shaderProgram, name);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, t.getId());
		glUniform1i(location, 0);
	}
	
	public void setVector2f(String name, float x, float y)
	{
		int location = glGetUniformLocation(this.shaderProgram, name);
		glUniform2f(location, x, y);
	}
	
	public void setVector3f(String name, float x, float y, float z)
	{
		int location = glGetUniformLocation(this.shaderProgram, name);
		glUniform3f(location, x, y, z);
	}
	
	public void releaseResources()
	{
		glDeleteProgram(this.shaderProgram);

		for(int shader : this.shaders.values())
		{
			glDeleteShader(shader);
		}
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
