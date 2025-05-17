package org.drakum.demo;

import org.barghos.util.Debug;

public class App
{
	
	public static void main(String[] args)
	{
		Debug.DEBUG_MODE = true;
		Debug.PRINT_STACK_ELEMENT = true;
		
		Engine engine = new Engine();
		engine.start();
	}

}
