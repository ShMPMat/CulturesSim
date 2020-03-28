package simulation.culture.aspect;

import simulation.culture.aspect.dependency.AspectDependencies;
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
    AspectDependencies dependencies = new AspectDependencies(new HashMap<>());
    /**
     * Coefficient which represents how much this aspect is used by its owner.
     */
    private int usefulness = 50;
    /**
     * Whether it was used on this turn.
     */
    private boolean used = false;
    public boolean canInsertMeaning = false;

    Aspect(AspectCore aspectCore, AspectDependencies dependencies) {
        this.aspectCore = aspectCore;
        initDependencies(dependencies);
    }

    void initDependencies(AspectDependencies dependencies) {
        for (Map.Entry<ResourceTag, Set<Dependency>> entry: dependencies.getMap().entrySet()) {
            this.dependencies.getMap().put(
                    entry.getKey(),
                    entry.getValue().stream()
                            .map(Dependency::copy)
                            .collect(Collectors.toSet())
            );
        }
    }

    public void swapDependencies(AspectCenter aspectCenter) {
        dependencies.getMap().values().forEach(set ->
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

    public AspectDependencies getDependencies() {
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

    public boolean isDependenciesOk(AspectDependencies dependencies) {
        return getRequirements().size() == dependencies.getSize();
    }

    public boolean canTakeResources() {
        return false;
    }

    @Deprecated
    public void mergeDependencies(Aspect aspect) {//TODO what's going on here?
        if (!aspect.equals(this)) {
            return;
        }

        for (ResourceTag tag : dependencies.getMap().keySet()) {
            dependencies.getMap().get(tag).addAll(aspect.dependencies.getMap().get(tag));
        }
    }

    public void addOneDependency(AspectDependencies newDependencies) {
        for (ResourceTag tag : dependencies.getMap().keySet()) {
            try {
                for (Dependency dependency1 : newDependencies.getMap().get(tag)) {
                    if (!dependencies.getMap().get(tag).contains(dependency1)) {
                        dependencies.getMap().get(tag).add(dependency1);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
                int i = 0;
            }
        }
    }

    public Aspect copy(AspectDependencies dependencies) {
        return aspectCore.copy(dependencies);
    }

    public AspectResult use(AspectController controller) {//TODO instrument efficiency
        //TODO put dependency resources only in node; otherwise they may merge with phony
        MutableResourcePack meaningfulPack = new MutableResourcePack();
        controller.setCeiling(controller.getPopulationCenter().changeStratumAmountByAspect(
                this,
                controller.getCeiling()
        ));
        AspectResult.ResultNode node = new AspectResult.ResultNode(this);
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : dependencies.getMap().entrySet()) {
            if (!satisfyRequirement(controller, entry.getValue(), entry.getKey(), meaningfulPack, node)) {
                new AspectResult(false, node);
            }
        }
        if (controller.isFloorExceeded(meaningfulPack)) {
            markAsUsed();
        }
        return new AspectResult(meaningfulPack, node);
    }

    private boolean satisfyRequirement(
            AspectController controller,
            Set<Dependency> dependencies,
            ResourceTag requirementTag,
            MutableResourcePack meaningfulPack,
            AspectResult.ResultNode node
            ) {
        boolean isFinished;
        if (requirementTag.equals(ResourceTag.phony())) {
            isFinished = satisfyPhonyDependency(controller, dependencies, meaningfulPack);
        } else {
            isFinished = satisfyRegularDependency(controller, requirementTag, dependencies, meaningfulPack, node);
        }
        return isFinished;
    }

    private boolean satisfyPhonyDependency(
            AspectController controller,
            Set<Dependency> dependencies,
            MutableResourcePack meaningfulPack
    ) {
        for (Dependency dependency : dependencies) {
            int newDelta = meaningfulPack.getAmount(((ConverseWrapper) this).resource);
            AspectResult _p = dependency.useDependency(new AspectController(
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
        }
        return true;
    }

    private boolean satisfyRegularDependency(
            AspectController controller,
            ResourceTag requirementTag,
            Set<Dependency> dependencies,
            MutableResourcePack meaningfulPack,
            AspectResult.ResultNode node
    ) {
        boolean isFinished = false;
        MutableResourcePack _rp = new MutableResourcePack();
        _rp.addAll(
                controller.getPopulationCenter().getStratumByAspect(this).getInstrumentByTag(requirementTag)
        );
        MutableResourcePack usedForDependency = new MutableResourcePack();
        for (Dependency dependency : dependencies) {
            int newDelta = _rp.getAmount(requirementTag);
            AspectResult result = dependency.useDependency(new AspectController(
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
            if (_rp.getAmount(requirementTag) >= controller.getCeiling()) {
                if (!requirementTag.isInstrumental) {
                    //TODO sometimes can spend resources without getting resources because other dependencies are lacking
                    usedForDependency.addAll(_rp.getAmountOfResourcesWithTagAndErase(
                            requirementTag,
                            controller.getCeiling()).getSecond()
                    );
                } else {
                    usedForDependency.addAll(_rp.getResources(requirementTag));
                }
                meaningfulPack.addAll(_rp);
                isFinished = true;
                break;
            }
        }
        node.resourceUsed.put(requirementTag, usedForDependency);
        return isFinished;
    }

    protected boolean shouldPassMeaningNeed(boolean isMeaningNeeded) {
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
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : dependencies.getMap().entrySet()) {
            stringBuilder.append("\n**").append(entry.getKey().name).append(":");
            for (Dependency dependency : entry.getValue()) {
                stringBuilder.append("\n**").append(dependency.getName());
            }
        }
        return stringBuilder.toString();
    }
}
