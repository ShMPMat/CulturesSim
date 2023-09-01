package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.reason.Reason


abstract class Ritual(var reason: Reason) : CultureAspect {
    abstract override fun adopt(group: Group): Ritual?
}
