package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin implements EquipmentUser, Leashable, Targeting
{
    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void checkMobSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        RegistryAccess registryAccess = level.registryAccess();

        ResourceLocation entityTypeLocation = registryAccess.registryOrThrow(Registries.ENTITY_TYPE).getKey(type);
        ResourceLocation dimensionLocation = ((WorldGenLevel) level).getLevel().dimension().location();

        if (entityTypeLocation == null)
        {
            callbackInfoReturnable.setReturnValue(null);
            return;
        }

        Rules.parseRules(Rule.Type.ENTITY, registryAccess);

        Rule rule = Rules.entityRules.get(dimensionLocation);
        if (rule != null && rule.isRestricted(entityTypeLocation))
        {
            callbackInfoReturnable.setReturnValue(null);
        }
    }
}
