package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceEvaluator;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Collection;

public class ConversionDependency extends AbstractDependency {
    private ShnyPair<Resource, Aspect> conversion;
    private Group group;

    public ConversionDependency(AspectTag tag, Group group, ShnyPair<Resource, Aspect> conversion) {
        super(tag);
        this.conversion = conversion;
        this.group = group;
    }

    @Override
    public String getName() {
        return conversion.second.getName() + " on " + conversion.first.getBaseName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        return (conversion.second.equals(aspect) && conversion.second != aspect);
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator) {
        ResourcePack resourcePack = new ResourcePack();
        Collection<Resource> resourceInstances = group.getOverallTerritory().getResourceInstances(conversion.first);
        for (Resource res : resourceInstances) {
            if (ceiling <= evaluator.evaluate(resourcePack)) {
                break;
            }
            resourcePack.add(res.applyAndConsumeAspect(conversion.second,
                    ceiling - evaluator.evaluate(resourcePack)));
        }
        return new ShnyPair<>(true, resourcePack);
    }
}
