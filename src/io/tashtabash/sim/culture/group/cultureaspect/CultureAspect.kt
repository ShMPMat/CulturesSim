package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.request.Request


interface  CultureAspect {
    fun getRequest(group: Group): Request?

    fun use(group: Group)

    fun adopt(group: Group): CultureAspect?

    fun die(group: Group)
}
