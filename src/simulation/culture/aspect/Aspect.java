package simulation.culture.aspect;

import extra.ShnyPair;
import simulation.World;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.Group;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

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
    Map<AspectTag, Set<Dependency>> dependencies = new HashMap<>();
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
     * Base constructor.
     *
     * @param aspectCore   core with common properties.
     * @param dependencies dependencies for all requirements.
     */
    Aspect(AspectCore aspectCore, Map<AspectTag, Set<Dependency>> dependencies, Group group) {
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
    public Aspect(String[] tags, Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        this(new AspectCore(tags), dependencies, group);
    }

    public static Collection<Aspect> getAllAspectsWithTag(AspectTag aspectTag, World world) {
        return world.aspectPool.stream().filter(aspect -> aspect.getTags().contains(aspectTag))
                .collect(Collectors.toList());
    }

    void initDependencies(Map<AspectTag, Set<Dependency>> dependencies) {
        for (Map.Entry<AspectTag, Set<Dependency>> entry: dependencies.entrySet()) {
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
    public Collection<AspectTag> getTags() {
        return aspectCore.getTags();
    }

    /**
     * Requirements getter.
     *
     * @return requirements for this Aspect.
     */
    public Collection<AspectTag> getRequirements() {
        return aspectCore.getRequirements();
    }

    public Group getGroup() {
        return group;
    }

    /**
     * Dependencies getter.
     *
     * @return dependencies for this Aspect.
     */
    public Map<AspectTag, Set<Dependency>> getDependencies() {
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

    public boolean isDependenciesOk(Map<AspectTag, Set<Dependency>> dependencies) {
        return getRequirements().size() == dependencies.size();
    }

    @Deprecated
    public void mergeDependencies(Aspect aspect) {
        if (!aspect.getName().equals(getName())) {
            return;
        }

        for (AspectTag tag : dependencies.keySet()) {
            dependencies.get(tag).addAll(aspect.dependencies.get(tag));
        }
    }

    public void addOneDependency(Map<AspectTag, Set<Dependency>> newDependencies) {
        for (AspectTag tag : dependencies.keySet()) {
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

    public Aspect copy(Map<AspectTag, Set<Dependency>> dependencies, Group group) {
        return aspectCore.copy(dependencies, group);
    }

    public AspectResult use(int ceiling, ResourceEvaluator evaluator) {//TODO instrument efficiency
        boolean isFinished;//TODO not increase importance if floor is not reached
        markAsUsed();//TODO wrire Instruction class to control use of aspect (floor, ceiling, whether I want resources with a meaning)
        ResourcePack meaningfulPack = new ResourcePack();
        ceiling = group.changeStratumAmountByAspect(this, ceiling);
        for (Set<Dependency> dependency : getDependencies().values()) {
            isFinished = false;
            ResourcePack _rp = new ResourcePack();
            for (Dependency dependency1 : dependency) {
                if (dependency1.isPhony()) {
                    isFinished = true;
                    AspectResult _p = dependency1.useDependency(ceiling -
                            meaningfulPack.getAmountOfResource(((ConverseWrapper) this).resource), evaluator);
                    if (!_p.isFinished) {
                        continue;
                    }
                    meaningfulPack.add(_p.resources);
                    if (evaluator.evaluate(meaningfulPack) >= ceiling) {
                        break;
                    }
                } else {
                    AspectResult _p = dependency1.useDependency(ceiling -
                            _rp.getAmountOfResourcesWithTag(dependency1.getType()), evaluator);
                    _rp.add(_p.resources);
                    if (!_p.isFinished) {
                        continue;
                    }
                    if (_rp.getAmountOfResourcesWithTag(dependency1.getType()) >= ceiling) {
                        if (!dependency1.getType().isInstrumental) {//TODO sometimes can spend resources without getting resources because other dependencies are lacking
                            _rp.getAmountOfResourcesWithTagAndErase(dependency1.getType(), ceiling);
                        }
                        meaningfulPack.add(_rp);
                        isFinished = true;
                        break;
                    }
                }
            }
            if (!isFinished) {
                return new AspectResult(false);
            }
        }
        return  new AspectResult(meaningfulPack);
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
        for (Map.Entry<AspectTag, Set<Dependency>> entry : dependencies.entrySet()) {
            stringBuilder.append("\n**").append(entry.getKey().name).append(":");
            for (Dependency dependency : entry.getValue()) {
                stringBuilder.append("\n**").append(dependency.getName());
            }
        }
        return stringBuilder.toString();
    }
}
