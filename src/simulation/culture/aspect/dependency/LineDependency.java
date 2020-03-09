package simulation.culture.aspect.dependency;

import extra.ShnyPair;
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
    private ShnyPair<ConverseWrapper, ConverseWrapper> line;

    public LineDependency(ResourceTag tag, Group group, ShnyPair<ConverseWrapper, ConverseWrapper> line) {
        super(tag);
        this.group = group;
        this.line = line;
    }

    @Override
    public String getName() {
        return line.first.getName() + " from " + line.second.getName();
    }

    public ShnyPair<ConverseWrapper, ConverseWrapper> getLine() {
        return line;
    }

    @Override
    public boolean isCycleDependency(Aspect aspect) {
        if (isAlreadyUsed) {
            return false;
        }
        isAlreadyUsed = true;
        boolean b = line.second.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect))) || (line.second.equals(aspect));
        isAlreadyUsed = false;
        return b;
    }

    @Override
    public boolean isCycleDependencyInner(Aspect aspect) {
        if (isAlreadyUsed) {
            return false;
        }
        isAlreadyUsed = true;
        boolean b = line.second.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependencyInner(aspect))) || (line.second.equals(aspect));
        isAlreadyUsed = false;
        return b;
    }

    @Override
    public AspectResult useDependency(AspectController controller) {
        try {
            ResourcePack resourcePack = new ResourcePack();
            if (isAlreadyUsed || controller.ceiling <= 0 || !foodForInsertMeaning() && controller.isMeaningNeeded) {
                return new AspectResult(resourcePack, null);
            }
            isAlreadyUsed = true;
            AspectResult _p = group.getAspect(line.second).use(new AspectController(controller.ceiling, controller.floor,
                    new ResourceEvaluator(rp -> rp.getResource(line.first.resource),
                            rp -> rp.getAmountOfResource(line.first.resource)), controller.isMeaningNeeded));
            resourcePack.add(_p.resources.getResource(line.first.resource).getResources().stream()
                    .flatMap(res -> res.applyAndConsumeAspect(line.first.aspect, controller.ceiling).stream()).collect(Collectors.toList()));
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
        return line.second;
    }

    @Override
    public LineDependency copy(Group group) {
        return new LineDependency(tag, group, line);
    }

    @Override
    public void swapDependencies(Group group) {
        line = new ShnyPair<>((ConverseWrapper) group.getAspect(line.first), (ConverseWrapper) group.getAspect(line.second));
        if (line.first == null || line.second == null) {
            throw new RuntimeException(String.format("Wrong swapping in Dependency %s", getName()));
        }
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
