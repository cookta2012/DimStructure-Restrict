package org.qolmodding.dimensionalcontrolneo;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public final class DimensionRule extends Rule{

	public DimensionRule(ResourceLocation id, Mode mode, Set<ResourceLocation> resource, Boolean false_place, Boolean active) {
		super(id, Rule.Type.DIMENSION, mode, resource, false_place, active);
	}
	
}
