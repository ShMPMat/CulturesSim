package simulation.culture.group.reason;

import simulation.culture.group.Group;

public class AbstractReason implements Reason {
    Group group;

    public AbstractReason(Group group) {
        this.group = group;
    }
}
