package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

data class CultWorship(
        val worship: Worship
) : CultureAspect {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) = worship.use(group)

    override fun copy(group: Group) = CultWorship(worship.copy(group))
    override fun toString(): String {
        return "Cult of $worship"
    }


}