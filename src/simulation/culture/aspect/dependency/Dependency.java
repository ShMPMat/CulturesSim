package simulation.culture.aspect.dependency;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.space.resource.ResourceTag;
import simulation.culture.group.Group;

public interface Dependency {
    String getName();
    boolean isCycleDependency(Aspect aspect);
    boolean isCycleDependencyInner(Aspect aspect);
    AspectResult useDependency(AspectController controller);
    boolean isPhony();
    ResourceTag getType();
    Dependency copy(Group group);
    void swapDependencies(Group group);
}
