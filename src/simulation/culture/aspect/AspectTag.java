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
    boolean isConverseCondition;
    public int level;

    public AspectTag(String name, boolean isAbstract, boolean isInstrumental, boolean isConverseCondition, int level) {
        this.name = name;
        this.isAbstract = isAbstract;
        this.isInstrumental = isInstrumental;
        this.isConverseCondition = isConverseCondition;
        this.level = level;
    }

    public AspectTag(String name, boolean isAbstract, boolean isInstrumental, boolean isConverseCondition) {
        this(name, isAbstract, isInstrumental, isConverseCondition, 1);
    }

    public AspectTag(String name) {
        this(name, false, false, false);
    }


    public AspectTag(String name, int level) {
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

    public AspectTag copy() {
        return new AspectTag(name, isAbstract, isInstrumental, isConverseCondition, level);
    }


    public static AspectTag phony() {
        return new AspectTag("phony");
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

    public boolean isWrapperCondition() {
        return isConverseCondition;
    }
}
