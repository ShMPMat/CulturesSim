package simulation.culture.group.reason;

import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;

import static extra.ProbFunc.*;

public class Reasons {
    public static Reason randomReason(Group group) {
        Reason reason = null;
        switch (randomInt(1)) {
            case 0:
                ConverseWrapper converseWrapper = randomElement(group.getCulturalCenter().getConverseWrappers());
                if (converseWrapper != null) {
                    reason = new BetterAspectUseReason(group, converseWrapper);
                    break;
                }
        }
        return reason;
    }
}
