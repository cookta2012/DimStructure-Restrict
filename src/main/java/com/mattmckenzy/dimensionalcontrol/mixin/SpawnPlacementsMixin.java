package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin
{
    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void isSpawnPositionOk(EntityType<?> entityType, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        RegistryAccess registryAccess = level.registryAccess();

        ResourceLocation entityTypeLocation = registryAccess.registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);

        ResourceLocation dimensionLocation = (level instanceof ServerLevel) ? ((ServerLevel)level).dimension().location() :
                (level instanceof ServerLevelAccessor) ? ((ServerLevelAccessor)level).getLevel().dimension().location() :
                        null;

        if (entityTypeLocation == null)
        {
            callbackInfoReturnable.setReturnValue(false);
            return;
        }

        Rules.parseRules(Rule.Type.ENTITY, registryAccess);

        Rule rule = Rules.entityRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(entityTypeLocation))
        {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void checkSpawnRules(EntityType<T> entityType, ServerLevelAccessor serverLevel, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        RegistryAccess registryAccess = serverLevel.registryAccess();

        ResourceLocation entityTypeLocation = registryAccess.registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);

        ResourceLocation dimensionLocation = serverLevel.getLevel().dimension().location();

        if (entityTypeLocation == null)
        {
            callbackInfoReturnable.setReturnValue(false);
            return;
        }

        Rules.parseRules(Rule.Type.ENTITY, registryAccess);

        Rule rule = Rules.entityRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(entityTypeLocation))
        {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
