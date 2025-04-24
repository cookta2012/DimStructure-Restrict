package com.cookta2012.dimstructrestrict;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class DimensionalStructureRestrictConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair =
            new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue log_prevented_structures;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            log_prevented_structures = builder
                .comment("Enable logging of prevented structures. This is on by default. If you use this on a server and dont want to waste space with constant spam turn it off.")
                .define("log-prevented-structures", true); // default: true

            builder.pop();
        }
    }
}
