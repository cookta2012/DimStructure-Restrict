package org.qolmodding.dimensionalcontrolneo.generators;

import org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo;
import org.qolmodding.dimensionalcontrolneo.implementation.GlobalLootModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class GlobalLootModifiersGenerator extends GlobalLootModifierProvider
{
    public GlobalLootModifiersGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, DimensionalControlNeo.MOD_ID);
    }

    @Override
    protected void start()
    {
        this.add(GlobalLootModifier.Name, new GlobalLootModifier(new LootItemCondition[]{}));
    }
}