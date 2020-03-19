package simulation.culture.aspect.dependency;

import kotlin.Pair;
import simulation.culture.aspect.*;
import simulation.culture.group.AspectCenter;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Objects;
import java.util.stream.Collectors;

public class LineDependency extends AbstractDependency {
    private boolean isAlreadyUsed = false;
    private Pair<ConverseWrapper, ConverseWrapper> line;

    public LineDependency(ResourceTag tag, Pair<ConverseWrapper, ConverseWrapper> line) {
        super(tag);
        this.line = line;
    }

    @Override
    public String getName() {
        return line.getFirst().getName() + " from " + line.getSecond().getName();
    }

    public Pair<ConverseWrapper, ConverseWrapper> getLine() {
        return line;
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        if (isAlreadyUsed) {
            return false;
        }
        isAlreadyUsed = true;
        boolean b = line.getSecond().getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect))) || (line.getSecond().equals(aspect));
        isAlreadyUsed = false;
        return b;
    }

    @Override
    public boolean isCycleDependencyInner(Aspect aspect) {
        if (isAlreadyUsed) {
            return false;
        }
        isAlreadyUsed = true;
        boolean b = line.getSecond().getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect))) || (line.getSecond().equals(aspect));
        isAlreadyUsed = false;
        return b;
    }

    @Override
    public AspectResult useDependency(AspectController controller) {
        try {
            MutableResourcePack resourcePack = new MutableResourcePack();
            if (isAlreadyUsed || controller.getCeiling() <= 0 || !foodForInsertMeaning() && controller.isMeaningNeeded()) {
                return new AspectResult(resourcePack, null);
            }
            isAlreadyUsed = true;
            AspectResult _p = line.getSecond().use(new AspectController(
                            controller.getCeiling(),
                            controller.getFloor(),
                            new ResourceEvaluator(
                                    rp -> rp.getPackedResource(line.getFirst().resource),
                                    rp -> rp.getAmount(line.getFirst().resource)
                            ),
                            controller.getPopulationCenter(),
                            controller.getTerritory(),
                            controller.isMeaningNeeded(),
                            controller.getMeaning()
                    ));
            resourcePack.addAll(_p.resources.getPackedResource(line.getFirst().resource).getResources().stream()
                    .flatMap(res -> res.applyAndConsumeAspect(line.getFirst().aspect, controller.getCeiling()).stream())
                    .collect(Collectors.toList()));
            resourcePack.addAll(_p.resources);
            isAlreadyUsed = false;
            return new AspectResult(_p.isFinished, resourcePack, null);
        } catch (NullPointerException e) {
            throw new RuntimeException("No such aspect in Group");
        }
    }

    private boolean foodForInsertMeaning() {
        return !tag.equals(ResourceTag.phony()) || getNextWrapper().canInsertMeaning;
    }

    public ConverseWrapper getNextWrapper() {
        return line.getSecond();
    }

    @Override
    public LineDependency copy() {
        return new LineDependency(tag, line);
    }

    @Override
    public void swapDependencies(AspectCenter aspectCenter) {
        line = new Pair<>(
                (ConverseWrapper) aspectCenter.getAspectPool().get(line.getSecond()),
                (ConverseWrapper) aspectCenter.getAspectPool().get(line.getSecond())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LineDependency that = (LineDependency) o;
        return Objects.equals(line, that.line);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), line);
    }
}
