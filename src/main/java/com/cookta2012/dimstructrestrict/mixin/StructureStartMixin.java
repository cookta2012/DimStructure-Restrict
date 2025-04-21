package com.cookta2012.dimstructrestrict.mixin;

import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict;
import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
	                             RandomSource random, BoundingBox box, ChunkPos chunkPos, CallbackInfo cir) {
		//StructureStart something = ((StructureStart)(Object)true).
		Structure structure = ((StructureStart)(Object)this).getStructure();
	    ResourceLocation structId = worldGenLevel.registryAccess()
	        .registryOrThrow(Registries.STRUCTURE)
	        .getKey(structure);
	     ResourceKey<Level> dimKey = worldGenLevel.getLevel().dimension();
	    

	      //com.cookta2012.dimstructrestrict.DimensonalStructureFilter.LOGGER.info("Structure Name: " + structId);
	      //com.cookta2012.dimstructrestrict.DimensonalStructureFilter.LOGGER.info("World Name: " + dimKey);
	      
          if (structId == null) return;

          // 1) per‑structure rule
          StructureRule sRule = DimensionalStructureRestrict.STRUCT_RULES.get(structId);
          if (sRule != null) {
              applyStructureRule(sRule, cir, dimKey);
              return;
          }

          // 2) per‑dimension rule
          //ResourceKey<Level> dimKey = ctx.chunkGenerator().getLevel().dimension();
          DimensionRule dRule = DimensionalStructureRestrict.DIM_RULES.get(dimKey);
          if (dRule != null) {
              applyDimensionRule(dRule, structId, cir);
          }
        }
        

    private void applyStructureRule(StructureRule rule, CallbackInfo cir, ResourceKey<Level> dimKey) {
        //ResourceKey<Level> dim = ctx.chunkGenerator().getLevel().dimension();
        if (rule.mode() == Mode.WHITELIST) {
            if (!rule.dims().contains(dimKey)) {
                cir.cancel();
            }
        } else { // BLACKLIST
            if (rule.dims().contains(dimKey)) {
            	cir.cancel();
            }
        }
    }

    private void applyDimensionRule(DimensionRule rule, ResourceLocation structId, CallbackInfo cir) {
        if (rule.mode() == Mode.WHITELIST) {
            if (!rule.structures().contains(structId)) {
            	cir.cancel();
            }
        } else { // BLACKLIST
            if (rule.structures().contains(structId)) {
            	cir.cancel();
            }
        }
    }
}
