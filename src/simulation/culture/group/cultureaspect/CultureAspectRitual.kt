package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.reason.Reason
import simulation.culture.group.request.Request
import java.util.*

class CultureAspectRitual(private val aspect: CultureAspect, reason: Reason?) : Ritual(reason!!) {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) = aspect.use(group)

    override fun adopt(group: Group): CultureAspectRitual? {
        return CultureAspectRitual(aspect.adopt(group) ?: return null, reason)
    }

    override fun toString() = "Ritual with $aspect because $reason"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CultureAspectRitual
        return aspect == that.aspect
    }

    override fun hashCode() = Objects.hash(aspect)

    override fun die(group: Group) = aspect.die(group)
}