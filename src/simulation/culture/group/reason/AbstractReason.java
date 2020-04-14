package simulation.culture.group.reason;

import simulation.culture.group.centers.Group;

public class AbstractReason implements Reason {
    Group group;

    public AbstractReason(Group group) {
        this.group = group;
    }
}
