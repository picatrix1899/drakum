package org.drakum.anim;

import java.util.ArrayList;
import java.util.List;

public class BoneTrack
{
	public final String boneName;
	public final List<BoneKey> keys = new ArrayList<>();

	public BoneTrack(String boneName)
	{
		this.boneName = boneName;
	}
}
