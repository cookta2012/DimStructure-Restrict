package org.qolmodding.dimensionalcontrolneo.mixin.accessors;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.BiFunction;
import java.util.function.Predicate;

@Mixin(LootPool.class)
public interface LootPoolAccessor {
    @Accessor("entries")
    LootPoolEntryContainer[] getEntries();

    @Accessor("conditions")
    LootItemCondition[] getConditions();

    @Accessor("compositeCondition")
    Predicate<LootContext> getCompositeCondition();

    @Accessor("functions")
    LootItemFunction[] getFunctions();

    @Accessor("compositeFunction")
    BiFunction<ItemStack, LootContext, ItemStack> getCompositeFunction();

    @Accessor("rolls")
    NumberProvider getRolls();

    @Accessor("bonusRolls")
    NumberProvider getBonusRolls();

}
