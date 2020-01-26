package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.Collection;
import java.util.Objects;

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

    @Override
    public ConversionDependency copy(Group group) {
        return new ConversionDependency(tag, group, conversion);
    }

    @Override
    public void swapDependencies(Group group) {
        conversion = new ShnyPair<>(conversion.first, group.getAspect(conversion.second));
        if (conversion.second == null) {
            int i = 0;
            throw new RuntimeException(String.format("Wrong swapping in Dependency %s", getName()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConversionDependency that = (ConversionDependency) o;
        return super.equals(o) && Objects.equals(conversion, that.conversion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conversion);
    }
}
