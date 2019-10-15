package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;

/**
 * Class which stores source to which an Aspect requirement is linked.
 */
public class Dependency {
    private Resource resource;
    private Aspect aspect;
    private ShnyPair<Resource, Aspect> conversion;
    private ShnyPair<ConverseWrapper, ConverseWrapper> line;
    private AspectTag type;
    private Group group;

    private Dependency(AspectTag type, Group group) {
        this.type = type;
        this.group = group;
        this.resource = null;
        this.aspect = null;
        this.conversion = null;
    }

    public Dependency(AspectTag type, Group group, Resource resource) {
        this(type, group);
        this.resource = resource;
    }

    public Dependency(AspectTag type, Group group, Aspect aspect) {
        this(type, group);
        this.aspect = aspect;
    }

    public Dependency(AspectTag type, Group group, ShnyPair<Resource, Aspect> conversion) {
        this(type, group);
        this.conversion = conversion;
    }

    public Dependency(AspectTag type, ShnyPair<ConverseWrapper, ConverseWrapper> line, Group group) {
        this(type, group);
        this.line = line;
    }

    public String getName() {
        if (resource != null) {
            return resource.getBaseName();
        } else if (aspect != null){
            return aspect.getName();
        } else if (conversion != null) {
            return conversion.second.getName() + " on " + conversion.first.getBaseName();
        } else if (line != null) {
            return line.first.getName() + " from " + line.second.getName();
        }
        return "What?";
    }

    public AspectTag getType() {
        return type;
    }

    public ConverseWrapper getNextWrapper() {
        if (line == null) {
            return null;
        }
        return line.second;
    }

    public ShnyPair<ConverseWrapper, ConverseWrapper> getLine() {
        return line;
    }

    public boolean isCycleDependency(Aspect aspect) {
        if (resource != null) {
            return false;
        }
        if (this.aspect != null) {
            if (this.aspect.equals(aspect)) {
                return true;
            }
            return this.aspect.dependencies.values().stream().anyMatch(dependencies -> dependencies.stream()
                    .anyMatch(dependency -> dependency.isCycleDependency(aspect)));
        }
        if (conversion != null) {
            return (conversion.second.equals(aspect) && conversion.second != aspect);
        }
        if (line != null) {//TODO cant TakeApart Plants, which Planted from Seed, TakeAparted from Plant
            return line.second.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
            .anyMatch(dependency -> dependency.isCycleDependency(aspect))) || line.second.equals(aspect);
        }
        return false;
    }

    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, Function<ResourcePack, Integer> amount) {
        ResourcePack resourcePack = new ResourcePack();
        if (type.isAbstract) {
            return new ShnyPair<>(true, resourcePack);
        }
        if (aspect != null) {
            return aspect.use(ceiling, r -> ceiling);
        }
        if (resource != null) {//TODO I dont like this shit, why is it working through gdamn AspectTag??
            return new ShnyPair<>(true, type.consumeAndGetResult(group.getOverallGroup().getTerritory().getResourceInstances(resource), ceiling));
        }
        if (conversion != null){
            Collection<Resource> resourceInstances = group.getOverallTerritory().getResourceInstances(conversion.first);
            for (Resource res : resourceInstances) {
                if (ceiling <= amount.apply(resourcePack)) {
                    break;
                }
                resourcePack.add(res.applyAndConsumeAspect(conversion.second,
                        ceiling - amount.apply(resourcePack)));
            }
            return new ShnyPair<>(true, resourcePack);
        }
        if (line != null) {
            ShnyPair<Boolean, ResourcePack> _p = group.getAspect(line.second).use(ceiling,
                    rp -> rp.getAmountOfResource(line.first.resource));
            _p.second.getResource(line.first.resource)
                    .forEach(res -> res.applyAndConsumeAspect(line.first.aspect, ceiling));
            resourcePack.add(_p.second);
            return new ShnyPair<>(_p.first, resourcePack);
        }
        return new ShnyPair<>(false, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(resource, that.resource) &&
                Objects.equals(aspect, that.aspect) &&
                Objects.equals(type, that.type) &&
                Objects.equals(conversion, that.conversion) &&
                Objects.equals(line, that.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, conversion, type, line, aspect);
    }

    @Override
    public String toString() {
        return "Dependency " + type.name + " " + getName();
    }
}
