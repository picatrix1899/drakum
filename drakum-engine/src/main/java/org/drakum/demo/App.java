package org.drakum.demo;

import org.barghos.impl.core.Debug;

public class App
{
	public static void main(String[] args)
	{
		Debug.DEBUG_MODE = true;
		
		Game game = new Game();
		game.start();
	}

}
