package simulation.culture.group.cultureaspect;

import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceRequest;
import simulation.space.resource.Resource;

import java.util.Objects;

public class AestheticallyPleasingObject extends AbstractCultureAspect {
    private Resource resource;
    private ResourceBehaviour resourceBehaviour;

    public AestheticallyPleasingObject(Group group, Resource resource, ResourceBehaviour resourceBehaviour) {
        super(group);
        this.resource = resource;
        this.resourceBehaviour = resourceBehaviour;
    }

    public AestheticallyPleasingObject(Group group, Resource resource) {
        this(group, resource, ResourceBehaviour.getRandom(group));
    }

    @Override
    public Request getRequest() {
        return new ResourceRequest(group, resource, 1, 10, (pair, percent) -> {
            pair.first.cherishedResources.add(pair.second);
            //addAspiration(new Aspiration(5, want.first));
            resourceBehaviour.procedeResources(pair.second);
            return null;
        },
                (pair, percent) -> {
                    pair.first.cherishedResources.add(pair.second);
                    resourceBehaviour.procedeResources(pair.second);
                    return null;
                });
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public void use() {

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
}
