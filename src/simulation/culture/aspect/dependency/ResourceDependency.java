package simulation.culture.aspect.dependency;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.space.resource.tag.ResourceTag;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class ResourceDependency extends AbstractDependency {
    private Resource resource;
    private Group group;

    public ResourceDependency(ResourceTag tag, Group group, Resource resource) {
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
        return false;
    }

    @Override
    public AspectResult useDependency(AspectController controller) {
        ResourcePack resourcePack = new ResourcePack();
        if (resource != null) {//TODO I dont like this shit, why is it working through gdamn AspectTag??
            return new AspectResult(tag.consumeAndGetResult(group.getTerritory()//TODO mayhaps i should pass territory; because i may want to use resources from all the GroupConglomerationTiles
                    .getResourceInstances(resource), controller.getCeiling()), null);
        }
        return new AspectResult(resourcePack, null);
    }

    public ResourceTag getType() {
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
