package com.cookta2012.dimstructrestrict;

import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;



public abstract class Rule {	
    public enum Mode { WHITELIST, BLACKLIST }
    public enum Type { DIMENSION, STRUCTURE }
    
    public final ResourceLocation id;    
    public final Type type;
    public final Mode mode;
    public final Set<ResourceLocation> resource;
    public final Boolean false_place;
    public final Boolean active;

    public Rule(ResourceLocation id, Type type, Mode mode, Set<ResourceLocation> resource, Boolean false_place, Boolean active) {
    	this.id = id;
    	this.type = type;
        this.mode = mode;
        this.resource = resource;
        this.false_place = false_place;
        this.active = active;
    }

    public String getType() {
        return switch (this.type) {
            case DIMENSION -> "Dimension";
            case STRUCTURE -> "Structure";
            default -> throw new IllegalStateException("Attempted to return invalid Rule Type");
        };
    }
    
    public String getMode() {
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
		
		DimensionalStructureRestrict.logMsg("Begin Processing Rule");
		// Short circuit due to rule inactive or false_place active
		if(!this.active) { 
			DimensionalStructureRestrict.logMsg("Rule not active" + this.type.toString());
			return false; 
		}
		if(this.false_place && !isClean) {
			DimensionalStructureRestrict.logMsg("Rule set to false_place");
			return false;
		}
        if (this.mode == Mode.WHITELIST) {
            if (!this.resource().contains(targetResource)) {
            	logDebugMessage( targetResource, isClean );
                return true;
            }
        } else { // BLACKLIST
            if (this.resource().contains(targetResource)) {
            	logDebugMessage( targetResource, isClean );
            	return true;
            }
        }
        DimensionalStructureRestrict.logMsg("Fell Through Processing Rule");
        DimensionalStructureRestrict.logMsg("---START What Could Have Been--------");
		logDebugMessage( targetResource, isClean );
		DimensionalStructureRestrict.logMsg("---END What Could Have Been-------");
        return false;
    }
	
	private void logDebugMessage(ResourceLocation targetResource, Boolean isClean) {
	    String ruleType = getType(); // "Dimension" or "Structure"
	    String modeText = getMode(); // "WHITELIST" or "BLACKLIST"
	    String preventionType = isClean ? "cleanly" : "uncleanly";

	    String blockedInDimension = (this.type == Type.DIMENSION) ? id.toString() : targetResource.toString() ;
	    String blockedResource = (this.type == Type.STRUCTURE) ?  id.toString() : targetResource.toString();

	    String reason = (this.mode == Mode.WHITELIST)
	        ? "not being whitelisted"
	        : "being blacklisted";

	    StringBuilder msg = new StringBuilder();
	    msg.append(ruleType)
	       .append(" Rule ")
	       .append(modeText)
	       .append(" ")
	       .append(preventionType)
	       .append(" prevented generation of Structure: ")
	       .append(blockedResource)
	       .append(" in Dimension: ")
	       .append(blockedInDimension)
	       .append(" due to ")
	       .append(reason);

	    DimensionalStructureRestrict.logMsg(msg.toString());
	}


}


