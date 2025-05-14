package com.mattmckenzy.dimensionalcontrol.implementation;

import com.mattmckenzy.dimensionalcontrol.helpers.GroupingLogger;
import com.mattmckenzy.dimensionalcontrol.helpers.ResourceLocationHelpers;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.mattmckenzy.dimensionalcontrol.DimensionalControl.CONFIG_PATH;

public class Rules
{
    private static final Path definitionsFilePath = CONFIG_PATH.resolve(String.format("%s.json", "definitions"));
    private static final Path expandedDefinitionsFilePath = CONFIG_PATH.resolve(String.format("%s.json", "expanded-definitions"));

    public static final Map<ResourceLocation, Rule> structureRules = new HashMap<>();
    public static final Map<ResourceLocation, Rule> structurePoolElementRules = new HashMap<>();
    public static final Map<ResourceLocation, Rule> featureRules = new HashMap<>();
    public static final Map<ResourceLocation, Rule> entityRules = new HashMap<>();
    public static final Map<ResourceLocation, Rule> lootRules = new HashMap<>();

    public static JsonArray unparsedStructureRules = null;
    public static JsonArray unparsedStructurePoolElementRules = null;
    public static JsonArray unparsedFeatureRules = null;
    public static JsonArray unparsedEntityRules = null;
    public static JsonArray unparsedLootRules = null;

    public static void parseRules(Rule.Type ruleType, RegistryAccess registryAccess)
    {
        switch (ruleType)
        {
            case DIMENSION -> throw new NotImplementedException();
            case STRUCTURE ->
            {
                if (unparsedStructureRules != null)
                {
                    parseRuleArray(registryAccess, Rule.Type.STRUCTURE);
                    unparsedStructureRules = null;
                }
            }
            case STRUCTURE_POOL_ELEMENT ->
            {
                if (unparsedStructurePoolElementRules != null)
                {
                    parseRuleArray(registryAccess, Rule.Type.STRUCTURE_POOL_ELEMENT);
                    unparsedStructurePoolElementRules = null;
                }
            }
            case FEATURE ->
            {
                if (unparsedFeatureRules != null)
                {
                    parseRuleArray(registryAccess, Rule.Type.FEATURE);
                    unparsedFeatureRules = null;
                }
            }
            case ENTITY ->
            {
                if (unparsedEntityRules != null)
                {
                    parseRuleArray(registryAccess, Rule.Type.ENTITY);
                    unparsedEntityRules = null;
                }
            }
            case LOOT ->
            {
                if (unparsedLootRules != null)
                {
                    parseRuleArray(registryAccess, Rule.Type.LOOT);
                    unparsedEntityRules = null;
                }
            }
        }
    }

    public static void loadJsonConfig()
    {
        try
        {
            if (Files.notExists(definitionsFilePath))
            {
                Files.writeString(definitionsFilePath, """
                        {
                            "structures": [
                                {
                                    "dimension": "minecraft:overworld",
                                    "whitelist": [],
                                    "active": true
                                }
                            ],
                            "structurePoolElements": [
                                {
                                    "dimension": "minecraft:overworld",
                                    "whitelist": [],
                                    "active": true
                                }
                            ],
                            "features": [
                                {
                                    "dimension": "minecraft:overworld",
                                    "blacklist": [],
                                    "active": true
                                }
                            ],
                            "entities": [
                                {
                                    "dimension": "minecraft:.+",
                                    "whitelist": [],
                                    "active": true
                                }
                            ],
                            "loot": [
                                {
                                    "dimension": "minecraft:.+",
                                    "blacklist": [],
                                    "active": true
                                }
                            ]
                        }
                        """);
            }

            try (BufferedReader reader = Files.newBufferedReader(definitionsFilePath);
                 JsonReader jsonReader = new JsonReader(reader))
            {

                jsonReader.setLenient(true); // Allows comments and more relaxed syntax

                JsonElement root = JsonParser.parseReader(jsonReader);
                if (root.isJsonObject())
                {
                    JsonObject rootJsonObject = root.getAsJsonObject();
                    if (rootJsonObject.has("structures"))
                    {
                        unparsedStructureRules = rootJsonObject.getAsJsonArray("structures");
                    }
                    if (rootJsonObject.has("structurePoolElements"))
                    {
                        unparsedStructurePoolElementRules = rootJsonObject.getAsJsonArray("structurePoolElements");
                    }
                    if (rootJsonObject.has("features"))
                    {
                        unparsedFeatureRules = rootJsonObject.getAsJsonArray("features");
                    }
                    if (rootJsonObject.has("entities"))
                    {
                        unparsedEntityRules = rootJsonObject.getAsJsonArray("entities");
                    }
                    if (rootJsonObject.has("loot"))
                    {
                        unparsedLootRules = rootJsonObject.getAsJsonArray("loot");
                    }
                }
                else
                {
                    throw new IllegalStateException("\"" + definitionsFilePath + "\" must be properly formed JSON!");
                }
            }
        } catch (com.google.gson.JsonSyntaxException syntaxEx)
        {
            throw new RuntimeException("Malformed JSON in file \"" + definitionsFilePath + "\" at line " + syntaxEx.getMessage(), syntaxEx);
        } catch (Exception ex)
        {
            throw new RuntimeException("Failed to load \"" + definitionsFilePath + "\": " + ex.getMessage(), ex);
        }
    }

    public static void saveExpandedJsonConfig()
    {
        JsonObject rootJsonObject = new JsonObject();

        JsonArray structuresArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : structureRules.entrySet())
        {
            JsonObject structureRuleJsonObject = getRuleJson(entry);
            structuresArray.add(structureRuleJsonObject);
        }
        rootJsonObject.add("structures", structuresArray);

        JsonArray structurePoolElementsArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : structurePoolElementRules.entrySet())
        {
            JsonObject structurePoolElementRuleJsonObject = getRuleJson(entry);
            structurePoolElementsArray.add(structurePoolElementRuleJsonObject);
        }
        rootJsonObject.add("structurePoolElements", structurePoolElementsArray);

        JsonArray featuresArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : featureRules.entrySet())
        {
            JsonObject featureRuleJsonObject = getRuleJson(entry);
            featuresArray.add(featureRuleJsonObject);
        }
        rootJsonObject.add("features", featuresArray);

        JsonArray entitiesArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : entityRules.entrySet())
        {
            JsonObject entityRuleJsonObject = getRuleJson(entry);
            entitiesArray.add(entityRuleJsonObject);
        }
        rootJsonObject.add("entities", entitiesArray);

        JsonArray lootArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : lootRules.entrySet())
        {
            JsonObject lootRulesJsonObject = getRuleJson(entry);
            lootArray.add(lootRulesJsonObject);
        }
        rootJsonObject.add("loot", lootArray);

        try
        {
            Files.writeString(expandedDefinitionsFilePath, new GsonBuilder().setPrettyPrinting().create().toJson(rootJsonObject));
            GroupingLogger.logInfo(String.format("Config saved to %s", expandedDefinitionsFilePath));
        } catch (Exception exception)
        {
            throw new RuntimeException(String.format("Failed to save \"%s\": %s", expandedDefinitionsFilePath, exception.getMessage()), exception);
        }
    }

    private static @NotNull JsonObject getRuleJson(Map.Entry<ResourceLocation, Rule> entry)
    {
        JsonObject ruleJsonObject = new JsonObject();
        ruleJsonObject.addProperty("dimension", entry.getKey().toString());

        Rule rule = entry.getValue();
        JsonArray list = new JsonArray();
        for (ResourceLocation resource : rule.getResourceLocations())
        {
            list.add(resource.toString());
        }

        if (rule.getMode() == Rule.Mode.WHITELIST)
        {
            ruleJsonObject.add("whitelist", list);
        }
        else
        {
            ruleJsonObject.add("blacklist", list);
        }

        ruleJsonObject.addProperty("active", rule.getActive() != null ? rule.getActive() : false);
        return ruleJsonObject;
    }

    private static void parseRuleArray(RegistryAccess registryAccess, Rule.Type ruleType)
    {
        JsonArray rulesJsonArray = switch (ruleType)
        {
            case Rule.Type.DIMENSION -> throw new NotImplementedException();
            case Rule.Type.STRUCTURE -> unparsedStructureRules;
            case Rule.Type.STRUCTURE_POOL_ELEMENT -> unparsedStructurePoolElementRules;
            case Rule.Type.FEATURE -> unparsedFeatureRules;
            case Rule.Type.ENTITY -> unparsedEntityRules;
            case Rule.Type.LOOT -> unparsedLootRules;
        };
        Map<ResourceLocation, Rule> dimensionRuleMap = switch (ruleType)
        {
            case Rule.Type.DIMENSION -> throw new NotImplementedException();
            case Rule.Type.STRUCTURE -> structureRules;
            case Rule.Type.STRUCTURE_POOL_ELEMENT -> structurePoolElementRules;
            case Rule.Type.FEATURE -> featureRules;
            case Rule.Type.ENTITY -> entityRules;
            case Rule.Type.LOOT -> lootRules;
        };

        for (JsonElement ruleJsonELement : rulesJsonArray)
        {
            JsonObject ruleJsonObject = ruleJsonELement.getAsJsonObject();
            String dimensionLocationString = ruleJsonObject.get("dimension").getAsString();

            boolean hasWhitelist = ruleJsonObject.has("whitelist");
            boolean hasBlacklist = ruleJsonObject.has("blacklist");
            if (hasWhitelist && hasBlacklist)
            {
                throw new IllegalStateException(String.format("%s rule for '%s' has both whitelist and blacklist.", ruleType, dimensionLocationString));
            }
            if (!hasWhitelist && !hasBlacklist)
            {
                continue;
            }

            Rule.Mode mode = hasWhitelist ? Rule.Mode.WHITELIST : Rule.Mode.BLACKLIST;
            JsonArray resourceLocationsJsonArray = ruleJsonObject.getAsJsonArray(hasWhitelist ? "whitelist" : "blacklist");
            Set<String> resourceLocationStrings = new HashSet<>();
            for (JsonElement resourceLocationJsonElement : resourceLocationsJsonArray)
            {
                resourceLocationStrings.add(resourceLocationJsonElement.getAsString());
            }

            Boolean active = ruleJsonObject.has("active") && ruleJsonObject.get("active").getAsBoolean();

            Set<ResourceLocation> resourceLocations = new HashSet<>();
            for (String resourceLocationString : resourceLocationStrings)
            {
                resourceLocations.addAll(ResourceLocationHelpers.expandLocation(ruleType, registryAccess, resourceLocationString));
            }

            for (ResourceLocation dimensionLocation : ResourceLocationHelpers.expandLocation(Rule.Type.DIMENSION, registryAccess, dimensionLocationString))
            {
                dimensionRuleMap.put(dimensionLocation, new Rule(dimensionLocation, ruleType, mode, resourceLocations, active));
            }
        }
    }
}
