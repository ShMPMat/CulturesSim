package simulation.space.resource.tag;

import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;

import java.util.Collection;
import java.util.Objects;

public class ResourceTag {
    public String name;
    /**
     * Whether Resource doesn't waste on use.
     */
    public boolean isInstrumental;
    public int level;

    public ResourceTag(String name, boolean isInstrumental, int level) {
        this.name = name;
        this.isInstrumental = isInstrumental;
        this.level = level;
    }

    public ResourceTag(String name, boolean isInstrumental) {
        this(name, isInstrumental, 1);
    }

    public ResourceTag(String name) {
        this(name, false);
    }


    public ResourceTag(String name, int level) {
        this(name, false, level);
    }

    public boolean isInstrumental() {
        return isInstrumental;
    }

    private Resource consumeAndGetResult(Resource resource, int ceiling) {
        return resource.getPart(ceiling);
    }

    public MutableResourcePack consumeAndGetResult(Collection<Resource> resources, int ceiling) {
        MutableResourcePack resourceResult = new MutableResourcePack();
        for (Resource resource : resources) {
            resourceResult.add(consumeAndGetResult(resource, ceiling));
        }
        return resourceResult;
    }

    public ResourceTag copy() {
        return new ResourceTag(name, isInstrumental, level);
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
}
