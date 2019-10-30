package simulation.culture.aspect;

import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Collection;
import java.util.Objects;

/**
 * Class which represents cultural meaning of an object, material.
 */
public class AspectTag {
    /**
     * Name of the AspectTag
     */
    public String name;
    /**
     * Whether it don't need any resource.
     */
    boolean isAbstract;
    /**
     * Whether Resource doesn't waste on use.
     */
    boolean isInstrumental;
    public int level = 1;

    public AspectTag(String name, boolean isAbstract, boolean isInstrumental) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.isInstrumental = isInstrumental;
    }

    public AspectTag(String name) {
        this(name, false, false);
    }

    private Resource consumeAndGetResult(Resource resource, int ceiling) {
        return resource.getPart(ceiling);
    }

    ResourcePack consumeAndGetResult(Collection<Resource> resources, int ceiling) {
        ResourcePack resourceResult = new ResourcePack();
        for (Resource resource : resources) {
            resourceResult.add(consumeAndGetResult(resource, ceiling));
        }
        return resourceResult;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AspectTag aspectTag = (AspectTag) o;
        return name.equals(aspectTag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
