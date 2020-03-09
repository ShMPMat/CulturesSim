package simulation.culture.aspect.dependency;

import simulation.space.resource.tag.ResourceTag;

import java.util.Objects;

abstract class AbstractDependency implements Dependency {
    protected ResourceTag tag;

    AbstractDependency(ResourceTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean isPhony() {
        return tag.name.equals("phony");
    }

    @Override
    public ResourceTag getType() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDependency that = (AbstractDependency) o;
        return Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }
}
