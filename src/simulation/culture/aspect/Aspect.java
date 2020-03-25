package simulation.culture.aspect;

import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.AspectCenter;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.tag.ResourceTag;

import java.util.*;
import java.util.stream.Collectors;

public class Aspect {
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
    public boolean canInsertMeaning = false;

    Aspect(AspectCore aspectCore, Map<ResourceTag, Set<Dependency>> dependencies) {
        this.aspectCore = aspectCore;
        initDependencies(dependencies);
    }

    void initDependencies(Map<ResourceTag, Set<Dependency>> dependencies) {
        for (Map.Entry<ResourceTag, Set<Dependency>> entry: dependencies.entrySet()) {
            this.dependencies.put(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(Dependency::copy)
                            .collect(Collectors.toSet())
            );
        }
    }

    public void swapDependencies(AspectCenter aspectCenter) {
        dependencies.values().forEach(set ->
                set.forEach(d ->
                        d.swapDependencies(aspectCenter)
                )
        );
    }

    public String getName() {
        return aspectCore.getName();
    }

    public Collection<ResourceTag> getTags() {
        return aspectCore.getTags();
    }

    public Collection<ResourceTag> getRequirements() {
        return aspectCore.getRequirements();
    }

    public Map<ResourceTag, Set<Dependency>> getDependencies() {
        return dependencies;
    }

    public List<AspectMatcher> getMatchers() {
        return aspectCore.getMatchers();
    }

    public int getUsefulness() {
        return usefulness;
    }

    public boolean isValid() {
        return true;
    }

    public boolean canApplyMeaning() {
        return aspectCore.getApplyMeaning();
    }

    public boolean canReturnMeaning() {
        return this instanceof ConverseWrapper && ((ConverseWrapper) this).canInsertMeaning;
    }

    public boolean isDependenciesOk(Map<ResourceTag, Set<Dependency>> dependencies) {
        return getRequirements().size() == dependencies.size();
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
                System.err.println(e);
                int i = 0;
            }
        }
    }

    public Aspect copy(Map<ResourceTag, Set<Dependency>> dependencies) {
        return aspectCore.copy(dependencies);
    }

    public AspectResult use(AspectController controller) {//TODO instrument efficiency
        boolean isFinished;//TODO put dependency resources only in node; otherwise they may merge with phony
        MutableResourcePack meaningfulPack = new MutableResourcePack();
        controller.setCeiling(controller.getPopulationCenter().changeStratumAmountByAspect(
                this,
                controller.getCeiling()
        ));
        AspectResult.ResultNode node = new AspectResult.ResultNode(this);
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : getDependencies().entrySet()) {
            Set<Dependency> dependency = entry.getValue();
            MutableResourcePack usedForDependency = new MutableResourcePack();
            isFinished = false;
            MutableResourcePack _rp = new MutableResourcePack();
            MutableResourcePack provided = controller.getPopulationCenter().getStratumByAspect(this)
                    .getInstrumentByTag(entry.getKey());
            if (provided != null) {
                _rp.addAll(provided);
            }
            for (Dependency dependency1 : dependency) {
                if (dependency1.isPhony()) {
                    isFinished = true;
                    int newDelta = meaningfulPack.getAmount(((ConverseWrapper) this).resource);
                    AspectResult _p = dependency1.useDependency(new AspectController(
                            controller.getCeiling() - newDelta,
                            controller.getFloor() - newDelta,
                            controller.getEvaluator(),
                            controller.getPopulationCenter(),
                            controller.getTerritory(),
                            shouldPassMeaningNeed(controller.isMeaningNeeded()),
                            controller.getMeaning()
                    ));
                    if (!_p.isFinished) {
                        continue;
                    }
                    meaningfulPack.addAll(_p.resources);
                    if (controller.isCeilingExceeded(meaningfulPack)) {
                        break;
                    }
                } else {
                    int newDelta = _rp.getAmount(dependency1.getType());
                    AspectResult result = dependency1.useDependency(new AspectController(
                            controller.getCeiling() - newDelta,
                            controller.getFloor() - newDelta,
                            controller.getEvaluator(),
                            controller.getPopulationCenter(),
                            controller.getTerritory(),
                            false,
                            controller.getMeaning()
                    ));
                    _rp.addAll(result.resources);
                    if (!result.isFinished) {
                        continue;
                    }
                    if (_rp.getAmount(dependency1.getType()) >= controller.getCeiling()) {
                        if (!dependency1.getType().isInstrumental) {//TODO sometimes can spend resources without getting resources because other dependencies are lacking
                            usedForDependency.addAll(_rp.getAmountOfResourcesWithTagAndErase(
                                    dependency1.getType(),
                                    controller.getCeiling()).getSecond()
                            );
                        } else {
                            usedForDependency.addAll(_rp.getResources(dependency1.getType()));
                        }
                        meaningfulPack.addAll(_rp);
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
        return aspectCore.getName().equals(aspect.aspectCore.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspectCore.getName());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Aspect " + aspectCore.getName() + ", usefulness - " + usefulness +
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
