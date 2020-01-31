package simulation.culture.group.cultureaspect;

import simulation.culture.aspect.AspectController;
import simulation.culture.aspect.AspectResult;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;
import simulation.culture.group.ResourceBehaviour;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.request.Request;
import simulation.culture.group.request.ResourceEvaluator;
import simulation.space.resource.ResourcePack;

import java.util.Objects;

public class CultureAspectRitual extends Ritual {
    private CultureAspect aspect;

    public CultureAspectRitual(Group group, CultureAspect aspect, Reason reason) {
        super(group, reason);
        this.aspect = aspect;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use() {
        aspect.use();
    }

    @Override
    public String toString() {
        return String.format("Ritual with %s because %s", aspect, reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CultureAspectRitual that = (CultureAspectRitual) o;
        return Objects.equals(aspect, that.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspect);
    }
}
