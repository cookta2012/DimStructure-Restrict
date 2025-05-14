package com.mattmckenzy.dimensionalcontrol.implementation;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    public static boolean logAllowedStructure;
    public static boolean logPreventedStructure;
    public static boolean logAllowedStructurePoolElement;
    public static boolean logPreventedStructurePoolElement;
    public static boolean logAllowedFeature;
    public static boolean logPreventedFeature;
    public static boolean logAllowedEntity;
    public static boolean logPreventedEntity;
    public static boolean logAllowedLoot;
    public static boolean logPreventedLoot;
    public static boolean debug;

    public static void initialize()
    {
        logAllowedStructure = LOG_ALLOWED_STRUCTURE.get();
        logPreventedStructure = LOG_PREVENTED_STRUCTURE.get();
        logAllowedStructurePoolElement = LOG_ALLOWED_STRUCTURE_POOL_ELEMENT.get();
        logPreventedStructurePoolElement = LOG_PREVENTED_STRUCTURE_POOL_ELEMENT.get();
        logAllowedFeature = LOG_ALLOWED_FEATURE.get();
        logPreventedFeature = LOG_PREVENTED_FEATURE.get();
        logAllowedEntity = LOG_ALLOWED_ENTITY.get();
        logPreventedEntity = LOG_PREVENTED_ENTITY.get();
        logAllowedLoot = LOG_ALLOWED_LOOT.get();
        logPreventedLoot = LOG_PREVENTED_LOOT.get();
        debug = DEBUG.get();
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LOG_ALLOWED_STRUCTURE = BUILDER
            .comment("Enable logging of allowed structures. This is off by default.")
            .define("log-allowed-structures", false);

    private static final ModConfigSpec.BooleanValue LOG_PREVENTED_STRUCTURE = BUILDER
            .comment("Enable logging of prevented structures. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("log-prevented-structures", true);

    private static final ModConfigSpec.BooleanValue LOG_ALLOWED_STRUCTURE_POOL_ELEMENT = BUILDER
            .comment("Enable logging of allowed structure pool elements. This is off by default.")
            .define("log-allowed-structure-pool-elements", false);

    private static final ModConfigSpec.BooleanValue LOG_PREVENTED_STRUCTURE_POOL_ELEMENT = BUILDER
            .comment("Enable logging of prevented structure pool elements. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("log-prevented-structure-pool-elements", true);

    private static final ModConfigSpec.BooleanValue LOG_ALLOWED_FEATURE = BUILDER
            .comment("Enable logging of allowed environmental features. This is off by default.")
            .define("log-allowed-features", false);

    private static final ModConfigSpec.BooleanValue LOG_PREVENTED_FEATURE = BUILDER
            .comment("Enable logging of prevented environmental features. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("log-prevented-features", true);

    private static final ModConfigSpec.BooleanValue LOG_ALLOWED_ENTITY = BUILDER
            .comment("Enable logging of allowed entity spawns. This is off by default.")
            .define("log-allowed-entities", false);

    private static final ModConfigSpec.BooleanValue LOG_PREVENTED_ENTITY = BUILDER
            .comment("Enable logging of prevented entity spawns. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("log-prevented-entities", true);

    private static final ModConfigSpec.BooleanValue LOG_ALLOWED_LOOT = BUILDER
            .comment("Enable logging of allowed loot items. This is off by default.")
            .define("log-allowed-loot", false);

    private static final ModConfigSpec.BooleanValue LOG_PREVENTED_LOOT = BUILDER
            .comment("Enable logging of prevented loot items. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("log-prevented-loot", true);

    private static final ModConfigSpec.BooleanValue DEBUG = BUILDER
            .comment("Enable debug logging. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
            .define("debug", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
