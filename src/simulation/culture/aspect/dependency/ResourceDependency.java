package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
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
    public boolean isCycleDependencyInner(Aspect aspect) {
        return resource != null;
    }

    @Override
    public AspectResult useDependency(int ceiling, ResourceEvaluator evaluator) {
        ResourcePack resourcePack = new ResourcePack();
        if (resource != null) {//TODO I dont like this shit, why is it working through gdamn AspectTag??
            return new AspectResult(tag.consumeAndGetResult(group.getParentGroup().getTerritory().getResourceInstances(resource), ceiling), null);
        }
        return new AspectResult(resourcePack, null);
    }

    public AspectTag getType() {
        return tag;
    }

    @Override
    public ResourceDependency copy(Group group) {
        return new ResourceDependency(tag, group, resource);
    }

    @Override
    public void swapDependencies(Group group) {
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
