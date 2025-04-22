package com.cookta2012.dimstructrestrict;

import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public final class StructureRule extends Rule{

	public StructureRule(ResourceLocation id, Mode mode, Set<ResourceLocation> resource, Boolean false_place, Boolean active) {
		super(id, Rule.Type.STRUCTURE, mode, resource, false_place, active);
	}
	
}
