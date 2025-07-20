package org.drakum.demo.registry;

public class ResourceInvalidException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ResourceInvalidException()
	{
        super();
    }

    public ResourceInvalidException(String message)
    {
        super(message);
    }
}
