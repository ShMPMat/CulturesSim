package simulation.culture.group;

import simulation.culture.aspect.Aspect;
import simulation.culture.group.cultureaspect.CultureAspect;

public class Groups {
    /**
     * This operation is not commutative.
     * @return double from 0 to 1.
     */
    public static double getGroupsDifference(Group g1, Group g2) {
        double matched = 1;
        double overall = 1;
        for (Aspect aspect: g1.getAspects()) {
            if (g2.getCulturalCenter().getAspectCenter().getAspectPool().contains(aspect)) {
                matched++;
            }
            overall++;
        }
        for (CultureAspect aspect: g1.getCulturalCenter().getCultureAspects()) {
            if (g2.getCulturalCenter().getCultureAspects().contains(aspect)) {
                matched++;
            }
            overall++;
        }
        return matched / overall;
    }
}
