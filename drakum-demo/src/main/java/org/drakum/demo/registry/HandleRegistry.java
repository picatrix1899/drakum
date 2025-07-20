package org.drakum.demo.registry;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.concurrent.atomic.AtomicLong;

import it.unimi.dsi.fastutil.longs.Long2LongMap;

public class HandleRegistry
{
	private static Long2LongMap longHandles = new Long2LongOpenHashMap();
	private static AtomicLong current = new AtomicLong(1);
	
	public static long getLong(long id)
	{
		return longHandles.get(id);
	}
	
	public static long registerLong(long handle)
	{
		long id = current.getAndIncrement();
		
		longHandles.put(id, handle);
		
		return id;
	}
	
	public static void removeLong(long id)
	{
		longHandles.remove(id);
	}
}
