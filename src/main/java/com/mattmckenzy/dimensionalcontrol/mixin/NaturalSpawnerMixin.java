package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin
{
    @Inject(method = "isValidEmptySpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void isValidEmptySpawnBlock(BlockGetter block, BlockPos pos, BlockState blockState, FluidState fluidState, EntityType<?> entityType, CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        ServerLevel level = (block instanceof ServerLevel) ? (ServerLevel) block : null;
        if (level == null) return;

        RegistryAccess registryAccess = level.registryAccess();

        ResourceLocation entityTypeLocation = registryAccess.registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);

        ResourceLocation dimensionLocation = (level instanceof ServerLevel) ? level.dimension().location() :
                (level instanceof ServerLevelAccessor) ? level.getLevel().dimension().location() :
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
}
