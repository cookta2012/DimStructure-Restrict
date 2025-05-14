package com.mattmckenzy.dimensionalcontrol.helpers;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.mixin.accessors.FeaturePoolElementAccessor;
import com.mattmckenzy.dimensionalcontrol.mixin.accessors.ListPoolElementAccessor;
import com.mattmckenzy.dimensionalcontrol.mixin.accessors.SinglePoolElementAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.darkhax.bookshelf.common.api.loot.LootPoolEntryDescriptions;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import static com.mattmckenzy.dimensionalcontrol.DimensionalControl.CONFIG_PATH;

public class RegistryHelpers
{
    public static List<ResourceLocation> getResourceLocations(StructurePoolElement structurePoolElement, Registry<PlacedFeature> placedFeatureRegistry)
    {
        List<ResourceLocation> returnResourceLocations = new ArrayList<>();

        if (Objects.equals(structurePoolElement.toString(), "Empty")) {
            return returnResourceLocations;
        }
        switch (structurePoolElement)
        {
            case LegacySinglePoolElement legacySinglePoolElement ->
                    returnResourceLocations.add(((SinglePoolElementAccessor) legacySinglePoolElement).getTemplate().orThrow());
            case SinglePoolElement singlePoolElement ->
                    returnResourceLocations.add(((SinglePoolElementAccessor) singlePoolElement).getTemplate().orThrow());
            case FeaturePoolElement featurePoolElement ->
                returnResourceLocations.add(placedFeatureRegistry.getKey(((FeaturePoolElementAccessor) featurePoolElement).feature().value()));
            case ListPoolElement listPoolElement ->
                    returnResourceLocations.addAll(((ListPoolElementAccessor) listPoolElement).getElements().stream()
                            .flatMap(element -> getResourceLocations(element, placedFeatureRegistry).stream())
                            .toList());
            default -> GroupingLogger.logWarning(String.format("Found unhandled subclass of StructurePoolElement: %s",structurePoolElement));
        }

        return returnResourceLocations;
    }

    public static List<ResourceLocation> getResourceLocations(Rule.Type ruleType, RegistryAccess registryAccess)
    {
        return switch (ruleType)
        {
            case DIMENSION ->
                    registryAccess.registryOrThrow(Registries.DIMENSION).keySet().stream().sorted().toList();
            case STRUCTURE ->
                    registryAccess.registryOrThrow(Registries.STRUCTURE).keySet().stream().sorted().toList();
            case Rule.Type.STRUCTURE_POOL_ELEMENT ->
            {
                Registry<StructureTemplatePool> structureTemplatePoolRegistry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
                Registry<PlacedFeature> placedFeatureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
                TreeSet<ResourceLocation> structurePoolElements = new TreeSet<>();
                RandomSource randomSource = RandomSource.create();
                for (StructureTemplatePool structureTemplatePool : structureTemplatePoolRegistry)
                {
                    List<StructurePoolElement> shuffledTemplates = structureTemplatePool.getShuffledTemplates(randomSource);
                    for (ResourceLocation structurePoolElementLocation :
                            shuffledTemplates.stream().flatMap(structurePoolElement ->
                                    getResourceLocations(structurePoolElement, placedFeatureRegistry).stream()).toList())
                    {
                        if (structurePoolElementLocation != null)
                        {
                            structurePoolElements.add(structurePoolElementLocation);
                        }
                    }
                }
                yield structurePoolElements.stream().sorted().toList();
            }
            case FEATURE ->
                    registryAccess.registryOrThrow(Registries.FEATURE).keySet().stream().sorted().toList();
            case ENTITY ->
                    registryAccess.registryOrThrow(Registries.ENTITY_TYPE).keySet().stream().sorted().toList();
            case LOOT ->
            {
                Registry<Item> itemRegistry = registryAccess.registryOrThrow(Registries.ITEM);
                Registry<LootTable> lootTableRegistry = registryAccess.registryOrThrow(Registries.LOOT_TABLE);
                TreeSet<ResourceLocation> lootPoolItems = new TreeSet<>();
                for (LootTable lootTableEntry : lootTableRegistry)
                {
                    for (ItemStack itemStack : LootPoolEntryDescriptions.getUniqueItems(registryAccess, lootTableEntry))
                    {
                        ResourceLocation itemLocation = itemRegistry.getKey(itemStack.getItem());
                        assert itemLocation != null;
                        lootPoolItems.add(itemLocation);
                    }
                }
                yield lootPoolItems.stream().sorted().toList();
            }
        };
    }

    public static void saveRegistries(MinecraftServer minecraftServer)
    {
        Path registriesFilePath = CONFIG_PATH.resolve(String.format("%s.json", "registries"));

        JsonObject root = new JsonObject();

        root.add("dimensions", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.DIMENSION, minecraftServer.registryAccess())
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("structures", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.STRUCTURE, minecraftServer.registryAccess())
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("structurePoolElements", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.STRUCTURE_POOL_ELEMENT, minecraftServer.registryAccess())
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("features", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.FEATURE, minecraftServer.registryAccess())
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("entities", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.ENTITY, minecraftServer.registryAccess())
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("loot", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.LOOT, minecraftServer.reloadableRegistries().get())
                .stream().map(ResourceLocation::toString).sorted().toList())));

        try
        {
            Files.writeString(registriesFilePath, new GsonBuilder().setPrettyPrinting().create().toJson(root));
            GroupingLogger.logInfo(String.format("Registries saved to %s", registriesFilePath));
        } catch (Exception exception)
        {
            throw new RuntimeException(String.format("Failed to save \"%s\": %s", registriesFilePath, exception.getMessage()), exception);
        }
    }
}
