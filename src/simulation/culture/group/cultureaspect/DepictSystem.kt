package simulation.culture.group.cultureaspect

import simulation.culture.group.CultureCenter
import simulation.culture.group.Group
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*

class DepictSystem(
        group: Group,
        depictions: Collection<DepictObject>,
        val groupingMeme: Meme
) : AbstractCultureAspect(group) {
    val depictions: MutableSet<DepictObject> = depictions.toMutableSet()

    fun addDepiction(depiction: DepictObject) {
        depictions.add(depiction)
    }

    override fun getRequest(): Request? {
        return null
    }

    override fun use(center: CultureCenter) {
        depictions.forEach { it.use(center) }
    }

    override fun copy(group: Group): DepictSystem {
        return DepictSystem(group, depictions.map { it.copy(group) }, groupingMeme.copy())
    }

    override fun toString(): String {
        return "Depiction system about $groupingMeme"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as DepictSystem
        return groupingMeme == that.groupingMeme
    }

    override fun hashCode(): Int {
        return Objects.hash(groupingMeme)
    }
}