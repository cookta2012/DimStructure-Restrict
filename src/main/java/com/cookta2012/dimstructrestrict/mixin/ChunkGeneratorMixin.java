package com.cookta2012.dimstructrestrict.mixin;

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

import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict;
import com.cookta2012.dimstructrestrict.Rule;
import com.cookta2012.dimstructrestrict.Rule.Mode;
import com.cookta2012.dimstructrestrict.Rule.Type;


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
		    //DimensionalStructureRestrict.LOGGER.debug("HIT structure: " + structureId + " in Dimension: " + dimension);
		    // dimension/structure filter logic
		    Rule sRule = DimensionalStructureRestrict.STRUCTURE_RULES.get(structureId);
		    if (sRule != null && (sRule.isRestricted(dimension, true) || isRestricted( sRule, dimension, true ))) {
		        cir.setReturnValue(false);
		        return;
		    }

		    Rule dRule = DimensionalStructureRestrict.DIMENSION_RULES.get(dimension);
		    if (dRule != null && (dRule.isRestricted(structureId, true) || isRestricted( dRule, structureId, true ))) {
		        cir.setReturnValue(false);
		        return;
		    }
		}
	public Boolean isRestricted(Rule rule, ResourceLocation targetResource, Boolean isClean) {
		
		DimensionalStructureRestrict.logMsg("Begin Processing Rule");
		// Short circuit due to rule inactive or false_place active
		if(!rule.active) { 
			DimensionalStructureRestrict.logMsg("Rule not active" + rule.type.toString());
			return false; 
		}
		if(rule.false_place && !isClean) {
			DimensionalStructureRestrict.logMsg("Rule set to false_place");
			return false;
		}
        if (rule.mode == Mode.WHITELIST) {
            if (!rule.resource().contains(targetResource)) {
            	logDebugMessage(rule, targetResource, isClean );
                return true;
            }
        } else { // BLACKLIST
            if (rule.resource().contains(targetResource)) {
            	logDebugMessage(rule, targetResource, isClean );
            	return true;
            }
        }
        DimensionalStructureRestrict.logMsg("Fell Through Processing Rule INSIDE CHUNK GENERATOR");
        DimensionalStructureRestrict.logMsg("---START What Should Have Been INSIDE CHUNK GENERATOR--------");
		logDebugMessage(rule,  targetResource, isClean );
		DimensionalStructureRestrict.logMsg("---END What Should Have Been INSIDE CHUNK GENERATOR-------");
        return false;
    }
	
	private void logDebugMessage(Rule rule, ResourceLocation targetResource, Boolean isClean) {
	    String ruleType = rule.getType(); // "Dimension" or "Structure"
	    String modeText = rule.getMode(); // "WHITELIST" or "BLACKLIST"
	    String preventionType = isClean ? "cleanly" : "uncleanly";

	    String blockedInDimension = (rule.type == Type.DIMENSION) ? rule.id.toString() : targetResource.toString() ;
	    String blockedResource = (rule.type == Type.STRUCTURE) ?  rule.id.toString() : targetResource.toString();

	    String reason = (rule.mode == Mode.WHITELIST)
	        ? "not being whitelisted"
	        : "being blacklisted";

	    StringBuilder msg = new StringBuilder();
	    msg.append("INSIDE CHUNK GENERATOR CLASS: ")
	       .append(ruleType)
	       .append(" Rule ")
	       .append(modeText)
	       .append(" ")
	       .append(preventionType)
	       .append(" prevented generation of Structure: ")
	       .append(blockedResource)
	       .append(" in Dimension: ")
	       .append(blockedInDimension)
	       .append(" due to ")
	       .append(reason);

	    DimensionalStructureRestrict.logMsg(msg.toString());
	}
}





