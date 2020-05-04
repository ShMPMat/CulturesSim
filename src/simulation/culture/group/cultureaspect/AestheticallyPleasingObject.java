package simulation.culture.group.cultureaspect;

import simulation.culture.group.centers.Group;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceRequest;
import simulation.culture.group.resource_behaviour.ResourceBehaviour;
import simulation.space.resource.Resource;

import java.util.Objects;

public class AestheticallyPleasingObject implements CultureAspect {
    private Resource resource;
    private ResourceBehaviour resourceBehaviour;

    public AestheticallyPleasingObject(Resource resource, ResourceBehaviour resourceBehaviour) {
        this.resource = resource;
        this.resourceBehaviour = resourceBehaviour;
    }

    @Override
    public Request getRequest(Group group) {
        return new ResourceRequest(group, resource, 1, 10, (pair, percent) -> {
            pair.getFirst().getResourceCenter().addAll(pair.getSecond());
            resourceBehaviour.proceedResources(pair.getSecond());
            return null;
        },
                (pair, percent) -> {
                    pair.getFirst().getResourceCenter().addAll(pair.getSecond());
                    resourceBehaviour.proceedResources(pair.getSecond());
                    return null;
                });
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public void use(Group group) {

    }

    @Override
    public AestheticallyPleasingObject adopt(Group group) {
        return new AestheticallyPleasingObject(resource, resourceBehaviour);
    }

    @Override
    public String toString() {
        return "Aesthetically pleasing " + resource.getFullName() + " " + resourceBehaviour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AestheticallyPleasingObject that = (AestheticallyPleasingObject) o;
        return Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource);
    }

    @Override
    public void die(Group group) {}
}
