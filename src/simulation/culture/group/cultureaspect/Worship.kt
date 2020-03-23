package simulation.culture.group.cultureaspect

import simulation.culture.group.CultureCenter
import simulation.culture.group.Group
import simulation.culture.group.request.Request

class Worship(
        val taleSystem: TaleSystem,
        val depictSystem: DepictSystem
) : CultureAspect {
    override fun getRequest(): Request? {
        return null
    }

    override fun use(center: CultureCenter) {
        taleSystem.use(center)
        depictSystem.use(center)
    }

    override fun copy(group: Group): Worship {
        return Worship(taleSystem.copy(group), depictSystem.copy(group))
    }

    override fun toString(): String {
        return "Deity ${taleSystem.groupingMeme}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Worship

        if (taleSystem != other.taleSystem) return false

        return true
    }

    override fun hashCode(): Int {
        return taleSystem.hashCode()
    }
}