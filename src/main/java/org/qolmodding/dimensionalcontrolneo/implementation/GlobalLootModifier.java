package org.qolmodding.dimensionalcontrolneo.implementation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import static org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo.MOD_ID;

public class GlobalLootModifier extends LootModifier
{
    public static final String Name = "global_loot_modifier";

    public GlobalLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static final MapCodec<GlobalLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            codecInstance ->
                LootModifier.codecStart(codecInstance).apply(codecInstance, GlobalLootModifier::new)
    );

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    @SuppressWarnings("unused")
    public static final RegistryObject<Codec<GlobalLootModifier>> MY_LOOT_MODIFIER =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(Name, GlobalLootModifier.CODEC::codec);

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
    {
        ServerLevel serverLevel = context.getLevel();
        ResourceLocation dimensionLocation = serverLevel.dimension().location();

        RegistryAccess registryAccess = serverLevel.registryAccess();
        Registry<Item> itemRegistryAccess = registryAccess.registryOrThrow(Registries.ITEM);

        Rules.parseRules(Rule.Type.LOOT, serverLevel.getServer().registryAccess());

        ObjectArrayList<ItemStack> modifiedLoot = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot)
        {
            ResourceLocation lootItemLocation = itemRegistryAccess.getKey(stack.getItem());

            Rule rule = Rules.lootRules.get(dimensionLocation);
            if (rule == null || !rule.isRestricted(lootItemLocation))
            {
                modifiedLoot.add(stack);
            }
        }

        return modifiedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec()
    {
        return CODEC.codec();
    }
}