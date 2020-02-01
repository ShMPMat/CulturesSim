package simulation.culture.group.cultureaspect;

import simulation.culture.aspect.Aspect;
import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.reason.Reasons;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
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
        this(group, converseWrapper, ResourceBehaviour.getRandom(group), reason);
    }

    public AspectRitual(Group group, ConverseWrapper converseWrapper) {
        this(group, converseWrapper, ResourceBehaviour.getRandom(group), Reasons.randomReason(group));
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        AspectResult result = converseWrapper.use(new AspectController(1, 1,
                new ResourceEvaluator(rp -> rp, ResourcePack::getAmount), true));
        if (result.isFinished) {
            group.cherishedResources.add(result.resources);
            resourceBehaviour.procedeResources(result.resources);
        }
    }

    @Override
    public AspectRitual copy(Group group) {
        return new AspectRitual(group, (ConverseWrapper) group.getAspect(converseWrapper), reason);
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
