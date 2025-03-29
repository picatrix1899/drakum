package org.drakum.demo;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK14.*;

import org.lwjgl.vulkan.VkExtent2D;

public class WindowRenderContext
{
	public Window window;
	public long surface;
	public Swapchain swapchain;
	public int swapchainFormat = VK_FORMAT_B8G8R8A8_SRGB;
	public int swapchainColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
	public int swapchainImageCount;
	public VkExtent2D framebufferExtent;
}
