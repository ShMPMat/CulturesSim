package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class AspectDependency extends AbstractDependency {
    private Aspect aspect;

    public AspectDependency(AspectTag tag, Aspect aspect) {
        super(tag);
        this.aspect = aspect;
    }

    @Override
    public String getName() {
        return aspect.getName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        if (aspect instanceof ConverseWrapper && this.aspect.equals(((ConverseWrapper) aspect).aspect)) {
            return true;
        }
        return this.aspect.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect)));
    }

    @Override
    public boolean isCycleDependencyInner(Aspect aspect) {
        try {
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
    public AspectResult useDependency(int ceiling, ResourceEvaluator evaluator) {
        return aspect.use(ceiling, new ResourceEvaluator());
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
