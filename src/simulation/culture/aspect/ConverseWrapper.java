package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.aspect.dependency.LineDependency;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

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
    private ConverseWrapper traversedCopy;

    public ConverseWrapper(Aspect aspect, Resource resource, Group group) {
        super(new String[]{aspect.getName()+"On"+resource.getBaseName()}, new HashMap<>(), group);
        if (aspect.getName().equals("Paint")) {
            int i = 0;
        }
        this.aspect = aspect;
        this.resource = resource.cleanCopy();
        for (Resource res : resource.applyAspect(aspect)) {
            aspectCore.addAllTags(res.getTags());
        }
        getRequirements().add(ResourceTag.phony());
        getRequirements().addAll(aspect.getRequirements().stream().filter(tag -> !tag.isConverseCondition)
                .collect(Collectors.toList()));
    }

    public ResourceTag getRequirement() {
        return ResourceTag.phony();
    }

    @Override
    public Collection<ResourceTag> getWrapperRequirements() {
        return aspect.getWrapperRequirements();
    }

    public List<Resource> getResult() {
        return resource.applyAspect(aspect);
    }

    @Override
    public Map<ResourceTag, Set<Dependency>> getDependencies() {
        return dependencies;
    }

    @Override
    public AspectResult use(AspectController controller) {
        try {
            group.getAspect(aspect).markAsUsed();
        if (aspect.getName().equals("Paint")) {
            int i = 0;
        }
        return super.use(controller);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }

//    public ConverseWrapper stripToMeaning() {
//        if (traversedCopy != null) {
//            return traversedCopy;
//        }
//        ConverseWrapper converseWrapper = copy(dependencies, group);
//        traversedCopy = converseWrapper;
//        converseWrapper.dependencies.put(AspectTag.phony(), new HashSet<>());
//        for (Dependency dependency : dependencies.get(AspectTag.phony())) {
//            if (!(dependency instanceof LineDependency)) {
//                continue;
//            }
//            ConverseWrapper next = ((LineDependency) dependency).getNextWrapper();
//            if (next != null && next.canInsertMeaning) {
//                converseWrapper.dependencies.get(AspectTag.phony()).add(new LineDependency(dependency.getType(), group,
//                        new ShnyPair<>(converseWrapper, next.stripToMeaning())));
//                next.stripToMeaning();
//            }
//        }
//        traversedCopy = null;
//        return converseWrapper;
//    }

    @Override
    public boolean isDependenciesOk(Map<ResourceTag, Set<Dependency>> dependencies) {
        return getRequirements().size() == dependencies.size();
    }

    @Override
    public boolean isValid() {
        if (resource.getGenome().willResist() && aspect.getName().equals("Take")) {
            return false;
        }
        if (!resource.hasApplicationForAspect(aspect)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canTakeResources() {
        return aspect.getName().equals("Take") || aspect.getName().equals("Killing");
    }

    @Override
    public ConverseWrapper copy(Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        ConverseWrapper copy = new ConverseWrapper(aspect, resource, group);
        copy.initDependencies(dependencies);
        try {
            copy.canInsertMeaning = dependencies.get(ResourceTag.phony()).stream().anyMatch(dependency ->
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
