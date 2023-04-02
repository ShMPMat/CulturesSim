package io.tashtabash.simulation.culture.group.cultureaspect

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.simulation.culture.group.cultureaspect.worship.WorshipObject
import io.tashtabash.simulation.culture.group.cultureaspect.worship.WorshipObjectDependent
import io.tashtabash.simulation.culture.group.request.Request
import io.tashtabash.generator.culture.worldview.Meme
import java.util.*

class DepictSystem(
        depictions: Collection<DepictObject>,
        val groupingMeme: Meme,
        val objectConcept: ObjectConcept?
) : CultureAspect, WorshipObjectDependent {
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
        return DepictSystem( newDepictions.filterNotNull(), groupingMeme.copy(), objectConcept)
    }

    override fun die(group: Group) = depictions.forEach { it.die(group) }

    override fun swapWorship(worshipObject: WorshipObject) =
            DepictSystem(depictions.map { it.swapWorship(worshipObject) }, worshipObject.name, objectConcept)

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