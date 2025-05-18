package org.qolmodding.dimensionalcontrolneo;

import org.qolmodding.dimensionalcontrolneo.implementation.Config;
import org.qolmodding.dimensionalcontrolneo.implementation.Rules;
import org.qolmodding.dimensionalcontrolneo.helpers.GroupingLogger;
import org.qolmodding.dimensionalcontrolneo.integrations.CobblemonSpawnHandler;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.*;

@Mod(DimensionalControlNeo.MOD_ID)
public class DimensionalControlNeo
{
    public static final String MOD_ID = "dimensionalcontrolneo";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);

    public DimensionalControlNeo() throws IOException
    {
        GroupingLogger.logInfo("Attempting to load config file...");

        Files.createDirectories(CONFIG_PATH);
        Path configFilePath = CONFIG_PATH.resolve(String.format("%s-common.toml", "config"));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, configFilePath.toString());

        GroupingLogger.logInfo("Attempting to load json config file...");
        Rules.loadJsonConfig();

        if (ModList.get().isLoaded("cobblemon"))
        {
            CobblemonSpawnHandler.subscribeToCobblemonSpawnEvents();
        }
    }
}