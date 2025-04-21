package com.cookta2012.dimstructrestrict.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.FeatureSorter.StepFeatureData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict;
import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict.DimensionRule;
import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict.Mode;
import com.cookta2012.dimstructrestrict.DimensionalStructureRestrict.StructureRule;

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
		    ResourceLocation id = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(structure);
		    LevelAccessor level = ((StructureManagerAccessor)manager).getLevel();
		    ResourceKey<Level> dimKey = ((WorldGenLevel)level).getLevel().dimension();

		    // dimension/structure filter logic
		    StructureRule sRule = DimensionalStructureRestrict.STRUCT_RULES.get(id);
		    if (sRule != null && isDimensionRestrictedInStructureRule(sRule, dimKey)) {
		        DimensionalStructureRestrict.LOGGER.debug("Structure Rule prevented generation of structure: " + id + " in Dimension: " + dimKey);
		        cir.setReturnValue(false);
		        return;
		    }

		    DimensionRule dRule = DimensionalStructureRestrict.DIM_RULES.get(dimKey);
		    if (dRule != null && isStructureRestrictedInDimensionRule(dRule, id)) {
		        DimensionalStructureRestrict.LOGGER.debug("Dimension Rule prevented generation of structure: " + id + " in Dimension: " + dimKey);
		        cir.setReturnValue(false);
		        return;
		    }
		}

	/*
	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"), cancellable = false)
	public void applyBiomeDecorationHead(WorldGenLevel pLevel, ChunkAccess pChunk, StructureManager pStructureManager, CallbackInfo ci) {
		DimensionalStructureRestrict.LOGGER.info("Beginning of applyBiomeDecoration");
	}
	@Inject(method = "applyBiomeDecoration", at = @At("TAIL"), cancellable = false)
	public void applyBiomeDecorationTail(WorldGenLevel pLevel, ChunkAccess pChunk, StructureManager pStructureManager, CallbackInfo ci) {
		DimensionalStructureRestrict.LOGGER.info("End of applyBiomeDecoration");
	}
	*/
	/*
	@Inject(
		    method = "applyBiomeDecoration",
		    at = @At(
		        value = "INVOKE",
		        target = "Ljava/util/Set;retainAll(Ljava/util/Collection;)Z",
		        shift = At.Shift.AFTER
		    ),
		    locals = LocalCapture.CAPTURE_FAILSOFT
		)
		private void afterRetainAll(
		    WorldGenLevel level,
		    ChunkAccess chunk,
		    StructureManager structureManager,
		    CallbackInfo ci,
		    ChunkPos chunkpos,
		    SectionPos sectionpos,
		    BlockPos blockpos,
		    Registry<Structure> structureRegistry,
		    Map<Integer, List<Structure>> structureMap,
		    List<FeatureSorter.StepFeatureData> list,
		    WorldgenRandom random,
		    long decorationSeed,
		    Set<Holder<Biome>> set // <- this is the 'set' being retainAll'd
		) {
			DimensionalStructureRestrict.LOGGER.info("Biomes after retainAll: " + set);

		    // Example mutation: add or remove from the set if you want
		    // set.removeIf(...); or set.add(...);
		}

	private static final ThreadLocal<ResourceKey<Level>> CURRENT_DIM = new ThreadLocal<>();
	private static final ThreadLocal<ResourceLocation> CURRENT_STRUCTURE = new ThreadLocal<>();
	
	@Inject(
		    method = "applyBiomeDecoration",
		    at = @At(
		        value = "INVOKE",
		        target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setFeatureSeed(JII)V",
		        ordinal = 0 // first occurrence
		    ),
		    locals = LocalCapture.CAPTURE_FAILHARD
		)
		private void beforeSetFeatureSeed(
		    WorldGenLevel level,
		    ChunkAccess chunk,
		    StructureManager structureManager,
		    CallbackInfo ci,
		    ChunkPos chunkpos,
		    SectionPos sectionpos,
		    BlockPos blockpos,
		    Registry<Structure> structureRegistry,
		    Map<Integer, List<Structure>> structureMap,
		    List<FeatureSorter.StepFeatureData> list,
		    WorldgenRandom random,
		    long decorationSeed,
		    Set<Holder<Biome>> biomeSet,
		    int i,
		    Registry<Structure> regStruct,
		    int j,
		    int k,
		    int r,
		    Iterator<Structure> structureIterator,    
		    Structure structure // ✅ structure comes *last*
		) {
	    	ResourceLocation structId = level.registryAccess()
		        .registryOrThrow(Registries.STRUCTURE)
		        .getKey(structure);
		    
		    CURRENT_STRUCTURE.set(structId); 
			DimensionalStructureRestrict.LOGGER.info("Injecting before setFeatureSeed: l = ");
			DimensionalStructureRestrict.LOGGER.info("Injecting before setFeatureSeed: STRUCTURE " + structId);
		    
		}
	
	@Redirect(
		    method = "applyBiomeDecoration",
		    at = @At(
		        value = "INVOKE",
		        target = "Lnet/minecraft/world/level/WorldGenLevel;setCurrentlyGenerating(Ljava/util/function/Supplier;)V",
		        ordinal = 0
		    )
		)
		private void redirectSetCurrentlyGenerating(
		    WorldGenLevel level,
		    Supplier<String> supplier
		) {
		    ResourceKey<Level> dimKey = level.getLevel().dimension();
		    // Restriction logic
		    CURRENT_DIM.set(dimKey);
			ResourceLocation structId = CURRENT_STRUCTURE.get();
		    // Parse the structure from the supplier string (if you can)
		    String name = supplier.get();
		    
		    if (structId == null) {
		    	DimensionalStructureRestrict.LOGGER.debug("Name:  is " + name);
		    	DimensionalStructureRestrict.LOGGER.debug("structId is null and this should not happen.......");
		        level.setCurrentlyGenerating(supplier); // fallback
		        return;
		    }


		    StructureRule sRule = DimensionalStructureRestrict.STRUCT_RULES.get(structId);
		    if (sRule != null && isDimensionRestrictedInStructureRule(sRule, dimKey)) {
		    	DimensionalStructureRestrict.LOGGER.debug("Structure Rule prevented setCurrentlyGenerating of structure: " + structId + " in Dimension: " + dimKey);
		    	return;
		    }

		    DimensionRule dRule = DimensionalStructureRestrict.DIM_RULES.get(dimKey);
		    if (dRule != null && isStructureRestrictedInDimensionRule(dRule, structId)) {
		    	DimensionalStructureRestrict.LOGGER.debug("Dimension Rule prevented setCurrentlyGenerating of structure: " + structId + " in Dimension: " + dimKey);
		    	return;
		    }

		    // Only call if allowed
		    DimensionalStructureRestrict.LOGGER.debug("Attempting to setCurrentlyGenerating structure: " + structId + " in Dimension: " + dimKey);
		    // Only call if allowed
		    level.setCurrentlyGenerating(supplier);
		}
	
	@Redirect(
		    method = "applyBiomeDecoration",
		    at = @At(
		        value = "INVOKE",
		        target = "Lnet/minecraft/world/level/StructureManager;startsForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;)Ljava/util/List;"

		    )
		)
		private List<StructureStart> redirectStartsForStructure(
		    StructureManager structureManager,
		    SectionPos sectionPos,
		    Structure structure
		) {
			ResourceKey<Level> dimKey = CURRENT_DIM.get();
			CURRENT_DIM.remove();
			ResourceLocation structId = CURRENT_STRUCTURE.get();
			CURRENT_STRUCTURE.remove();

		    // Restriction logic
		    StructureRule sRule = DimensionalStructureRestrict.STRUCT_RULES.get(structId);
		    if (sRule != null && isDimensionRestrictedInStructureRule(sRule, dimKey)) {
		    	DimensionalStructureRestrict.LOGGER.debug("Structure Rule prevented startsForStructure of structure: " + structId + " in Dimension: " + dimKey);
		    	return List.of();
		    }

		    DimensionRule dRule = DimensionalStructureRestrict.DIM_RULES.get(dimKey);
		    if (dRule != null && isStructureRestrictedInDimensionRule(dRule, structId)) {
		    	DimensionalStructureRestrict.LOGGER.debug("Dimension Rule prevented startsForStructure of structure: " + structId + " in Dimension: " + dimKey);
		    	return List.of();
		    }

		    // Only call if allowed
		    DimensionalStructureRestrict.LOGGER.debug("Attempting to place structure: " + structId + " in Dimension: " + dimKey);
		    return structureManager.startsForStructure(sectionPos, structure);
		}

	
	/*


	@Inject(
		    method = "applyBiomeDecoration",
		    at = @At(
		        value = "INVOKE",
		        target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setFeatureSeed(JII)V",
		        ordinal = 0 // first occurrence
		    ),
		    locals = LocalCapture.CAPTURE_FAILHARD
		)
		private void beforeSetFeatureSeed(
		    WorldGenLevel level,
		    ChunkAccess chunk,
		    StructureManager structureManager,
		    CallbackInfo ci,
		    ChunkPos chunkpos,
		    SectionPos sectionpos,
		    BlockPos blockpos,
		    Registry<Structure> structureRegistry,
		    Map<Integer, List<Structure>> structureMap,
		    List<FeatureSorter.StepFeatureData> list,
		    WorldgenRandom random,
		    long decorationSeed,
		    Set<Holder<Biome>> biomeSet,
		    int i,
		    Registry<Structure> regStruct,
		    int j,
		    int k,
		    int r,
		    Iterator<Structure> structureIterator,    
		    Structure structure // ✅ structure comes *last*
		) {
	    	ResourceLocation structId = level.registryAccess()
		        .registryOrThrow(Registries.STRUCTURE)
		        .getKey(structure);
		    ResourceKey<Level> dimKey = level.getLevel().dimension();
		    
	          // 1) per‑structure rule
	          StructureRule sRule = DimensionalStructureRestrict.STRUCT_RULES.get(structId);
	          if (sRule != null && isDimensionRestrictedInStructureRule(sRule, dimKey)) {
	        	  continue;
	          }
	          // 2) per‑dimension rule
	          //ResourceKey<Level> dimKey = ctx.chunkGenerator().getLevel().dimension();
	          DimensionRule dRule = DimensionalStructureRestrict.DIM_RULES.get(dimKey);
	          if (dRule != null && isStructureRestrictedInDimensionRule(dRule, structId)) {
	        	  continue;
	          }
			DimensionalStructureRestrict.LOGGER.info("Injecting before setFeatureSeed: l = ");
			DimensionalStructureRestrict.LOGGER.info("Injecting before setFeatureSeed: STRUCTURE " + structure);
		    
		}
	*/


	private Boolean isDimensionRestrictedInStructureRule(StructureRule rule, ResourceKey<Level> dimKey) {
	        //ResourceKey<Level> dim = ctx.chunkGenerator().getLevel().dimension();
	        if (rule.mode() == Mode.WHITELIST) {
	            if (!rule.dims().contains(dimKey)) {
	                return true;
	            }
	        } else { // BLACKLIST
	            if (rule.dims().contains(dimKey)) {
	            	return true;
	            }
	        }
	        return false;
	    }

	    private Boolean isStructureRestrictedInDimensionRule(DimensionRule rule, ResourceLocation structId) {
	        if (rule.mode() == Mode.WHITELIST) {
	            if (!rule.structures().contains(structId)) {
	            	return true;
	            }
	        } else { // BLACKLIST
	            if (rule.structures().contains(structId)) {
	            	return true;
	            }
	        }
	        return false;
	    }


}





