package com.mattmckenzy.dimensionalcontrol.implementation;

import java.util.Objects;
import java.util.Set;

import com.mattmckenzy.dimensionalcontrol.helpers.GroupingLogger;
import net.minecraft.resources.ResourceLocation;

public class Rule
{
    public enum Mode
    {
        WHITELIST,
        BLACKLIST
    }

    public enum Type
    {
        DIMENSION,
        STRUCTURE,
        STRUCTURE_POOL_ELEMENT,
        FEATURE,
        ENTITY,
        LOOT
    }

    private final ResourceLocation dimensionLocation;
    private final Type type;
    private final Mode mode;
    private final Set<ResourceLocation> resourceLocations;
    private final Boolean active;

    public Set<ResourceLocation> getResourceLocations()
    {
        return resourceLocations;
    }

    public Mode getMode()
    {
        return mode;
    }

    public Boolean getActive()
    {
        return active;
    }

    public Rule(ResourceLocation dimensionLocation, Type type, Mode mode, Set<ResourceLocation> resources, Boolean active)
    {
        this.dimensionLocation = dimensionLocation;
        this.type = type;
        this.mode = mode;
        this.resourceLocations = resources;
        this.active = active;
    }

    public String getType()
    {
        return switch (this.type)
        {
            case DIMENSION -> "Dimension";
            case STRUCTURE -> "Structure";
            case STRUCTURE_POOL_ELEMENT -> "Structure Pool Element";
            case FEATURE -> "Feature";
            case ENTITY -> "Entity";
            case LOOT -> "Loot";
        };
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(mode, rule.mode)
                && Objects.equals(resourceLocations, rule.resourceLocations)
                && Objects.equals(active, rule.active);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mode, resourceLocations, active);
    }

    @Override
    public String toString()
    {
        return
                "Rule {" +
                        "dimension = \"" + dimensionLocation + "\"" +
                        "type = \"" + type + "\"" +
                        "mode = \"" + mode + "\"" +
                        "resources = \"" + resourceLocations + "\"" +
                        "active = \"" + active + "\"" +
                        '}';
    }


    public Boolean isRestricted(ResourceLocation targetResource)
    {
        // Short circuit due to rule inactive or false_place active
        if (!this.active)
        {
            if (Config.debug)
            {
                GroupingLogger.logDebug(String.format("Rule not active: %s", this));
            }
            return false;
        }

        if ((this.mode == Mode.WHITELIST && !this.resourceLocations.contains(targetResource)) ||
                (this.mode == Mode.BLACKLIST && this.resourceLocations.contains(targetResource)))
        {
            logResourceConsequence(targetResource, false);
            return true;
        }
        else
        {
            logResourceConsequence(targetResource, true);
            return false;
        }
    }

    private void logResourceConsequence(ResourceLocation resourceLocation, boolean wasAllowed)
    {
        if (Config.debug &&
            (
                (this.type == Type.STRUCTURE && wasAllowed && Config.logAllowedStructure) ||
                (this.type == Type.STRUCTURE && !wasAllowed && Config.logPreventedStructure) ||
                (this.type == Type.STRUCTURE_POOL_ELEMENT && wasAllowed && Config.logAllowedStructurePoolElement) ||
                (this.type == Type.STRUCTURE_POOL_ELEMENT && !wasAllowed && Config.logPreventedStructurePoolElement) ||
                (this.type == Type.FEATURE && wasAllowed && Config.logAllowedFeature) ||
                (this.type == Type.FEATURE && !wasAllowed && Config.logPreventedFeature) ||
                (this.type == Type.ENTITY && wasAllowed && Config.logAllowedEntity) ||
                (this.type == Type.ENTITY && !wasAllowed && Config.logPreventedEntity) ||
                (this.type == Type.LOOT && wasAllowed && Config.logAllowedLoot) ||
                (this.type == Type.LOOT && !wasAllowed && Config.logPreventedLoot)
            )
        )
        {
            String msg = String.format(
                    "%s resource \"%s\" in dimension \"%s\" was %s due to %s %s.",
                    this.getType(),
                    resourceLocation.toString(),
                    dimensionLocation.toString(), wasAllowed ? "allowed" : "prevented",
                    ((wasAllowed && this.mode == Mode.WHITELIST) || (!wasAllowed && this.mode == Mode.BLACKLIST)) ? "being" : "not being",
                    (this.mode == Mode.WHITELIST) ? "whitelisted" : "blacklisted");

            GroupingLogger.logDebug(msg);
        }
    }
}