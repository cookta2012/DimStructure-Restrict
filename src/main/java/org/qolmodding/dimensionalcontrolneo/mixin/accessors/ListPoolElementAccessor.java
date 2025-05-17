package org.qolmodding.dimensionalcontrolneo.mixin.accessors;

import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ListPoolElement.class)
public interface ListPoolElementAccessor
{
    @Accessor("elements")
    List<StructurePoolElement> getElements();
}
