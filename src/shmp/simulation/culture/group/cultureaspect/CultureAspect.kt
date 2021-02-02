package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.Request


interface  CultureAspect {
    fun getRequest(group: Group): Request?

    fun use(group: Group)

    fun adopt(group: Group): CultureAspect?

    fun die(group: Group)
}
