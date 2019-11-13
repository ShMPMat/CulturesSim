package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

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
        if (this.aspect.equals(aspect)) {
            return true;
        }
        return this.aspect.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependency(aspect)));
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator) {
        return aspect.use(ceiling, new ResourceEvaluator());
    }
}
