package simulation.culture.aspect.dependency;

import simulation.culture.aspect.AspectTag;

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
}
