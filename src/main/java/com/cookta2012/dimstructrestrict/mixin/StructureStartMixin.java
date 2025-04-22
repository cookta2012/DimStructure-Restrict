package com.cookta2012.dimstructrestrict.mixin;

import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict;
import com.cookta2012.dimstructrestrict.Rule;

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
//import org.slf4j.Logger;

@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
	//private static final Logger LOGGER = com.cookta2012.dimstructrestrict.DimensionalStructureRestrict.LOGGER;
	@Inject(method = "placeInChunk", at = @At("HEAD"), cancellable = true)
	private void placeInChunk(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator generator,
	                             RandomSource random, BoundingBox box, ChunkPos chunkPos, CallbackInfo ci) {
		//StructureStart something = ((StructureStart)(Object)true).
		Structure structure = ((StructureStart)(Object)this).getStructure();
	    ResourceLocation structId = worldGenLevel.registryAccess()
	        .registryOrThrow(Registries.STRUCTURE)
	        .getKey(structure);
	      ResourceLocation dimKey = worldGenLevel.getLevel().dimension().location();
	      
          if (structId == null) return;

          // 1) per‑structure rule
          Rule sRule = DimensionalStructureRestrict.STRUCTURE_RULES.get(structId);
          if (sRule != null && sRule.isRestricted( structId, false )) {
        	  ci.cancel();
              return;
          }

          // 2) per‑dimension rule
          //ResourceKey<Level> dimKey = ctx.chunkGenerator().getLevel().dimension();
          Rule dRule = DimensionalStructureRestrict.DIMENSION_RULES.get(dimKey);
          if (dRule != null && dRule.isRestricted( structId, false ) ) {
        	  ci.cancel();
              return;
          }
        }
        
}
