package simulation.culture.group.reason;

import shmp.random.RandomCollectionsKt;
import simulation.Controller;
import simulation.culture.aspect.ConverseWrapper;
import simulation.culture.group.Group;

import java.util.List;

public class Reasons {
    public static Reason randomReason(Group group) {
        Reason reason = null;
        switch (Controller.session.random.nextInt(1)) {
            case 0:
                List<ConverseWrapper> wrappers =
                        group.getCultureCenter().getAspectCenter().getAspectPool().getConverseWrappers();
                if (!wrappers.isEmpty()) {
                    ConverseWrapper converseWrapper = RandomCollectionsKt.randomElement(
                            wrappers,
                            Controller.session.random
                    );
                    reason = new BetterAspectUseReason(group, converseWrapper);
                    break;
                }
        }
        return reason;
    }
}
