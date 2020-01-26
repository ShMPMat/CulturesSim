package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.aspect.dependency.LineDependency;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;

/**
 * Special Aspect which wraps around another aspect and resource and returns application
 * of this aspect to the resource.
 */
public class ConverseWrapper extends Aspect {
    /**
     * Aspect which is applied to the resource.
     */
    public Aspect aspect;
    /**
     * Resource on which aspect is applied.
     */
    public Resource resource;
    /**
     * Whether this ConverseWrapper can insert meaning.
     */
    public boolean canInsertMeaning = false;
    private ConverseWrapper traversedCopy;

    public ConverseWrapper(Aspect aspect, Resource resource, Group group) {
        super(new String[]{aspect.getName()+"On"+resource.getBaseName()}, new HashMap<>(), group);
        this.aspect = aspect;
        this.resource = resource.cleanCopy();
        for (Resource res : resource.applyAspect(aspect)) {
            aspectCore.addAllTags(res.getTags());
        }
        getRequirements().add(AspectTag.phony());
        getRequirements().addAll(aspect.getRequirements());
    }

    public AspectTag getRequirement() {
        return AspectTag.phony();
    }

    public List<Resource> getResult() {
        return resource.applyAspect(aspect);
    }

    @Override
    public Map<AspectTag, Set<Dependency>> getDependencies() {
        return dependencies;
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> use(int ceiling, ResourceEvaluator evaluator) {
        try {
            group.getAspect(aspect).markAsUsed();
        } catch (Exception e) {
            int i = 0;
        }
        return super.use(ceiling, evaluator);
    }

    public ConverseWrapper stripToMeaning() {
        if (traversedCopy != null) {
            return traversedCopy;
        }
        ConverseWrapper converseWrapper = copy(dependencies, group);
        traversedCopy = converseWrapper;
        converseWrapper.dependencies.put(AspectTag.phony(), new HashSet<>());
        for (Dependency dependency : dependencies.get(AspectTag.phony())) {
            if (!(dependency instanceof LineDependency)) {
                continue;
            }
            ConverseWrapper next = ((LineDependency) dependency).getNextWrapper();
            if (next != null && next.canInsertMeaning) {
                converseWrapper.dependencies.get(AspectTag.phony()).add(new LineDependency(dependency.getType(), group,
                        new ShnyPair<>(converseWrapper, next.stripToMeaning())));
                next.stripToMeaning();
            }
        }
        traversedCopy = null;
        return converseWrapper;
    }

    @Override
    public boolean isValid() {
        if (resource.getGenome().willResist() && aspect.getName().equals("Take")) {
            return false;
        }
        return true;
    }

    @Override
    public ConverseWrapper copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        ConverseWrapper copy = new ConverseWrapper(aspect, resource, group);
        copy.initDependencies(dependencies);
        try {
            copy.canInsertMeaning = dependencies.get(AspectTag.phony()).stream().anyMatch(dependency ->
                    dependency instanceof LineDependency &&
                            ((LineDependency) dependency).getNextWrapper().canInsertMeaning);
            return copy;
        } catch (Exception e) {
            int i = 0;
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspectCore.name, aspect, resource);
    }
}
