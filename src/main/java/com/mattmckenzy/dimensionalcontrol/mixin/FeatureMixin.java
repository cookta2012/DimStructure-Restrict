package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Feature.class)
public abstract class FeatureMixin<FC extends FeatureConfiguration>
{
    @Inject(method = "place(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void place(FC config, WorldGenLevel level, ChunkGenerator chunkGenerator,
                       RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        RegistryAccess registryAccess = level.registryAccess();

        @SuppressWarnings("unchecked")
        Feature<FC> feature = (Feature<FC>) (Object) this;
        ResourceLocation featureLocation = registryAccess.registryOrThrow(Registries.FEATURE).getKey(feature);
        ResourceLocation dimensionLocation = level.getLevel().dimension().location();

        if (featureLocation == null)
        {
            callbackInfoReturnable.setReturnValue(false);
            return;
        }

        Rules.parseRules(Rule.Type.FEATURE, registryAccess);

        Rule rule = Rules.featureRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(featureLocation))
        {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
