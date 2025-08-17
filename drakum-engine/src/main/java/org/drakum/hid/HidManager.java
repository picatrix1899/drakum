package org.drakum.hid;

import java.util.ArrayList;
import java.util.List;

public class HidManager
{
	private static List<IHidProvider> providers = new ArrayList<>();
	
	public static void registerProvider(IHidProvider provider)
	{
		providers.add(provider);
	}
	
	public static void update()
	{
		for(IHidProvider provider : providers)
		{
			provider.update();
		}
	}
}
