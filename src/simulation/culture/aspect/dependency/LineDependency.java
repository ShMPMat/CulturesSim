package simulation.culture.aspect.dependency;

import extra.ShnyPair;
import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectTag;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class LineDependency extends AbstractDependency {
    private boolean isAlreadyUsed = false;
    private Group group;
    private ShnyPair<ConverseWrapper, ConverseWrapper> line;

    public LineDependency(AspectTag tag, Group group, ShnyPair<ConverseWrapper, ConverseWrapper> line) {
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
    public boolean isCycleDependency(Aspect aspect) {//TODO prohibit dependencies from same aspect which are not separated by other aspects;
        if (isAlreadyUsed) {
            return false;
        }
        isAlreadyUsed = true;
        boolean b = line.second.getDependencies().values().stream().anyMatch(dependencies -> dependencies.stream()
                .anyMatch(dependency -> dependency.isCycleDependency(aspect))) || (line.second.equals(aspect) /*&& line.second != aspect*/);
        isAlreadyUsed = false;
        return b;
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> useDependency(int ceiling, ResourceEvaluator evaluator) {
        try {
        ResourcePack resourcePack = new ResourcePack();
        if (isAlreadyUsed || ceiling <= 0) {
            return new ShnyPair<>(true, resourcePack);
        }
        isAlreadyUsed = true;
            ShnyPair<Boolean, ResourcePack> _p = group.getAspect(line.second).use(ceiling,
                    new ResourceEvaluator(rp -> rp.getResource(line.first.resource),
                            rp -> rp.getAmountOfResource(line.first.resource)));
            _p.second.getResource(line.first.resource).getResources()
                    .forEach(res -> res.applyAndConsumeAspect(line.first.aspect, ceiling));
            resourcePack.add(_p.second);
            isAlreadyUsed = false;
            return new ShnyPair<>(_p.first, resourcePack);
        } catch (NullPointerException e) {
            throw new RuntimeException("No such aspect in Group");
        }
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
            int i = 0;
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
