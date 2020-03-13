package simulation.culture.aspect.dependency;

import kotlin.Pair;
import simulation.culture.aspect.*;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Collection;
import java.util.Objects;

public class ConversionDependency extends AbstractDependency {
    private Pair<Resource, Aspect> conversion;
    private Group group;

    public ConversionDependency(ResourceTag tag, Group group, Pair<Resource, Aspect> conversion) {
        super(tag);
        this.conversion = conversion;
        this.group = group;
    }

    @Override
    public String getName() {
        return conversion.getSecond().getName() + " on " + conversion.getFirst().getBaseName();
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        return aspect instanceof ConverseWrapper && conversion.getSecond().equals(((ConverseWrapper) aspect).aspect);
    }

    public boolean isCycleDependencyInner(Aspect aspect) {
        return aspect instanceof ConverseWrapper && conversion.getSecond().equals(((ConverseWrapper) aspect).aspect) ||
                conversion.getSecond().equals(aspect);
    }

    @Override
    public AspectResult useDependency(AspectController controller) {
        ResourcePack resourcePack = new ResourcePack();
        Collection<Resource> resourceInstances = group.getOverallTerritory().getResourceInstances(conversion.getFirst());
        for (Resource res : resourceInstances) {
            if (controller.getCeiling() <= controller.getEvaluator().evaluate(resourcePack)) {
                break;
            }
            resourcePack.add(res.applyAndConsumeAspect(conversion.getSecond(),
                    controller.getCeiling() - controller.getEvaluator().evaluate(resourcePack)));
        }
        return new AspectResult(resourcePack, null);
    }

    @Override
    public ConversionDependency copy(Group group) {
        return new ConversionDependency(tag, group, conversion);
    }

    @Override
    public void swapDependencies(Group group) {
        conversion = new Pair<>(
                conversion.getFirst(),
                group.getCulturalCenter().getAspectCenter().getAspectPool().get(conversion.getSecond())
        );
        if (conversion.getSecond() == null) {
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
