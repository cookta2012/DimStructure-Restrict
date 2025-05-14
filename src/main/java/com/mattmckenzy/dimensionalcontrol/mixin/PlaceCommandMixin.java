package com.mattmckenzy.dimensionalcontrol.mixin;

import com.mattmckenzy.dimensionalcontrol.helpers.ThreadLocalContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.PlaceCommand;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.levelgen.structure.Structure;

@Mixin(PlaceCommand.class)
public abstract class PlaceCommandMixin
{
    @Inject(method = "placeStructure", at = @At("HEAD"))
    private static void onPlaceStructureHead(CommandSourceStack source, Holder.Reference<Structure> p_structure, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        ThreadLocalContext.setServerLevel(source.getLevel());
    }

    @Inject(method = "placeStructure", at = @At("RETURN"))
    private static void onPlaceStructureReturn(CommandSourceStack source, Holder.Reference<Structure> p_structure, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        ThreadLocalContext.clear();
    }

    @Inject(method = "placeJigsaw", at = @At("HEAD"))
    private static void onPlaceStructureHead(CommandSourceStack source, Holder<StructureTemplatePool> templatePool, ResourceLocation target, int maxDepth, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        ThreadLocalContext.setServerLevel(source.getLevel());
    }

    @Inject(method = "placeJigsaw", at = @At("RETURN"))
    private static void onPlaceStructureReturn(CommandSourceStack source, Holder<StructureTemplatePool> templatePool, ResourceLocation target, int maxDepth, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfoReturnable)
    {
        ThreadLocalContext.clear();
    }
}





