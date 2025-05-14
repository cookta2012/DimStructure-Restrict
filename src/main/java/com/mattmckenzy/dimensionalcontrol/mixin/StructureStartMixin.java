package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;

import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.RegistryAccess;
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
public abstract class StructureStartMixin
{
    @Inject(method = "placeInChunk", at = @At("HEAD"), cancellable = true)
    private void placeInChunkHead(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator generator,
                              RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo callbackInfo)
    {
        RegistryAccess registryAccess = worldGenLevel.registryAccess();

        Structure structure = ((StructureStart) (Object) this).getStructure();
        ResourceLocation structureLocation = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(structure);
        ResourceLocation dimensionLocation = worldGenLevel.getLevel().dimension().location();

        if (structureLocation == null) return;

        Rules.parseRules(Rule.Type.STRUCTURE, registryAccess);

        Rule rule = Rules.structureRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(structureLocation))
        {
            callbackInfo.cancel();
        }
    }
}
