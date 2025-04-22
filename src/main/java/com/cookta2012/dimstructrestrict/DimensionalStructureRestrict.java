package com.cookta2012.dimstructrestrict;

import com.cookta2012.dimstructrestrict.Rule.Mode;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.nio.file.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("dimstructrestrict")
public class DimensionalStructureRestrict {
    public static final Logger LOGGER = LoggerFactory.getLogger("dimstructrestrict");

    public static final Map<ResourceLocation, Rule> STRUCTURE_RULES = new HashMap<>();
    public static final Map<ResourceLocation, Rule> DIMENSION_RULES   = new HashMap<>();

    public DimensionalStructureRestrict() { loadConfig(); }
    
    private Boolean dirtyConfig = false;    
    private String configFile  = "DimStruct Restrict.json";
    
    public Boolean isConfigDirty() { return dirtyConfig; }
    public void setConfigDirty() { dirtyConfig = true; }
    private void setConfigClean() { dirtyConfig = false; }

    private void loadConfig() {
        Path cfg = FMLPaths.CONFIGDIR.get().resolve(configFile);
        try {
            if (Files.notExists(cfg)) {
                Files.writeString(cfg, """
{
  "structures": [
    {
      "id": "minecraft:village_plains",
      "whitelist": ["minecraft:overworld"],
      "active": true
    }
  ],
  "dimensions": [
    {
      "id": "minecraft:the_end",
      "blacklist": ["minecraft:village_plains"]
    }
  ]
}
""");
            }

            try (BufferedReader reader = Files.newBufferedReader(cfg);
                    JsonReader jsonReader = new JsonReader(reader)) {

                   jsonReader.setLenient(true); // Allows comments and more relaxed syntax

                   JsonElement root = JsonParser.parseReader(jsonReader);
                   if (root.isJsonArray()) {
                	   parseArr(root.getAsJsonArray(), Rule.Type.STRUCTURE, STRUCTURE_RULES);
                   } else if (root.isJsonObject()) {
                       JsonObject obj = root.getAsJsonObject();
                       if (obj.has("structures")) {
                    	   parseArr(obj.getAsJsonArray("structures"), Rule.Type.STRUCTURE, STRUCTURE_RULES);
                       }
                       if (obj.has("dimensions")) {
                    	   parseArr(obj.getAsJsonArray("dimensions"), Rule.Type.DIMENSION, DIMENSION_RULES);
                       }
                   } else {
                	   throw new IllegalStateException("\"" + configFile + "\" must be proper JSON or Structure array");
                   }
                   if(isConfigDirty()) {
                	   saveConfig();
                	   loadConfig();
                   }
               }
        } catch (com.google.gson.JsonSyntaxException syntaxEx) {
            throw new RuntimeException("Malformed JSON in file \"" + configFile + "\" at line " + syntaxEx.getMessage(), syntaxEx);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load \""+ configFile +"\": " + ex.getMessage(), ex);
        }
    }
    
    private void saveConfig() {
        Path cfg = FMLPaths.CONFIGDIR.get().resolve(configFile);
        JsonObject root = new JsonObject();

        JsonArray structuresArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : STRUCTURE_RULES.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", entry.getKey().toString());

            Rule rule = entry.getValue();
            JsonArray list = new JsonArray();
            for (ResourceLocation dim : rule.resource()) {
                list.add(dim.toString());
            }

            if (rule.mode() == Mode.WHITELIST) {
                o.add("whitelist", list);
            } else {
                o.add("blacklist", list);
            }

            o.addProperty("false_place", rule.false_place() != null ? rule.false_place() : false);
            o.addProperty("active", rule.active() != null ? rule.active() : false);
            structuresArray.add(o);
        }
        root.add("structures", structuresArray);

        JsonArray dimensionsArray = new JsonArray();
        for (Map.Entry<ResourceLocation, Rule> entry : DIMENSION_RULES.entrySet()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", entry.getKey().toString());

            Rule rule = entry.getValue();
            JsonArray list = new JsonArray();
            for (ResourceLocation structId : rule.resource()) {
                list.add(structId.toString());
            }

            if (rule.mode() == Mode.WHITELIST) {
                o.add("whitelist", list);
            } else {
                o.add("blacklist", list);
            }

            o.addProperty("false_place", rule.false_place() != null ? rule.false_place() : false);
            o.addProperty("active", rule.active() != null ? rule.active() : false);
            dimensionsArray.add(o);
        }
        root.add("dimensions", dimensionsArray);

        try {
            Files.writeString(cfg, new GsonBuilder().setPrettyPrinting().create().toJson(root));
            setConfigClean();
            LOGGER.debug("Config saved to " + cfg);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save \"" + configFile + "\": " + ex.getMessage(), ex);
        }
    }


    private Boolean get_param(JsonObject o, String param_name) {
    	return get_param(o, param_name, false);
    }
    
    private Boolean get_param(JsonObject o, String param_name, Boolean _default) {
    	if (!o.has(param_name)) {
    		setConfigDirty();
    		return _default;
    	} else {
    		return o.get(param_name).getAsBoolean();
    	}
    }

    private void parseArr(JsonArray arr, Rule.Type ruletype, Map<ResourceLocation, Rule> resourceMap) {
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
            Set<ResourceLocation> resources = new HashSet<>();
            for (JsonElement j : list) {
                resources.add(new ResourceLocation(j.getAsString()));
            }
            Boolean false_place = get_param( o, "false_place" );
            Boolean active = get_param( o, "active" );
            switch (ruletype) {
	            case DIMENSION -> {
	            	resourceMap.put(id, new DimensionRule(id, mode, resources, false_place, active));
	            	continue;
	            }
	            case STRUCTURE -> {
	            	resourceMap.put(id, new StructureRule(id, mode, resources, false_place, active));
	            	continue;
	            }
	            default -> throw new IllegalStateException("Attempted to create invalid Rule Type");
            }
        }
    }
}
