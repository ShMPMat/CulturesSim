package simulation.culture.aspect.dependency;

import simulation.culture.aspect.*;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourceTag;

import java.util.Objects;

public class AspectDependency extends AbstractDependency {
    static int debug = 0;
    private Aspect aspect;

    public AspectDependency(ResourceTag tag, Aspect aspect) {
        super(tag);
        this.aspect = aspect;
    }

    @Override
    public String getName() {
        return aspect.getName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        debug = 0;
        if (aspect instanceof ConverseWrapper && this.aspect.equals(((ConverseWrapper) aspect).aspect)) {
            return true;
        }
        return this.aspect.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect)));
    }

    @Override
    public boolean isCycleDependencyInner(Aspect aspect) {
        try {
            debug++;
            if (debug == 100) {
                int i = 0;
            }
            if (this.aspect.equals(aspect) ||
                    aspect instanceof ConverseWrapper && this.aspect.equals(((ConverseWrapper) aspect).aspect)) {
                return true;
            }
            return this.aspect.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                    .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect)));
        } catch (StackOverflowError e) {
            throw new RuntimeException("Endless dependencies");
        }
    }

    @Override
    public AspectResult useDependency(AspectController controller) {
        try {
            return aspect.use(new AspectController(controller.ceiling, controller.floor, new ResourceEvaluator()));
        } catch (StackOverflowError e) {
            throw new RuntimeException("Infinite Dependency");
        }
    }

    @Override
    public AspectDependency copy(Group group) {
        return new AspectDependency(tag, aspect);
    }

    @Override
    public void swapDependencies(Group group) {
        aspect = group.getAspect(aspect);
        if (aspect == null) {
            throw new RuntimeException(String.format("Wrong swapping in Dependency %s", getName()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AspectDependency that = (AspectDependency) o;
        return Objects.equals(aspect, that.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), aspect);
    }
}
