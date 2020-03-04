package simulation.culture.aspect;

import simulation.World;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.Group;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cultural token, which is owned by group and represents some cultural value.
 */
public class Aspect {
    /**
     * Core which stores all properties non-mutable for all Aspects with same name.
     */
    AspectCore aspectCore;
    /**
     * Map which stores for every requirement some Dependencies, from which
     * we can get get resource for using this aspect.
     */
    Map<ResourceTag, Set<Dependency>> dependencies = new HashMap<>();
    /**
     * Coefficient which represents how much this aspect is used by its owner.
     */
    private int usefulness = 50;
    /**
     * Whether it was used on this turn.
     */
    private boolean used = false;
    /**
     * Group which owns this aspect.
     */
    protected Group group;
    /**
     * Whether this Aspect can insert meaning.
     */
    public boolean canInsertMeaning = false;

    /**
     * Base constructor.
     *
     * @param aspectCore   core with common properties.
     * @param dependencies dependencies for all requirements.
     */
    Aspect(AspectCore aspectCore, Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        this.aspectCore = aspectCore;
        this.group = group;
        initDependencies(dependencies);
    }

    /**
     * Constructor from core tags.
     *
     * @param tags         tags for common Aspect properties.
     * @param dependencies dependencies for all requirements.
     */
    public Aspect(String[] tags, Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        this(new AspectCore(tags), dependencies, group);
    }

    public static Collection<Aspect> getAllAspectsWithTag(ResourceTag resourceTag, World world) {
        return world.aspectPool.stream().filter(aspect -> aspect.getTags().contains(resourceTag))
                .collect(Collectors.toList());
    }

    void initDependencies(Map<ResourceTag, Set<Dependency>> dependencies) {
        for (Map.Entry<ResourceTag, Set<Dependency>> entry: dependencies.entrySet()) {
            this.dependencies.put(entry.getKey(),
                    entry.getValue().stream().map(dependency -> dependency.copy(group)).collect(Collectors.toSet()));
        }
    }

    public void swapDependencies() {
        dependencies.values().forEach(set -> set.forEach(dependency -> dependency.swapDependencies(group)));
    }

    /**
     * Name getter.
     *
     * @return name of this Aspect.
     */
    public String getName() {
        return aspectCore.getName();
    }

    /**
     * Tags getter
     *
     * @return tags of this Aspect.
     */
    public Collection<ResourceTag> getTags() {
        return aspectCore.getTags();
    }

    /**
     * Requirements getter.
     *
     * @return requirements for this Aspect.
     */
    public Collection<ResourceTag> getRequirements() {
        return aspectCore.getRequirements();
    }

    public Collection<ResourceTag> getWrapperRequirements() {
        return aspectCore.getRequirements().stream().filter(aspectTag -> aspectTag.isConverseCondition)
                .collect(Collectors.toList());
    }

    public Group getGroup() {
        return group;
    }

    /**
     * Dependencies getter.
     *
     * @return dependencies for this Aspect.
     */
    public Map<ResourceTag, Set<Dependency>> getDependencies() {
        return dependencies;
    }

    public List<AspectMatcher> getMatchers() {
        return aspectCore.matchers;
    }

    public int getUsefulness() {
        return usefulness;
    }

    public boolean isValid() {
        return true;
    }

    public boolean canApplyMeaning() {
        return aspectCore.applyMeaning;
    }

    public boolean canReturnMeaning() {
        return this instanceof ConverseWrapper && ((ConverseWrapper) this).canInsertMeaning;
    }

    public boolean isDependenciesOk(Map<ResourceTag, Set<Dependency>> dependencies) {
        return getRequirements().size() - getWrapperRequirements().size() == dependencies.size();
    }

    public boolean canTakeResources() {
        return false;
    }

    @Deprecated
    public void mergeDependencies(Aspect aspect) {
        if (!aspect.getName().equals(getName())) {
            return;
        }

        for (ResourceTag tag : dependencies.keySet()) {
            dependencies.get(tag).addAll(aspect.dependencies.get(tag));
        }
    }

    public void addOneDependency(Map<ResourceTag, Set<Dependency>> newDependencies) {
        for (ResourceTag tag : dependencies.keySet()) {
            try {
                for (Dependency dependency1 : newDependencies.get(tag)) {
                    if (!dependencies.get(tag).contains(dependency1)) {
                        dependencies.get(tag).add(dependency1);
                        break;
                    }
                }
            } catch (Exception e) {
                int i = 0;
            }
        }
    }

    public Aspect copy(Map<ResourceTag, Set<Dependency>> dependencies, Group group) {
        return aspectCore.copy(dependencies, group);
    }

    public AspectResult use(AspectController controller) {//TODO instrument efficiency
        boolean isFinished;//TODO put dependency resources only in node; otherwise they may merge with phony
        ResourcePack meaningfulPack = new ResourcePack();
        controller.ceiling = group.changeStratumAmountByAspect(this, controller.ceiling);
        AspectResult.ResultNode node = new AspectResult.ResultNode(this);
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : getDependencies().entrySet()) {
            Set<Dependency> dependency = entry.getValue();
            ResourcePack usedForDependency = new ResourcePack();
            isFinished = false;
            ResourcePack _rp = new ResourcePack();
            ResourcePack provided = group.getStratumByAspect(this).getInstrumentByTag(entry.getKey());
            if (provided != null) {
                _rp.add(provided);
            }
            for (Dependency dependency1 : dependency) {
                if (dependency1.isPhony()) {
                    isFinished = true;
                    int newDelta = meaningfulPack.getAmountOfResource(((ConverseWrapper) this).resource);
                    AspectResult _p = dependency1.useDependency(new AspectController(controller.ceiling -
                            newDelta, controller.floor - newDelta, controller.evaluator,
                            shouldPassMeaningNeed(controller.isMeaningNeeded)));
                    if (!_p.isFinished) {
                        continue;
                    }
                    meaningfulPack.add(_p.resources);
                    if (controller.isCeilingExceeded(meaningfulPack)) {
                        break;
                    }
                } else {
                    int newDelta = _rp.getAmountOfResourcesWithTag(dependency1.getType());
                    AspectResult result = dependency1.useDependency(new AspectController(controller.ceiling -
                            newDelta, controller.floor - newDelta, controller.evaluator, false));
                    _rp.add(result.resources);
                    if (!result.isFinished) {
                        continue;
                    }
                    if (_rp.getAmountOfResourcesWithTag(dependency1.getType()) >= controller.ceiling) {
                        if (!dependency1.getType().isInstrumental) {//TODO sometimes can spend resources without getting resources because other dependencies are lacking
                            usedForDependency.add(_rp.getAmountOfResourcesWithTagAndErase(dependency1.getType(), controller.ceiling).second);
                        } else {
                            usedForDependency.add(_rp.getAllResourcesWithTag(dependency1.getType()));
                        }
                        meaningfulPack.add(_rp);
                        isFinished = true;
                        break;
                    }
                }
            }
            node.resourceUsed.put(entry.getKey(), usedForDependency);
            if (!isFinished) {
                return new AspectResult(false, node);
            }
        }
        if (controller.isFloorExceeded(meaningfulPack)) {
            markAsUsed();
        }
        return new AspectResult(meaningfulPack, node);
    }

    boolean shouldPassMeaningNeed(boolean isMeaningNeeded) {
        return isMeaningNeeded;
    }

    protected void markAsUsed() {
        usefulness += 1;
        used = true;
    }

    public void finishUpdate() {
        if (!used) {
            usefulness--;
        }
        used = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Aspect aspect = (Aspect) o;
        return aspectCore.name.equals(aspect.aspectCore.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspectCore.name);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Aspect " + aspectCore.name + ", usefulness - " + usefulness +
                ", dependencies:");
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : dependencies.entrySet()) {
            stringBuilder.append("\n**").append(entry.getKey().name).append(":");
            for (Dependency dependency : entry.getValue()) {
                stringBuilder.append("\n**").append(dependency.getName());
            }
        }
        return stringBuilder.toString();
    }
}
