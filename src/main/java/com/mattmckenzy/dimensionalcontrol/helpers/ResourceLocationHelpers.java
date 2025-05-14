package com.mattmckenzy.dimensionalcontrol.helpers;

import com.mattmckenzy.dimensionalcontrol.implementation.Rule;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceLocationHelpers
{
    public static Collection<ResourceLocation> expandLocation(Rule.Type ruleType, RegistryAccess registryAccess, String resourceLocationString)
    {
        Collection<ResourceLocation> returnResourceLocations = new ArrayList<>();
        Pattern specialSymbolPattern = Pattern.compile("[^a-z0-9:]", Pattern.CASE_INSENSITIVE);
        Matcher specialSymbolMatcher = specialSymbolPattern.matcher(resourceLocationString);

        if (specialSymbolMatcher.find())
        {
            for (ResourceLocation key : RegistryHelpers.getResourceLocations(ruleType, registryAccess))
            {
                Pattern resourceLocationPattern = Pattern.compile(resourceLocationString, Pattern.CASE_INSENSITIVE);
                Matcher resourceLocationMatcher = resourceLocationPattern.matcher(key.toString());
                if (resourceLocationMatcher.find())
                {
                    returnResourceLocations.add(key);
                }
            }
        }
        else
        {
            ResourceLocation resourceLocation = ResourceLocation.parse(resourceLocationString);
            returnResourceLocations.add(resourceLocation);
        }

        if (returnResourceLocations.isEmpty())
        {
            GroupingLogger.logWarning(String.format("Could not find any resource matching '%s'", resourceLocationString));
        }

        return returnResourceLocations;
    }
}
