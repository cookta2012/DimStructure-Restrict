package com.mattmckenzy.dimensionalcontrol.helpers;

import net.minecraft.server.level.ServerLevel;

public class ThreadLocalContext {
    private static final ThreadLocal<ServerLevel> SERVER_LEVEL = new ThreadLocal<>();

    public static void setServerLevel(ServerLevel level) {
        SERVER_LEVEL.set(level);
    }

    public static ServerLevel getServerLevel() {
        return SERVER_LEVEL.get();
    }

    public static void clear() {
        SERVER_LEVEL.remove();
    }
}
