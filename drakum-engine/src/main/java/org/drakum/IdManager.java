package org.drakum;

import java.util.concurrent.atomic.AtomicLong;

public class IdManager
{
	public static final AtomicLong CURRENT_ENTITY_ID = new AtomicLong(1);
	public static final AtomicLong CURRENT_ENTITY_TEMPLATE_ID = new AtomicLong(1);
	
	public static long nextEntityId()
	{
		return CURRENT_ENTITY_ID.getAndIncrement();
	}
	
	public static long nextEntityIds(long count)
	{
		return CURRENT_ENTITY_ID.getAndAdd(count);
	}
	
	public static long nextEntityTemplateId()
	{
		return CURRENT_ENTITY_TEMPLATE_ID.getAndIncrement();
	}
	
	public static long nextEntityTemplateIds(long count)
	{
		return CURRENT_ENTITY_ID.getAndAdd(count);
	}
}
