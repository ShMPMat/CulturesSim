package io.tashtabash.simulation.culture.group.cultureaspect

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.request.Request


interface  CultureAspect {
    fun getRequest(group: Group): Request?

    fun use(group: Group)

    fun adopt(group: Group): CultureAspect?

    fun die(group: Group)
}
