package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceEvaluator;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class ResourceDependency extends AbstractDependency {
    private Resource resource;
    private Group group;

    public ResourceDependency(AspectTag tag, Group group, Resource resource) {
        super(tag);
        this.resource = resource;
        this.group = group;
    }

    @Override
    public String getName() {
        return resource.getBaseName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        return resource != null;
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator) {
        ResourcePack resourcePack = new ResourcePack();
        if (resource != null) {//TODO I dont like this shit, why is it working through gdamn AspectTag??
            return new ShnyPair<>(true, tag.consumeAndGetResult(group.getOverallGroup().getTerritory().getResourceInstances(resource), ceiling));
        }
        return new ShnyPair<>(true, resourcePack);
    }

    public AspectTag getType() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ResourceDependency that = (ResourceDependency) o;
        return Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resource);
    }
}
