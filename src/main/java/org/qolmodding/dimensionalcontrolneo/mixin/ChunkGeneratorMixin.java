package org.qolmodding.dimensionalcontrolneo.mixin;

import org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.qolmodding.dimensionalcontrolneo.Rule;


@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
	@Inject(
		    method = "tryGenerateStructure",
		    at = @At("HEAD"),
		    cancellable = true
		)
		private void onTryGenerateStructure(
		    StructureSet.StructureSelectionEntry entry,
		    StructureManager manager,
		    RegistryAccess registryAccess,
		    RandomState random,
		    StructureTemplateManager templateManager,
		    long seed,
		    ChunkAccess chunk,
		    ChunkPos chunkPos,
		    SectionPos sectionPos,
		    CallbackInfoReturnable<Boolean> cir
		) {
		    Structure structure = entry.structure().value();
		    ResourceLocation structureId = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(structure);
		    LevelAccessor level = ((StructureManagerAccessor)manager).getLevel();
		    ResourceLocation dimension = ((WorldGenLevel)level).getLevel().dimension().location();
		    //DimensionalControlNeo.LOGGER.debug("HIT structure: " + structureId + " in Dimension: " + dimension);
		    // dimension/structure filter logic
		    Rule sRule = DimensionalControlNeo.STRUCTURE_RULES.get(structureId);
		    if (sRule != null) {
		    	if( sRule.isRestricted(dimension, true)) {
		        	  if (DimensionalControlNeo.isDebug()) {
		        		  DimensionalControlNeo.logDebugMsg("Cancelled ChunkGenerator.onTryGenerateStructure for Structure: " + structureId.toString() + " for Dimension: " + dimension.toString());
		        	  }
			        cir.setReturnValue(false);
			        return;
		    	}
		        return;
		    }

		    Rule dRule = DimensionalControlNeo.DIMENSION_RULES.get(dimension);
		    if (dRule != null && dRule.isRestricted(structureId, true)) {
	        	  if (DimensionalControlNeo.isDebug()) {
	        		  DimensionalControlNeo.logDebugMsg("Cancelled ChunkGenerator.onTryGenerateStructure for Dimension: " + dimension.toString() + " for Structure: " + structureId.toString());
	        	  }
		        cir.setReturnValue(false);
		        return;
		    }
		}
}





