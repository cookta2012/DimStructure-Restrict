package org.qolmodding.dimensionalcontrolneo.mixin;

import org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo;
import org.qolmodding.dimensionalcontrolneo.Rule;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.StructureManager;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
	@Inject(method = "placeInChunk", at = @At("HEAD"), cancellable = true)
	private void placeInChunk(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator generator,
	                             RandomSource random, BoundingBox box, ChunkPos chunkPos, CallbackInfo ci) {
		
		Structure structure = ((StructureStart)(Object)this).getStructure();
	    ResourceLocation structureId = worldGenLevel.registryAccess()
	        .registryOrThrow(Registries.STRUCTURE)
	        .getKey(structure);
	      ResourceLocation dimension = worldGenLevel.getLevel().dimension().location();
	      
          if (structureId == null) return;

          // 1) per‑structure rule
          Rule sRule = DimensionalControlNeo.STRUCTURE_RULES.get(structureId);
		    if (sRule != null) {
		    	if( sRule.isRestricted(dimension, true)) {
		        	  if (DimensionalControlNeo.isDebug()) {
        		  DimensionalControlNeo.logDebugMsg("Cancelled StructureStart.placeInChunk for Structure: " + structureId.toString() + " for Dimension: " + dimension.toString());
        	  }
        	  ci.cancel();
		        return;
	    	}
	        return;
          }
          

          // 2) per‑dimension rule
          //ResourceKey<Level> dimension = ctx.chunkGenerator().getLevel().dimension();
          Rule dRule = DimensionalControlNeo.DIMENSION_RULES.get(dimension);
          if (dRule != null && dRule.isRestricted( structureId, false )) {
      		 //throw new IllegalStateException("Attempted to return invalid m,m,m,m");
        	  if (DimensionalControlNeo.isDebug()) {
        		  DimensionalControlNeo.logDebugMsg("Cancelled StructureStart.placeInChunk for Dimension: " + dimension.toString() + " for Structure: " + structureId.toString());
        	  }
        	  ci.cancel();
              return;
          }
		 
        }
}
