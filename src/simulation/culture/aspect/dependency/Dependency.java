package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

public interface Dependency {
    String getName();
    boolean isCycleDependency(Aspect aspect);
    ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator);
    boolean isPhony();
    AspectTag getType();
    Dependency copy(Group group);
    void swapDependencies(Group group);
}
