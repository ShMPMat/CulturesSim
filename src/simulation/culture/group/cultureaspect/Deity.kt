package simulation.culture.group.cultureaspect

import simulation.culture.group.CultureCenter
import simulation.culture.group.Group
import simulation.culture.group.request.Request

class Deity(
        group: Group,
        val taleSystem: TaleSystem
) : AbstractCultureAspect(group) {
    override fun getRequest(): Request? {
        return null
    }

    override fun use(center: CultureCenter) {
        taleSystem.use(center)
    }

    override fun copy(group: Group): Deity {
        return Deity(group, taleSystem.copy(group))
    }

    override fun toString(): String {
        return "Deity ${taleSystem.groupingMeme}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Deity

        if (taleSystem != other.taleSystem) return false

        return true
    }

    override fun hashCode(): Int {
        return taleSystem.hashCode()
    }
}