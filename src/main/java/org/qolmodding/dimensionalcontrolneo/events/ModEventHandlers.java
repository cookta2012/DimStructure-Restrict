package org.qolmodding.dimensionalcontrolneo.events;

import org.qolmodding.dimensionalcontrolneo.generators.GlobalLootModifiersGenerator;
import org.qolmodding.dimensionalcontrolneo.implementation.Config;
import org.qolmodding.dimensionalcontrolneo.implementation.GlobalLootModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

import static org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo.MOD_ID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = MOD_ID)
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
                ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                helper -> helper.register(
                        ResourceLocation.tryBuild(MOD_ID, GlobalLootModifier.Name),
                        GlobalLootModifier.CODEC.codec()
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
