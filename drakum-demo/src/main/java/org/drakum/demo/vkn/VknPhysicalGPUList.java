package org.drakum.demo.vkn;

import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.system.MemoryStack;

public class VknPhysicalGPUList
{	
	private final VknContext context;
	
	private VknPhysicalGPU[] physicalGpus;
	
	public VknPhysicalGPU[] physicalGpus()
	{
		return this.physicalGpus;
	}
	
	public VknPhysicalGPUList(Settings settings)
	{
		try(MemoryStack stack = MemoryStack.stackPush())
		{
			this.context = settings.context;
			
			VknPhysicalGPU[] physicalGpus = VknInternalUtils.enumeratePhysicalDevices(this.context.instance.handle(), stack);

			record RatedGpu(int score, VknPhysicalGPU gpu) {}
			
			List<RatedGpu> suitablePhysicalGpus = new ArrayList<>();
			
			List<RequiredFeatureProcessor> requirementProcessors = new ArrayList<>(); 
			
			requirementProcessors.add((gpu) -> {
				boolean state = true;
				
				state = state && gpu.deviceFeatures().geometryShader == true;
				
				return state;
			});
			
			requirementProcessors.addAll(settings.requirementProcessors);
			
			List<RatingProcessor> ratingProcessors = new ArrayList<>();
			
			ratingProcessors.add((gpu) -> {
				int score = 0;
				
				if(gpu.deviceProperties().deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) score += 1000;
				
				return score;
			});
			
			ratingProcessors.addAll(settings.ratingProcessors);
			
			for(VknPhysicalGPU physGpu : physicalGpus)
			{
				boolean meetsRequirements = true;
				for(RequiredFeatureProcessor processor : requirementProcessors)
				{
					if(!processor.supportsFeatures(physGpu))
					{
						meetsRequirements = false;
						break;
					}
				}

				if(!meetsRequirements) continue;

				int score = 0;
				for(RatingProcessor processor : ratingProcessors)
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
			
			this.physicalGpus = sortedPhysicalGpus;
		}
	}
	
	public static class Settings
	{
		private final VknContext context;
		
		public final List<RequiredFeatureProcessor> requirementProcessors = new ArrayList<>(); 
		public final List<RatingProcessor> ratingProcessors = new ArrayList<>();
		
		public Settings(VknContext context)
		{
			this.context = context;
		}
		
		public VknContext context()
		{
			return this.context;
		}
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
