package simulation.culture.group.reason;

import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;

public class BetterAspectUseReason extends AbstractReason {
    ConverseWrapper converseWrapper;

    public BetterAspectUseReason(Group group, ConverseWrapper converseWrapper) {
        super(group);
        this.converseWrapper = converseWrapper;
    }

    @Override
    public String toString() {
        return "Better " + converseWrapper.getName();
    }
}
