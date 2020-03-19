package simulation.culture.group.cultureaspect;

import simulation.Controller;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.resource_behaviour.ResourceBehaviour;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.reason.Reasons;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.culture.group.resource_behaviour.ResourceBehaviourKt;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class AspectRitual extends Ritual {
    private ResourceBehaviour resourceBehaviour;
    private ConverseWrapper converseWrapper;

    public AspectRitual(Group group, ConverseWrapper converseWrapper, ResourceBehaviour resourceBehaviour, Reason reason) {
        super(group, reason);
        this.converseWrapper = converseWrapper;
        this.resourceBehaviour = resourceBehaviour;
    }

    public AspectRitual(Group group, ConverseWrapper converseWrapper, Reason reason) {
        this(group, converseWrapper, ResourceBehaviourKt.getRandom(group, Controller.session.random), reason);
    }

    public AspectRitual(Group group, ConverseWrapper converseWrapper) {
        this(
                group,
                converseWrapper,
                ResourceBehaviourKt.getRandom(group, Controller.session.random), Reasons.randomReason(group)
        );
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        AspectResult result = converseWrapper.use(new AspectController(
                1,
                1,
                new ResourceEvaluator(rp -> rp, ResourcePack::getAmount),
                group.getPopulationCenter(),
                group.getTerritoryCenter().getTerritory(),
                true,
                group.getCultureCenter().getMeaning()
        ));
        if (result.isFinished) {
            group.cherishedResources.addAll(result.resources);
            resourceBehaviour.proceedResources(result.resources);
        }
    }

    @Override
    public AspectRitual copy(Group group) {
        return new AspectRitual(
                group,
                (ConverseWrapper) group.getCultureCenter().getAspectCenter().getAspectPool().get(converseWrapper),
                reason
        );
    }

    @Override
    public String toString() {
        return "Ritual with " + converseWrapper.getName() + " " + resourceBehaviour + " because " + reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AspectRitual that = (AspectRitual) o;
        return Objects.equals(converseWrapper, that.converseWrapper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(converseWrapper);
    }
}
