package org.qolmodding.dimensionalcontrolneo.events;

import net.minecraftforge.fml.common.Mod;
import org.qolmodding.dimensionalcontrolneo.helpers.RegistryHelpers;
import org.qolmodding.dimensionalcontrolneo.implementation.Rule;
import org.qolmodding.dimensionalcontrolneo.implementation.Rules;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

@Mod.EventBusSubscriber
public class GameEventHandlers
{
    @SubscribeEvent
    public static void onServerStarting(ServerStartedEvent event)
    {
        RegistryHelpers.saveRegistries(event.getServer());
        Rules.parseRules(Rule.Type.STRUCTURE, event.getServer().registryAccess());
        Rules.parseRules(Rule.Type.STRUCTURE_POOL_ELEMENT, event.getServer().registryAccess());
        Rules.parseRules(Rule.Type.FEATURE, event.getServer().registryAccess());
        Rules.parseRules(Rule.Type.ENTITY, event.getServer().registryAccess());
        Rules.parseRules(Rule.Type.LOOT, event.getServer().registryAccess());
        Rules.saveExpandedJsonConfig();
    }
}
