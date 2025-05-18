package org.qolmodding.dimensionalcontrolneo.mixin;

import org.jetbrains.annotations.NotNull;
import org.qolmodding.dimensionalcontrolneo.helpers.ThreadLocalContext;
import org.qolmodding.dimensionalcontrolneo.implementation.Rule;
import org.qolmodding.dimensionalcontrolneo.implementation.Rules;

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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import  org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.qolmodding.dimensionalcontrolneo.helpers.RegistryHelpers.getResourceLocations;




@Mixin(targets = "net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer")
public abstract class JigsawPlacementPlacerMixin
{
    @Unique
    List<StructurePoolElement> list;
    // Capture the `list` local right before the addAll() call
    @ModifyVariable(
            method = "tryPlacingChildren",
            at = @At(
                    value  = "INVOKE",
                    target = "java/util/List.addAll(Ljava/util/Collection;)Z",
                    shift  = At.Shift.BEFORE
            ),
            ordinal = 0  // the first List<StructurePoolElement> local
    )
    private List<StructurePoolElement> captureList(List<StructurePoolElement> list) {
        this.list = list;
        return list;
    }

    @Inject(
            method = "tryPlacingChildren",
            at = @At(
                    value = "INVOKE",
                    target = "java/util/List.addAll(Ljava/util/Collection;)Z",
                    shift = At.Shift.AFTER
            )
    )
    //           tryPlacingChildren(PoolElementStructurePiece pPiece, MutableObject<VoxelShape> pFree, int pDepth, boolean pUseExpansionHack, LevelHeightAccessor pLevel, RandomState pRandomState)
    private void tryPlacingChildren(PoolElementStructurePiece pPiece, MutableObject<VoxelShape> pFree, int pDepth, boolean pUseExpansionHack, LevelHeightAccessor pLevel, RandomState pRandomState, CallbackInfo ci)
    {
        ServerLevel serverLevel = ThreadLocalContext.getServerLevel();
        RegistryAccess registryAccess = serverLevel.registryAccess();

        ResourceLocation dimensionLocation = serverLevel.dimension().location();

        Registry<PlacedFeature> placedFeatureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);

        Rules.parseRules(Rule.Type.STRUCTURE_POOL_ELEMENT, registryAccess);

        List<StructurePoolElement> structurePoolElementsToRemove = new ArrayList<>();
        for(StructurePoolElement structurePoolElement : this.list)
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
        this.list.removeAll(structurePoolElementsToRemove);
    }
}
