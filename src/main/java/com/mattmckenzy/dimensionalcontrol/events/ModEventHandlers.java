package com.mattmckenzy.dimensionalcontrol.events;

import com.mattmckenzy.dimensionalcontrol.DimensionalControl;
import com.mattmckenzy.dimensionalcontrol.generators.GlobalLootModifiersGenerator;
import com.mattmckenzy.dimensionalcontrol.implementation.Config;
import com.mattmckenzy.dimensionalcontrol.implementation.GlobalLootModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = DimensionalControl.MOD_ID)
public class ModEventHandlers
{
    @SubscribeEvent
    private static void onLoad(final ModConfigEvent event)
    {
        Config.initialize();
    }

    @SubscribeEvent
    public static void onRegisterSerializers(RegisterEvent event) {
        event.register(
                NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                helper -> helper.register(
                        ResourceLocation.fromNamespaceAndPath(DimensionalControl.MOD_ID, GlobalLootModifier.Name),
                        GlobalLootModifier.CODEC
                )
        );
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        
        generator.addProvider(
                event.includeServer(),
                new GlobalLootModifiersGenerator(output, lookupProvider)
        );
    }
}
