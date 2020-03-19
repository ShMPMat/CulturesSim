package simulation.culture.aspect.dependency;

import kotlin.Pair;
import simulation.culture.aspect.*;
import simulation.culture.group.AspectCenter;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Collection;
import java.util.Objects;

public class ConversionDependency extends AbstractDependency {
    private Pair<Resource, Aspect> conversion;

    public ConversionDependency(ResourceTag tag, Pair<Resource, Aspect> conversion) {
        super(tag);
        this.conversion = conversion;
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
        MutableResourcePack resourcePack = new MutableResourcePack();
        Collection<Resource> resourceInstances = controller.getTerritory().getResourceInstances(conversion.getFirst());
        for (Resource res : resourceInstances) {
            if (controller.getCeiling() <= controller.getEvaluator().evaluate(resourcePack)) {
                break;
            }
            resourcePack.addAll(res.applyAndConsumeAspect(conversion.getSecond(),
                    controller.getCeiling() - controller.getEvaluator().evaluate(resourcePack)));
        }
        return new AspectResult(resourcePack, null);
    }

    @Override
    public ConversionDependency copy() {
        return new ConversionDependency(tag, conversion);
    }

    @Override
    public void swapDependencies(AspectCenter aspectCenter) {
        conversion = new Pair<>(
                conversion.getFirst(),
                aspectCenter.getAspectPool().get(conversion.getSecond())
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
