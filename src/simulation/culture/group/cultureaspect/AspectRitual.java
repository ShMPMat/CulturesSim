package simulation.culture.group.cultureaspect;

import simulation.Controller;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.centers.Group;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.request.EvaluatorsKt;
import simulation.culture.group.request.Request;
import simulation.culture.group.resource_behaviour.ResourceBehaviour;
import simulation.culture.group.resource_behaviour.ResourceBehaviourKt;

import java.util.Objects;

public class AspectRitual extends Ritual {
    private ResourceBehaviour resourceBehaviour;
    private ConverseWrapper converseWrapper;

    public AspectRitual(ConverseWrapper converseWrapper, ResourceBehaviour resourceBehaviour, Reason reason) {
        super(reason);
        this.converseWrapper = converseWrapper;
        this.resourceBehaviour = resourceBehaviour;
    }

    @Override
    public Request getRequest(Group group) {
        return null;
    }

    @Override
    public void use(Group group) {
        AspectResult result = converseWrapper.use(new AspectController(
                1,
                1,
                EvaluatorsKt.getPassingEvaluator(),
                group.getPopulationCenter(),
                group.getTerritoryCenter().getAccessibleTerritory(),
                true,
                group.getCultureCenter().getMeaning()
        ));
        if (result.isFinished) {
            group.getResourceCenter().addAll(result.resources);
            resourceBehaviour.proceedResources(result.resources);
        }
    }

    @Override
    public AspectRitual copy(Group group) {
        return new AspectRitual(//TODO isn't it copied wrong
                (ConverseWrapper) group.getCultureCenter().getAspectCenter().getAspectPool().getValue(converseWrapper),
                ResourceBehaviourKt.getRandom(group, Controller.session.random),
                getReason()
        );
    }

    @Override
    public String toString() {
        return "Ritual with " + converseWrapper.getName() + " " + resourceBehaviour + " because " + getReason();
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
