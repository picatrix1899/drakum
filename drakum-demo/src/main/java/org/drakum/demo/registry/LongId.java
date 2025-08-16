package org.drakum.demo.registry;

import org.drakum.demo.VknObjectType;
import org.drakum.demo.vkn.VknContext;

public class LongId
{
	private long handle;
	private HandleState state;
	
	public LongId(long handle)
	{
		this.handle = handle;
		this.state = handle == 0 ? HandleState.INVALID : HandleState.VALID;
	}
	
	public long handle()
	{
		return this.handle;
	}
	
	public boolean isValid()
	{
		return this.state == HandleState.VALID;
	}
	
	public void markDestroyed()
	{
		this.state = HandleState.DESTROYED;
	}
	
	public void markInvalid()
	{
		if(this.state == HandleState.DESTROYED) return;
			
		this.state = HandleState.INVALID;
	}
	
	public void ensureValid(VknObjectType type)
	{
		if(!VknContext.OBJECT_VALIDATION || isValid()) return;
		
		switch(state)
		{
			case INVALID: throw new ResourceInvalidException("Object is invalid");
			case DESTROYED: throw new ResourceDestroyedException("Object is already destroyed");
			default: return;
		}
	}
}
