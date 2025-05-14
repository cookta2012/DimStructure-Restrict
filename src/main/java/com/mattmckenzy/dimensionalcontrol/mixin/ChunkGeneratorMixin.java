package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.helpers.ThreadLocalContext;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import com.mattmckenzy.dimensionalcontrol.mixin.accessors.StructureManagerAccessor;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin
{
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void onTryGenerateStructureHead(
            StructureSet.StructureSelectionEntry entry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager templateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            CallbackInfoReturnable<Boolean> callbackInfoReturnable
    )
    {
        Structure structure = entry.structure().value();
        ResourceLocation structureLocation = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey(structure);
        ServerLevel level = ((WorldGenLevel) ((StructureManagerAccessor) structureManager).getLevel()).getLevel();
        ThreadLocalContext.setServerLevel(level);

        ResourceLocation dimensionLocation = level.dimension().location();

        if (structureLocation == null)
        {
            callbackInfoReturnable.setReturnValue(false);
            return;
        }

        Rules.parseRules(Rule.Type.STRUCTURE, registryAccess);
        Rule rule = Rules.structureRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(structureLocation))
        {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "tryGenerateStructure", at = @At("RETURN"))
    private void onTryGenerateStructureReturn(
            StructureSet.StructureSelectionEntry entry,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager templateManager,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            CallbackInfoReturnable<Boolean> callbackInfoReturnable
    ){
        ThreadLocalContext.clear();
    }
}





