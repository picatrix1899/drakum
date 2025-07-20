package org.drakum.demo.registry;

public class ResourceDestroyedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ResourceDestroyedException()
	{
        super();
    }

    public ResourceDestroyedException(String message)
    {
        super(message);
    }
}
