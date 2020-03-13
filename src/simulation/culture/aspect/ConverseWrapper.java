package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.aspect.dependency.LineDependency;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.tag.ResourceTag;

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

    public ConverseWrapper(Aspect aspect, Resource resource, Group group) {
        super(
                new AspectCore(
                        aspect.getName() + "On" + resource.getBaseName(),
                        getReducedTags(resource, aspect),
                        getDependencies(aspect),
                        new ArrayList<>(),
                        false
                        ),
                new HashMap<>(),
                group
        );
        this.aspect = aspect;
        this.resource = resource.cleanCopy();
    }

    private static List<ResourceTag> getReducedTags(Resource resource, Aspect aspect) {
        List<ResourceTag> result = new ArrayList<>();
        List<ResourceTag> allTags = resource.applyAspect(aspect).stream()
                .flatMap(r -> r.getTags().stream())
                .collect(Collectors.toList());
        for (ResourceTag tag: allTags) {
            if (!result.contains(tag)) {
                result.add(tag);
            } else if (result.get(result.indexOf(tag)).level < tag.level) {
                result.remove(tag);
                result.add(tag);
            }
        }
        return result;
    }

    private static List<ResourceTag> getDependencies(Aspect aspect) {
        List<ResourceTag> result = new ArrayList<>();
        result.add(ResourceTag.phony());
        result.addAll(aspect.getRequirements().stream()
                .filter(tag -> !tag.isConverseCondition)
                .collect(Collectors.toList()));
        return result;
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
            group.getCulturalCenter().getAspectCenter().getAspectPool().get(aspect).markAsUsed();
            return super.use(controller);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }

    @Override
    public boolean isDependenciesOk(Map<ResourceTag, Set<Dependency>> dependencies) {
        return getRequirements().size() == dependencies.size();
    }

    @Override
    public boolean isValid() {
        if (resource.getGenome().isResisting() && aspect.getName().equals("Take")) {
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
        return Objects.hash(aspectCore.getName(), aspect, resource);
    }
}
