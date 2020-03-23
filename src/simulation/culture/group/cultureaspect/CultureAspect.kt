package simulation.culture.group.cultureaspect

import simulation.culture.group.Group
import simulation.culture.group.request.Request

interface CultureAspect {
    val request: Request?
    fun use(group: Group)
    fun copy(group: Group): CultureAspect
}