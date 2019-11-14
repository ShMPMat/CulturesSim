package simulation.culture.aspect.dependency;

import simulation.culture.aspect.AspectTag;

import java.util.Objects;

abstract class AbstractDependency implements Dependency {
    protected AspectTag tag;

    AbstractDependency(AspectTag tag) {
        this.tag = tag;
    }

    @Override
    public boolean isPhony() {
        return tag.name.equals("phony");
    }

    @Override
    public AspectTag getType() {
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
