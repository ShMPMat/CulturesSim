package simulation.culture.group.cultureaspect

import simulation.culture.group.Group
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*

class TaleSystem(
        tales: Collection<Tale>,
        val groupingMeme: Meme,
        val infoTag: String
) : CultureAspect {
    val tales: MutableSet<Tale> = tales.toMutableSet()
    override fun getRequest(group: Group): Request? = null

    fun addTale(tale: Tale) {
        tales.add(tale)
    }

    override fun use(group: Group) {
        tales.forEach { it.use(group) }
    }

    override fun copy(group: Group): TaleSystem {
        return TaleSystem(tales.map { it.copy(group) }, groupingMeme.copy(), infoTag)
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