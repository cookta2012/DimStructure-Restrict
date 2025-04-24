package com.cookta2012.dimstructrestrict.mixin;

import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict;
import com.cookta2012.dimstructrestrict.Rule;
import com.cookta2012.dimstructrestrict.Rule.Mode;
import com.cookta2012.dimstructrestrict.Rule.Type;

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
		
		//StructureStart something = ((StructureStart)(Object)true).
		Structure structure = ((StructureStart)(Object)this).getStructure();
	    ResourceLocation structId = worldGenLevel.registryAccess()
	        .registryOrThrow(Registries.STRUCTURE)
	        .getKey(structure);
	      ResourceLocation dimKey = worldGenLevel.getLevel().dimension().location();
	      
          if (structId == null) return;

          // 1) per‑structure rule
          Rule sRule = DimensionalStructureRestrict.STRUCTURE_RULES.get(structId);
          if (sRule != null && (sRule.isRestricted( dimKey, false ) || isRestricted(sRule, dimKey, false ))) {
        	  ci.cancel();
              return;
          }

          // 2) per‑dimension rule
          //ResourceKey<Level> dimKey = ctx.chunkGenerator().getLevel().dimension();
          Rule dRule = DimensionalStructureRestrict.DIMENSION_RULES.get(dimKey);
          if (dRule != null && (dRule.isRestricted( structId, false ) || isRestricted(dRule, structId, false ))) {
      		 //throw new IllegalStateException("Attempted to return invalid m,m,m,m");
        	  ci.cancel();
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
        DimensionalStructureRestrict.logMsg("Fell Through Processing Rule INSIDE STRUCTURE START");
        DimensionalStructureRestrict.logMsg("---START What Should Have Been INSIDE STRUCTURE START--------");
		logDebugMessage(rule,  targetResource, isClean );
		DimensionalStructureRestrict.logMsg("---END What Should Have Been INSIDE CHUNK STRUCTURE START-------");
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
	    msg.append("INSIDE STRUCTURE GENERATOR CLASS: ")
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
