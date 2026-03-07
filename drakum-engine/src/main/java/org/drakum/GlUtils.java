package org.drakum;

import org.drakum.boilerplate.FFMGL;

public class GlUtils
{
	public static void enableVertexAttribArray(int...attribs)
	{
		for(int attrib : attribs)
		{
			FFMGL.glEnableVertexAttribArray(attrib);
		}
	}
	
	public static void disableVertexAttribArray(int...attribs)
	{
		for(int attrib : attribs)
		{
			FFMGL.glDisableVertexAttribArray(attrib);
		}
	}
}
