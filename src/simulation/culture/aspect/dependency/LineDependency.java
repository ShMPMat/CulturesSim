package simulation.culture.aspect.dependency;

import kotlin.Pair;
import simulation.culture.aspect.*;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.Objects;
import java.util.stream.Collectors;

public class LineDependency extends AbstractDependency {
    private boolean isAlreadyUsed = false;
    private Group group;
    private Pair<ConverseWrapper, ConverseWrapper> line;

    public LineDependency(ResourceTag tag, Group group, Pair<ConverseWrapper, ConverseWrapper> line) {
        super(tag);
        this.group = group;
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
            ResourcePack resourcePack = new ResourcePack();
            if (isAlreadyUsed || controller.getCeiling() <= 0 || !foodForInsertMeaning() && controller.isMeaningNeeded()) {
                return new AspectResult(resourcePack, null);
            }
            isAlreadyUsed = true;
            AspectResult _p = group.getCulturalCenter().getAspectCenter().getAspectPool().get(line.getSecond())
                    .use(new AspectController(
                            controller.getCeiling(),
                            controller.getFloor(),
                            new ResourceEvaluator(
                                    rp -> rp.getResource(line.getFirst().resource),
                                    rp -> rp.getAmountOfResource(line.getFirst().resource)
                            ),
                            group,
                            controller.isMeaningNeeded()));
            resourcePack.add(_p.resources.getResource(line.getFirst().resource).getResources().stream()
                    .flatMap(res -> res.applyAndConsumeAspect(line.getFirst().aspect, controller.getCeiling()).stream())
                    .collect(Collectors.toList()));
            resourcePack.add(_p.resources);
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
    public LineDependency copy(Group group) {
        return new LineDependency(tag, group, line);
    }

    @Override
    public void swapDependencies(Group group) {
        line = new Pair<>(
                (ConverseWrapper) group.getCulturalCenter().getAspectCenter().getAspectPool().get(line.getSecond()),
                (ConverseWrapper) group.getCulturalCenter().getAspectCenter().getAspectPool().get(line.getSecond())
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
