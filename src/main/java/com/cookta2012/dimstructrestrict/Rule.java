package com.cookta2012.dimstructrestrict;

import java.util.Objects;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;



public abstract class Rule {
	
    public enum Mode { WHITELIST, BLACKLIST }
    public enum Type { DIMENSION, STRUCTURE }
    
    private final ResourceLocation id;    
    private final Type type;
    private final Mode mode;
    private final Set<ResourceLocation> resource;
    private final Boolean false_place;
    private final Boolean active;

    public Rule(ResourceLocation id, Type type, Mode mode, Set<ResourceLocation> resource, Boolean false_place, Boolean active) {
    	this.id = id;
    	this.type = type;
        this.mode = mode;
        this.resource = resource;
        this.false_place = false_place;
        this.active = active;
    }

    private String getType() {
        return switch (this.type) {
            case DIMENSION -> "Dimension";
            case STRUCTURE -> "Structure";
            default -> throw new IllegalStateException("Attempted to return invalid Rule Type");
        };
    }
    
    private String getMode() {
        return switch (this.mode) {
            case WHITELIST -> "Whitelist";
            case BLACKLIST -> "Blacklist";
            default -> throw new IllegalStateException("Attempted to return invalid Mode Type");
        };
    }

    public Mode mode() { return mode; }
    public Set<ResourceLocation> resource() { return resource; }
    public Boolean false_place() { return false_place; }
    public Boolean active() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(mode, rule.mode)
            && Objects.equals(resource, rule.resource)
            && Objects.equals(false_place, rule.false_place)
            && Objects.equals(active, rule.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, resource, false_place, active);
    }

    @Override
    public String toString() {
        return "Rule{" +
               "mode=" + mode +
               ", resource=" + resource +
               ", false_place=" + false_place +
               ", active=" + active +
               '}';
    }
    
    
	public Boolean isRestricted(ResourceLocation targetResource, Boolean isClean) {
		// Short circuit due to rule inactive or false_place active
		if(!this.active) { 
			DimensionalStructureRestrict.LOGGER.debug("Rule not active" + this.type.toString());
			return false; 
		}
		if(this.false_place && !isClean) {
			DimensionalStructureRestrict.LOGGER.debug("Rule set to false_place");
			{ return false; }
		}
        if (this.mode == Mode.WHITELIST) {
            if (!this.resource().contains(targetResource)) {
            	logDebugMessage( isClean, targetResource );
                return true;
            }
        } else { // BLACKLIST
            if (this.resource().contains(targetResource)) {
            	logDebugMessage( isClean, targetResource);
            	return true;
            }
        }
        return false;
    }
	
	private void logDebugMessage(Boolean isClean, ResourceLocation targetResource) {
		DimensionalStructureRestrict.LOGGER.debug(getType() + " Rule " + this.getMode()
        + (!isClean ? "un" : "" ) + "cleanly prevented generation of structure: " 
        + ((this.type == Type.DIMENSION) ? targetResource.toString() : id.toString())
        + " in Dimension: " 
        + ((this.type == Type.STRUCTURE) ? targetResource.toString() : id.toString())
        + " due to " 
        + ((this.mode == Mode.WHITELIST) ? "not being whitelisted": "being blacklisted")
        );
	}

}


