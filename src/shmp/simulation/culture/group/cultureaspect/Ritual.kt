package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.reason.Reason


abstract class Ritual(var reason: Reason) : CultureAspect {
    abstract override fun adopt(group: Group): Ritual?
}
