package org.drakum.anim;

import java.util.ArrayList;
import java.util.List;

public class Animation
{
	public final String name;
	public final float duration;
	public final float ticksPerSecond;
	public final List<BoneTrack> tracks = new ArrayList<>();

	public Animation(String name, float duration, float ticksPerSecond)
	{
		this.name = name;
		this.duration = duration;
		this.ticksPerSecond = ticksPerSecond;
	}
}
