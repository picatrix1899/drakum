package org.drakum.demo;

import java.util.Objects;

public class PipelineKey
{
	public long attribFormatId;
	public long materialTypeId;
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.attribFormatId, this.materialTypeId);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		if(obj == this) return true;
		
		if(obj instanceof PipelineKey key) {
			if(key.attribFormatId != this.attribFormatId) return false;
			if(key.materialTypeId != this.materialTypeId) return false;
			
			return true;
		}
		
		return false;
	}
}
