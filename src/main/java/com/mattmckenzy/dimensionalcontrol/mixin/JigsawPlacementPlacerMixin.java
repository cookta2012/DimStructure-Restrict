package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.helpers.ThreadLocalContext;
import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.mattmckenzy.dimensionalcontrol.helpers.RegistryHelpers.getResourceLocations;

@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacementPlacerMixin
{
    @Inject(
            method = "tryPlacingChildren",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/CompoundTag;getInt(Ljava/lang/String;)I",
                    shift = At.Shift.AFTER
            ))
    private void tryPlacingChildren(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth, boolean useExpansionHack, LevelHeightAccessor level, RandomState random, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings, CallbackInfo ci, @Local List<StructurePoolElement> list)
    {
        ServerLevel serverLevel = ThreadLocalContext.getServerLevel();
        RegistryAccess registryAccess = serverLevel.registryAccess();

        ResourceLocation dimensionLocation = serverLevel.dimension().location();

        Registry<PlacedFeature> placedFeatureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);

        Rules.parseRules(Rule.Type.STRUCTURE_POOL_ELEMENT, registryAccess);

        List<StructurePoolElement> structurePoolElementsToRemove = new ArrayList<>();
        for(StructurePoolElement structurePoolElement : list)
        {
            Rule rule = Rules.structurePoolElementRules.get(dimensionLocation);
            for(ResourceLocation structurePoolElementLocation : getResourceLocations(structurePoolElement, placedFeatureRegistry))
            {
                if (rule != null && rule.isRestricted(structurePoolElementLocation))
                {
                    structurePoolElementsToRemove.add(structurePoolElement);
                    break;
                }
            }
        }
        list.removeAll(structurePoolElementsToRemove);
    }
}
