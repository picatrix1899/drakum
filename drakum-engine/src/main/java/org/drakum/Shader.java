package org.drakum;

import static org.lwjgl.opengl.GL46C.*;

import org.barghos.math.matrix.Mat4F;

public class Shader
{
	private int vertexShader;
	private int fragmentShader;
	private int shaderProgram;
	
	private int matProj;
	
	public Shader()
	{
		this.vertexShader = glCreateShader(GL_VERTEX_SHADER);
		
		String vertexShaderCode = "#version 330 core\n" +
			    "layout (location = 0) in vec3 aPos;\n" +
				"uniform mat4 m_proj;\n" +
			    "void main()\n" +
			    "{\n" +
			    "   gl_Position = m_proj * vec4(aPos.x, aPos.y, aPos.z, 1.0);\n" +
			    "}\0";
		
		glShaderSource(this.vertexShader, vertexShaderCode);
		
		glCompileShader(this.vertexShader);
		
		this.fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		
		String fragmentShaderCode = "#version 330 core\n" +
			    "out vec4 FragColor;\n" +
			    "void main()\n" +
			    "{\n" +
			    "   FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
			    "}\n\0";
		
		glShaderSource(this.fragmentShader, fragmentShaderCode);
		
		glCompileShader(this.fragmentShader);
		
		this.shaderProgram = glCreateProgram();
		
		glAttachShader(this.shaderProgram, this.vertexShader);
		glAttachShader(this.shaderProgram, this.fragmentShader);
		
		glLinkProgram(this.shaderProgram);
		
		matProj = glGetUniformLocation(this.shaderProgram, "m_proj");
	}
	
	public void start()
	{
		glUseProgram(this.shaderProgram);
	}
	
	public void setProj(Mat4F m)
	{
		glUniformMatrix4fv(this.matProj, false, m.toArray());
	}
	
	public void releaseResources()
	{
		glDeleteProgram(this.shaderProgram);
		
		glDeleteShader(this.vertexShader);
		glDeleteShader(this.fragmentShader);
	}
}
