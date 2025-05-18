package org.qolmodding.dimensionalcontrolneo.helpers;

import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.apache.commons.lang3.NotImplementedException;
import org.qolmodding.dimensionalcontrolneo.implementation.Rule;
import org.qolmodding.dimensionalcontrolneo.mixin.accessors.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import static org.qolmodding.dimensionalcontrolneo.DimensionalControlNeo.CONFIG_PATH;

public class RegistryHelpers {
    public static List<ResourceLocation> getResourceLocations(StructurePoolElement structurePoolElement, Registry<PlacedFeature> placedFeatureRegistry) {
        List<ResourceLocation> returnResourceLocations = new ArrayList<>();

        if (Objects.equals(structurePoolElement.toString(), "Empty")) {
            return returnResourceLocations;
        }
        if (structurePoolElement instanceof LegacySinglePoolElement legacySinglePoolElement) {
            returnResourceLocations.add(((SinglePoolElementAccessor) legacySinglePoolElement).getTemplate().orThrow());
        } else if (structurePoolElement instanceof SinglePoolElement singlePoolElement) {
            returnResourceLocations.add(((SinglePoolElementAccessor) singlePoolElement).getTemplate().orThrow());
        } else if (structurePoolElement instanceof FeaturePoolElement featurePoolElement) {
            returnResourceLocations.add(placedFeatureRegistry.getKey(((FeaturePoolElementAccessor) featurePoolElement).feature().value()));
        } else if (structurePoolElement instanceof ListPoolElement listPoolElement) {
            returnResourceLocations.addAll(((ListPoolElementAccessor) listPoolElement).getElements().stream()
                    .flatMap(element -> getResourceLocations(element, placedFeatureRegistry).stream())
                    .toList());
        } else {
            GroupingLogger.logWarning(String.format("Found unhandled subclass of StructurePoolElement: %s", structurePoolElement));
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
            case STRUCTURE_POOL_ELEMENT ->
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
                throw new NotImplementedException("Somehow we made it to loot in side the function where you cannot get access to server");
            }
        };
    }

    public static List<ResourceLocation> getResourceLocations(Rule.Type ruleType, MinecraftServer minecraftServer)
    {
        RegistryAccess registryAccess = minecraftServer.registryAccess();
        return switch (ruleType)
        {
            case DIMENSION, STRUCTURE, STRUCTURE_POOL_ELEMENT, FEATURE, ENTITY ->
                    getResourceLocations(ruleType, registryAccess);
            case LOOT ->
            {
                Registry<Item> itemReg = registryAccess.registryOrThrow(Registries.ITEM);
                LootDataManager lootReg = minecraftServer.getLootData();
                TreeSet<ResourceLocation> lootItems = new TreeSet<>();

                // For every loot‐table in the registry
                for (ResourceLocation tableId : lootReg.getKeys(LootDataType.TABLE)) {
                    LootTable table = lootReg.getElement(LootDataType.TABLE,tableId);
                    if (table == null) continue;

                    // Walk each pool in that table
                    for (LootPool pool : ((LootTableAccessor)table).getPools()) {
                        // Walk each entry in the pool
                        for (LootPoolEntryContainer entry : ((LootPoolAccessor)pool).getEntries()) {
                            // If it’s a simple item‐entry, grab its Item
                            if (entry instanceof LootItem lootItemEntry) {
                                Item item = ((LootItemAccessor) lootItemEntry).getItem();            // MC 1.20.1 exposes getItem()
                                ResourceLocation id = itemReg.getKey(item);     // get the ResourceLocation
                                if (id != null) lootItems.add(id);
                            }
                            // (you can likewise handle nested references or tags if you need more coverage)
                        }
                    }
                }
                // Return a sorted list of every item seen in any loot table
                yield  lootItems.stream().toList();
            }
        };
    }




    public static void saveRegistries(MinecraftServer minecraftServer)
    {
        Path registriesFilePath = CONFIG_PATH.resolve(String.format("%s.json", "registries"));

        JsonObject root = new JsonObject();

        root.add("dimensions", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.DIMENSION, minecraftServer)
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("structures", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.STRUCTURE, minecraftServer)
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("structurePoolElements", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.STRUCTURE_POOL_ELEMENT, minecraftServer)
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("features", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.FEATURE, minecraftServer)
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("entities", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.ENTITY, minecraftServer)
                .stream().map(ResourceLocation::toString).sorted().toList())));
        root.add("loot", JsonParser.parseString(new Gson().toJson(getResourceLocations(Rule.Type.LOOT, minecraftServer)
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
