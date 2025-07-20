package org.drakum.demo;

import java.lang.foreign.MemorySegment;

public class AttribFormats
{
	public static final POS3_COL3_TEX2 POS3_COL3_TEX2 = new POS3_COL3_TEX2();
	
	public static class POS3_COL3_TEX2 extends AttribFormat
	{
		private final SegmentHandle handlePos;
		private final SegmentHandle handleCol;
		private final SegmentHandle handleTex; 
		
		public POS3_COL3_TEX2()
		{
			super(AttribFormat.Type.VERTEX, AttribFormat.Packing.MINIMAL);
			
			addAttrib(AttribFormat.Format.VEC3_FLOAT32);
			addAttrib(AttribFormat.Format.VEC3_FLOAT32);
			addAttrib(AttribFormat.Format.VEC2_FLOAT32);
			
			compile();
			
			this.handlePos = getVarHandle(0);
			this.handleCol = getVarHandle(1);
			this.handleTex = getVarHandle(2);
		}
		
		public void setPos(MemorySegment seg, float x, float y, float z)
		{
			this.handlePos.setTup3F(seg, x, y, z);
		}
		
		public void setCol(MemorySegment seg, float x, float y, float z)
		{
			this.handleCol.setTup3F(seg, x, y, z);
		}
		
		public void setTex(MemorySegment seg, float x, float y)
		{
			this.handleTex.setTup2F(seg, x, y);
		}
		
		public int id()
		{
			return 1;
		}
	}
}
