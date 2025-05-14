package com.mattmckenzy.dimensionalcontrol.events;

import com.mattmckenzy.dimensionalcontrol.helpers.RegistryHelpers;
import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber
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
        Rules.parseRules(Rule.Type.LOOT, event.getServer().reloadableRegistries().get());
        Rules.saveExpandedJsonConfig();
    }
}
