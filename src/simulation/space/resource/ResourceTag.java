package simulation.space.resource;

import java.util.Collection;
import java.util.Objects;

/**
 * Class which represents cultural meaning of an object, material.
 */
public class ResourceTag {
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
    public boolean isInstrumental;
    public boolean isConverseCondition;
    public int level;

    public ResourceTag(String name, boolean isAbstract, boolean isInstrumental, boolean isConverseCondition, int level) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.isInstrumental = isInstrumental;
        this.isConverseCondition = isConverseCondition;
        this.level = level;
    }

    public ResourceTag(String name, boolean isAbstract, boolean isInstrumental, boolean isConverseCondition) {
        this(name, isAbstract, isInstrumental, isConverseCondition, 1);
    }

    public ResourceTag(String name) {
        this(name, false, false, false);
    }


    public ResourceTag(String name, int level) {
        this(name, false, false, false, level);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInstrumental() {
        return isInstrumental;
    }

    private Resource consumeAndGetResult(Resource resource, int ceiling) {
        return resource.getPart(ceiling);
    }

    public ResourcePack consumeAndGetResult(Collection<Resource> resources, int ceiling) {
        ResourcePack resourceResult = new ResourcePack();
        for (Resource resource : resources) {
            resourceResult.add(consumeAndGetResult(resource, ceiling));
        }
        return resourceResult;
    }

    public ResourceTag copy() {
        return new ResourceTag(name, isAbstract, isInstrumental, isConverseCondition, level);
    }


    public static ResourceTag phony() {
        return new ResourceTag("phony");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceTag resourceTag = (ResourceTag) o;
        return name.equals(resourceTag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public boolean isWrapperCondition() {
        return isConverseCondition;
    }
}
