package org.drakum.hid;

import static org.lwjgl.glfw.GLFW.*;

import org.barghos.hid.HidInputKey;

public class HidKeys
{
	public static HidInputKey EXIT_GAME = new HidInputKey(0, GLFW_KEY_ESCAPE);
	public static HidInputKey FORWARD = new HidInputKey(0, GLFW_KEY_W);
	public static HidInputKey BACKWARD = new HidInputKey(0, GLFW_KEY_S);
	public static HidInputKey LEFT = new HidInputKey(0, GLFW_KEY_A);
	public static HidInputKey RIGHT = new HidInputKey(0, GLFW_KEY_D);
}
