package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.worship.WorshipObject
import simulation.culture.group.cultureaspect.worship.WorshipObjectDependent
import simulation.culture.group.request.Request
import simulation.culture.thinking.meaning.Meme
import java.util.*

class DepictSystem(
        depictions: Collection<DepictObject>,
        val groupingMeme: Meme
) : WorshipObjectDependent {
    val depictions: MutableSet<DepictObject> = depictions.toMutableSet()

    fun addDepiction(depiction: DepictObject) {
        depictions.add(depiction)
    }

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        depictions.forEach { it.use(group) }
    }

    override fun adopt(group: Group): DepictSystem? {
        val newDepictions = depictions.map { it.adopt(group) }
        if (newDepictions.any { it == null }) return null
        return DepictSystem( newDepictions.filterNotNull(), groupingMeme.copy())
    }

    override fun die(group: Group) = depictions.forEach { it.die(group) }

    override fun swapWorship(worshipObject: WorshipObject) =
            DepictSystem(depictions.map { it.swapWorship(worshipObject) }, worshipObject.name)

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