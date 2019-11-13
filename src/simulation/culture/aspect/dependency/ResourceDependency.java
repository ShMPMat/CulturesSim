package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceEvaluator;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

public class ResourceDependency implements Dependency {
    private Resource resource;
    private AspectTag tag;
    private Group group;

    @Override
    public String getName() {
        return resource.getBaseName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        return resource != null;
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator) {
        ResourcePack resourcePack = new ResourcePack();
        if (resource != null) {//TODO I dont like this shit, why is it working through gdamn AspectTag??
            return new ShnyPair<>(true, tag.consumeAndGetResult(group.getOverallGroup().getTerritory().getResourceInstances(resource), ceiling));
        }
        return new ShnyPair<>(true, resourcePack);
    }

    public AspectTag getType() {
        return tag;
    }
}
