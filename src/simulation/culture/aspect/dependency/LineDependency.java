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
        ResourcePack resourcePack = new ResourcePack();
        if (isAlreadyUsed || ceiling <= 0) {
            return new ShnyPair<>(true, resourcePack);
        }
        isAlreadyUsed = true;
        try {
            ShnyPair<Boolean, ResourcePack> _p = group.getAspect(line.second).use(ceiling,
                    new ResourceEvaluator(rp -> rp.getResource(line.first.resource),
                            rp -> rp.getAmountOfResource(line.first.resource)));
            _p.second.getResource(line.first.resource).getResources()
                    .forEach(res -> res.applyAndConsumeAspect(line.first.aspect, ceiling));
            resourcePack.add(_p.second);
            isAlreadyUsed = false;
            return new ShnyPair<>(_p.first, resourcePack);
        } catch (Exception e) {
            int i = 0;
            return null;
        }
    }

    public ConverseWrapper getNextWrapper() {
        return line.second;
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
