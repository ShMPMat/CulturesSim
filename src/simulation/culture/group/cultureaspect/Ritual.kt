package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.reason.Reason

abstract class Ritual(var reason: Reason) : CultureAspect {
    abstract override fun adopt(group: Group): Ritual?
}