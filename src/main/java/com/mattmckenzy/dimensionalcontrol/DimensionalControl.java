package com.mattmckenzy.dimensionalcontrol;

import com.mattmckenzy.dimensionalcontrol.implementation.Config;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import com.mattmckenzy.dimensionalcontrol.helpers.GroupingLogger;
import com.mattmckenzy.dimensionalcontrol.integrations.CobblemonSpawnHandler;

import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.nio.file.*;

@Mod("dimensionalcontrol")
public class DimensionalControl
{
    public static final String MOD_ID = "dimensionalcontrol";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);

    public DimensionalControl() throws IOException
    {
        GroupingLogger.logInfo("Attempting to load config file...");

        Files.createDirectories(CONFIG_PATH);
        Path configFilePath = CONFIG_PATH.resolve(String.format("%s-common.toml", "config"));
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.SPEC, configFilePath.toString());

        GroupingLogger.logInfo("Attempting to load json config file...");
        Rules.loadJsonConfig();

        if (ModList.get().isLoaded("cobblemon"))
        {
            CobblemonSpawnHandler.subscribeToCobblemonSpawnEvents();
        }
    }
}
