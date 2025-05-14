package com.mattmckenzy.dimensionalcontrol.integrations;

import com.mattmckenzy.dimensionalcontrol.helpers.GroupingLogger;
import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import com.mattmckenzy.dimensionalcontrol.implementation.Rules;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

public class CobblemonSpawnHandler
{
    public static void subscribeToCobblemonSpawnEvents()
    {
        try
        {
            Class<?> cobblemonEventsClass = Class.forName("com.cobblemon.mod.common.api.events.CobblemonEvents");
            Object cobblemonEntitySpawnEvent = cobblemonEventsClass.getField("ENTITY_SPAWN").get(cobblemonEventsClass);
            Optional<@NotNull Method> subscribeMethod = Arrays.stream(cobblemonEntitySpawnEvent.getClass().getMethods())
                    .filter((Method method) -> method.getName().equals("subscribe")).findFirst();

            if (subscribeMethod.isPresent())
            {
                Object priority = Class.forName("com.cobblemon.mod.common.api.Priority").getField("HIGHEST").get(null);

                Class<?> function1Class = Class.forName("kotlin.jvm.functions.Function1");
                Object functionHandler = Proxy.newProxyInstance(
                        CobblemonSpawnHandler.class.getClassLoader(),
                        new Class[]{function1Class},
                        (proxy, method, args) ->
                        {
                            Object spawnContext = args[0].getClass().getMethod("getCtx").invoke(args[0]);
                            ServerLevel serverLevel = (ServerLevel)spawnContext.getClass().getMethod("getWorld").invoke(spawnContext);
                            ResourceLocation dimensionLocation = serverLevel.dimension().location();

                            Object entity = args[0].getClass().getMethod("getEntity").invoke(args[0]);
                            EntityType<?> entityType = (EntityType<?>)entity.getClass().getMethod("getType").invoke(entity);
                            RegistryAccess registryAccess = serverLevel.registryAccess();
                            ResourceLocation entityTypeLocation = registryAccess.registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);

                            if (entityTypeLocation == null)
                            {
                                args[0].getClass().getMethod("cancel").invoke(args[0]);
                            }
                            else
                            {
                                Rule rule = Rules.entityRules.get(dimensionLocation);
                                if (rule != null && rule.isRestricted(entityTypeLocation))
                                {
                                    args[0].getClass().getMethod("cancel").invoke(args[0]);
                                }
                            }

                            return Class.forName("kotlin.Unit").getField("INSTANCE").get(null);
                        }
                );

                subscribeMethod.get().invoke(cobblemonEntitySpawnEvent, priority, functionHandler);
            }
        }
        catch (Exception exception)
        {
            GroupingLogger.logDebug(String.format("Could not subscriber to Cobblemon spawn event: \"%s\"", exception));
        }
    }
}
