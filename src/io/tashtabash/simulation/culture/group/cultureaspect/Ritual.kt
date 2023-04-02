package io.tashtabash.simulation.culture.group.cultureaspect

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.reason.Reason


abstract class Ritual(var reason: Reason) : CultureAspect {
    abstract override fun adopt(group: Group): Ritual?
}
