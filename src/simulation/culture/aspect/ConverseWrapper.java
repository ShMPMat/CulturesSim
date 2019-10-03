package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.culture.group.Group;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;

import java.util.*;
import java.util.function.Function;

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
    public Resource resource; //TODO change to ResourseIdeal
    public boolean canInsertMeaning = false;

    public ConverseWrapper(Aspect aspect, Resource resource, Group group) {
        super(new String[]{aspect.getName()+"On"+resource.getName()}, new HashMap<>(), group);
        this.aspect = aspect;
        this.resource = resource.cleanCopy();
        for (Resource res : resource.applyAspect(aspect)) {
            aspectCore.addAllTags(res.getTags());
        }
        getRequirements().add(new AspectTag("phony"));
        getRequirements().addAll(aspect.getRequirements());
    }

    public AspectTag getRequirement() {
        return getRequirements().stream().findFirst().get();
    }

    public List<Resource> getResult() {
        return resource.applyAspect(aspect);
    }

    @Override
    public Map<AspectTag, Set<Dependency>> getDependencies() {
        if (!aspect.getName().equals("TakeApart") && !aspect.getName().equals("Incinerate")) {
            int i = 0;
        }
//        Map<AspectTag, Set<Dependency>> _m = new HashMap<>(dependencies);
//        _m.putAll(aspect.dependencies);
        return dependencies;
    }

    @Override
    public ShnyPair<Boolean, ResourcePack> use(int ceiling, Function<ResourcePack, Integer> amount) {
        group.getAspect(aspect).markAsUsed();
        return super.use(ceiling, amount);
    }

    public ConverseWrapper stripToMeaning() {
        ConverseWrapper converseWrapper = copy(dependencies, group);
        converseWrapper.dependencies.put(new AspectTag("phony"), new HashSet<>());
        for (Dependency dependency : dependencies.get(new AspectTag("phony"))) {
            if (dependency.getNextWrapper() != null && dependency.getNextWrapper().canInsertMeaning) {
                converseWrapper.dependencies.get(new AspectTag("phony")).add(new Dependency(dependency.getType(),
                        new ShnyPair<>(converseWrapper, dependency.getNextWrapper().stripToMeaning()), group));
                dependency.getNextWrapper().stripToMeaning();
            }
        }
        return converseWrapper;
    }

    @Override
    public ConverseWrapper copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        ConverseWrapper _w = new ConverseWrapper(aspect, resource, group);
        _w.dependencies.putAll(dependencies);
        _w.canInsertMeaning = dependencies.get(new AspectTag("phony")).stream()
                .anyMatch(dependency -> dependency.getNextWrapper() != null && dependency.getNextWrapper()
                        .canInsertMeaning);
        return _w;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspectCore.name, aspect, resource);
    }
}
