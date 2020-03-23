package simulation.culture.group.cultureaspect

import simulation.culture.group.Group
import simulation.culture.group.request.Request

interface CultureAspect {
    fun getRequest(group: Group): Request?
    fun use(group: Group)
    fun copy(group: Group): CultureAspect
}