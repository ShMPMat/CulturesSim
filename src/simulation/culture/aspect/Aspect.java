package simulation.culture.aspect;

import kotlin.Pair;
import simulation.Controller;
import simulation.culture.aspect.dependency.AspectDependencies;
import simulation.culture.aspect.dependency.Dependency;
import simulation.culture.group.centers.AspectCenter;
import simulation.culture.group.request.EvaluatorsKt;
import simulation.space.resource.MutableResourcePack;
import simulation.space.resource.Resource;
import simulation.space.resource.ResourcePack;
import simulation.space.resource.tag.ResourceTag;
import simulation.space.resource.tag.labeler.ResourceLabeler;
import simulation.space.resource.tag.labeler.TagLabeler;

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
    public boolean used = false;
    private boolean usedThisTurn = false;
    public boolean canInsertMeaning = false;

    public Aspect(AspectCore aspectCore, AspectDependencies dependencies) {
        this.aspectCore = aspectCore;
        initDependencies(dependencies);
    }

    void initDependencies(AspectDependencies dependencies) {
        for (Map.Entry<ResourceTag, Set<Dependency>> entry : dependencies.getMap().entrySet()) {
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

    public List<Resource> getProducedResources() {
        return Collections.emptyList();
    }

    public AspectResult use(AspectController controller) {//TODO instrument efficiency
        //TODO put dependency resources only in node; otherwise they may merge with phony
        if (controller.getDepth() > Controller.session.maxGroupDependencyDepth || used) {
            return new AspectResult(
                    false,
                    new ArrayList<>(),
                    new MutableResourcePack(),
                    new AspectResult.ResultNode(this)
            );
        }
        used = true;
        boolean isFinished = true;
        List<Pair<ResourceLabeler, Integer>> neededResources = new ArrayList<>();
        MutableResourcePack meaningfulPack = new MutableResourcePack();
        int neededWorkers = controller.getCeilingSatisfiableAmount(getProducedResources());
        int gotWorkers = controller.getPopulationCenter().changeStratumAmountByAspect((ConverseWrapper) this, neededWorkers);
        controller.setMax(gotWorkers);
        AspectResult.ResultNode node = new AspectResult.ResultNode(this);
        if (controller.getCeiling() > 0) {
            for (Map.Entry<ResourceTag, Set<Dependency>> entry : dependencies.getNonPhony().entrySet()) {
                if (!satisfyRegularDependency(controller, entry.getKey(), entry.getValue(), meaningfulPack, node)) {
                    isFinished = false;
                    neededResources.add(new Pair<>(new TagLabeler(entry.getKey()), controller.getCeiling()));
                }
            }
        }
        if (isFinished) {
            isFinished = satisfyPhonyDependency(controller, dependencies.getPhony(), meaningfulPack);
        }
        if (controller.isFloorExceeded(meaningfulPack)) {
            markAsUsed();
        } else {
            controller.getPopulationCenter().freeStratumAmountByAspect((ConverseWrapper) this, gotWorkers);
        }
        used = false;
        return new AspectResult(isFinished, neededResources, meaningfulPack, node);
    }

    private boolean satisfyPhonyDependency(
            AspectController controller,
            Set<Dependency> dependencies,
            MutableResourcePack meaningfulPack
    ) {
        meaningfulPack.addAll(getPhonyFromResources(controller));
        if (controller.isCeilingExceeded(meaningfulPack)) {
            return true;
        }
        for (Dependency dependency : dependencies) {
            int newDelta = meaningfulPack.getAmount(((ConverseWrapper) this).resource);
            AspectResult _p = dependency.useDependency(new AspectController(
                    controller.getDepth() + 1,
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

    ResourcePack getPhonyFromResources(AspectController controller) {
        ResourcePack pack = EvaluatorsKt.resourceEvaluator(((ConverseWrapper) this).resource).pick(
                controller.getPopulationCenter().getTurnResources()
        );
        return controller.pickCeilingPart(
                pack.getResources(),
                r -> r.applyAspect(((ConverseWrapper) this).aspect),
                (r, n) -> r.applyAndConsumeAspect(((ConverseWrapper) this).aspect, n)
        );
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
                controller.pickCeilingPart(
                        controller.getPopulationCenter().getStratumByAspect((ConverseWrapper) this)
                                .getInstrumentByTag(requirementTag).getResources(),
                        r -> Collections.singletonList(r.copy(1)),
                        (r, n) -> Collections.singletonList(r.getCleanPart(n))
                )
        );
        MutableResourcePack usedForDependency = new MutableResourcePack();

        for (Dependency dependency : dependencies) {
            int newDelta = _rp.getAmount(requirementTag);
            AspectResult result = dependency.useDependency(new AspectController(
                    controller.getDepth() + 1,
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
                    //TODO sometimes can spend resources without getting result because other dependencies are lacking
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
        usedThisTurn = true;
    }

    public void finishUpdate() {
        if (!usedThisTurn) {
            usefulness--;
        }
        usedThisTurn = false;
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