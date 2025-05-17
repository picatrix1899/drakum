package org.drakum.demo.vkn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.system.MemoryStack;

public class VknPhysicalGPUList
{	
	private VknPhysicalGPU[] physicalGpus;
	
	public VknPhysicalGPU[] physicalGpus()
	{
		return this.physicalGpus;
	}
	
	public static VknPhysicalGPUList create(CreateSettings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			VknPhysicalGPU[] physicalGpus = VknInternalUtils.enumeratePhysicalDevices(CommonRenderContext.vkInstance.handle(), stack);

			record RatedGpu(int score, VknPhysicalGPU gpu) {}
			
			List<RatedGpu> suitablePhysicalGpus = new ArrayList<>();
			
			for(VknPhysicalGPU physGpu : physicalGpus)
			{
				boolean meetsRequirements = true;
				for(RequiredFeatureProcessor processor : settings.requirementProcessors)
				{
					if(!processor.supportsFeatures(physGpu))
					{
						meetsRequirements = false;
						break;
					}
				}

				if(!meetsRequirements) continue;

				int score = 0;
				for(RatingProcessor processor : settings.ratingProcessors)
				{
					score += processor.rate(physGpu);
				}
				
				RatedGpu ratedGpu = new RatedGpu(score, physGpu);
				
				suitablePhysicalGpus.add(ratedGpu);
			}
			
			suitablePhysicalGpus.sort(Comparator.comparingInt(RatedGpu::score).reversed());
			
			VknPhysicalGPU[] sortedPhysicalGpus = new VknPhysicalGPU[suitablePhysicalGpus.size()];
			
			for(int i = 0; i < suitablePhysicalGpus.size(); i++)
			{
				sortedPhysicalGpus[i] = suitablePhysicalGpus.get(i).gpu;
			}
			
			VknPhysicalGPUList result = new VknPhysicalGPUList();
			result.physicalGpus = sortedPhysicalGpus;
			
			return result;
		}
	}
	
	public static class CreateSettings
	{
		public final List<RequiredFeatureProcessor> requirementProcessors = new ArrayList<>(); 
		public final List<RatingProcessor> ratingProcessors = new ArrayList<>();
	}
	
	public static interface RequiredFeatureProcessor
	{
		boolean supportsFeatures(VknPhysicalGPU physicalGpu);
	}
	
	public static interface RatingProcessor
	{
		int rate(VknPhysicalGPU physicalGpu);
	}
}
