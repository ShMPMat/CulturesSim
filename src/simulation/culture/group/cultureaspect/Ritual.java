package simulation.culture.group.cultureaspect;

import simulation.culture.group.Group;
import simulation.culture.group.reason.Reason;

public abstract class Ritual extends AbstractCultureAspect {
    Reason reason;

    public Ritual(Group group, Reason reason) {
        super(group);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
