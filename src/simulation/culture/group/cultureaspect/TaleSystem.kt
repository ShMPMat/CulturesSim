package simulation.culture.group.cultureaspect

import simulation.culture.group.CultureCenter
import simulation.culture.group.Group
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*

class TaleSystem(
        group: Group,
        tales: Collection<Tale>,
        val groupingMeme: Meme,
        val infoTag: String
) : AbstractCultureAspect(group) {
    val tales: MutableSet<Tale> = tales.toMutableSet()

    fun addTale(tale: Tale) {
        tales.add(tale)
    }

    override fun getRequest(): Request? {
        return null
    }

    override fun use(center: CultureCenter) {
        tales.forEach { it.use(center) }
    }

    override fun copy(group: Group): TaleSystem {
        return TaleSystem(group, tales.map { it.copy(group) }, groupingMeme.copy(), infoTag)
    }

    override fun toString(): String {
        return "Tale system about $groupingMeme"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TaleSystem
        return groupingMeme == that.groupingMeme
    }

    override fun hashCode(): Int {
        return Objects.hash(groupingMeme)
    }
}