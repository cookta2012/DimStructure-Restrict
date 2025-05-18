package org.qolmodding.dimensionalcontrolneo.mixin.accessors;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// in src/main/java
@Mixin(LootItem.class)
public interface LootItemAccessor {
    @Accessor("item")
    Item getItem();
}

