package com.cookta2012.dimstructrestrict;

import com.google.gson.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("dimstructrestrict")
public class DimensionalStructureRestrict {
    public static final Logger LOGGER = LoggerFactory.getLogger("dimstructrestrict");


    public enum Mode { WHITELIST, BLACKLIST }

    public record StructureRule(Mode mode, Set<ResourceKey<Level>> dims) {}
    public record DimensionRule(Mode mode, Set<ResourceLocation> structures) {}

    public static final Map<ResourceLocation, StructureRule> STRUCT_RULES = new HashMap<>();
    public static final Map<ResourceKey<Level>, DimensionRule> DIM_RULES   = new HashMap<>();

    public DimensionalStructureRestrict() { load(); }

    private void load() {
        Path cfg = FMLPaths.CONFIGDIR.get().resolve("DimStruct Restrict.json");
        try {
            if (Files.notExists(cfg)) {
                Files.writeString(cfg, """
{
  "structures": [
    /*
    {
      "id": "minecraft:village_plains",
      "whitelist": ["minecraft:overworld"]
    }
    */
  ],
  "dimensions": [
    /*
    {
      "id": "minecraft:the_end",
      "blacklist": ["minecraft:village_plains"]
    }
    */
  ]
}
""");
            }

            JsonElement root = JsonParser.parseReader(Files.newBufferedReader(cfg));
            if (root.isJsonArray()) {
                // backwards‑compat: treat as structures list only
                parseStructures(root.getAsJsonArray());
            } else if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("structures"))
                    parseStructures(obj.getAsJsonArray("structures"));
                if (obj.has("dimensions"))
                    parseDimensions(obj.getAsJsonArray("dimensions"));
            } else {
                throw new IllegalStateException("dim_struct_filter.json must be proper JSON object or array");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load dim_struct_filter.json: " + ex.getMessage(), ex);
        }
    }
    
    

    private void parseStructures(JsonArray arr) {
        for (JsonElement elem : arr) {
            JsonObject o = elem.getAsJsonObject();
            String idStr = o.get("id").getAsString();
            ResourceLocation id = new ResourceLocation(idStr);

            boolean hasWhitelist = o.has("whitelist");
            boolean hasBlacklist = o.has("blacklist");
            if (hasWhitelist && hasBlacklist) {
                throw new IllegalStateException("Structure rule '" + idStr + "' has both whitelist and blacklist.");
            }
            if (!hasWhitelist && !hasBlacklist) {
                continue; // unrestricted
            }
            Mode mode = hasWhitelist ? Mode.WHITELIST : Mode.BLACKLIST;
            JsonArray list = o.getAsJsonArray(hasWhitelist ? "whitelist" : "blacklist");
            Set<ResourceKey<Level>> dims = new HashSet<>();
            for (JsonElement j : list) {
                dims.add(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(j.getAsString())));
            }
            STRUCT_RULES.put(id, new StructureRule(mode, dims));
        }
    }

    private void parseDimensions(JsonArray arr) {
        for (JsonElement elem : arr) {
            JsonObject o = elem.getAsJsonObject();
            String idStr = o.get("id").getAsString();
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(idStr));

            boolean hasWhitelist = o.has("whitelist");
            boolean hasBlacklist = o.has("blacklist");
            if (hasWhitelist && hasBlacklist) {
                throw new IllegalStateException("Dimension rule '" + idStr + "' has both whitelist and blacklist.");
            }
            if (!hasWhitelist && !hasBlacklist) {
                continue; // unrestricted
            }
            Mode mode = hasWhitelist ? Mode.WHITELIST : Mode.BLACKLIST;
            JsonArray list = o.getAsJsonArray(hasWhitelist ? "whitelist" : "blacklist");
            Set<ResourceLocation> structs = new HashSet<>();
            for (JsonElement j : list) {
                structs.add(new ResourceLocation(j.getAsString()));
            }
            DIM_RULES.put(dimKey, new DimensionRule(mode, structs));
        }
    }
}
