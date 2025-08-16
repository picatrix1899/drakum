package org.drakum.demo.registry;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.concurrent.atomic.AtomicLong;

import it.unimi.dsi.fastutil.longs.Long2LongMap;

public class HandleRegistry
{
	public static final LongRegistry PIPELINE = new LongRegistry();
	public static final LongRegistry PIPELINE_LAYOUT = new LongRegistry();
	public static final LongRegistry RENDERPASS = new LongRegistry();
	public static final LongRegistry BUFFER = new LongRegistry();

	public static class LongRegistry
	{
		private Long2LongMap handles = new Long2LongOpenHashMap();
		private AtomicLong currentId = new AtomicLong(1);
		
		public LongId register(long handle)
		{
			long id = currentId.getAndIncrement();
			
			handles.put(id, handle);
			
			return new LongId(id);
		}
		
		public long get(LongId id)
		{
			return handles.get(id.handle());
		}
		
		public void remove(LongId id)
		{
			handles.remove(id.handle());
		}
	}
}
