package simulation.culture.group.cultureaspect;

import simulation.culture.group.Group;
import simulation.culture.group.reason.Reason;
import simulation.culture.group.request.Request;

import java.util.Objects;

public class CultureAspectRitual extends Ritual {
    private CultureAspect aspect;

    public CultureAspectRitual(CultureAspect aspect, Reason reason) {
        super(reason);
        this.aspect = aspect;
    }

    @Override
    public Request getRequest() {
        return null;
    }

    @Override
    public void use(Group group) {
        aspect.use(group);
    }

    @Override
    public CultureAspectRitual copy(Group group) {
        return new CultureAspectRitual(aspect.copy(group), getReason());
    }

    @Override
    public String toString() {
        return String.format("Ritual with %s because %s", aspect, getReason());
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
