package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ReasonConcept
import shmp.simulation.culture.group.cultureaspect.worship.ConceptObjectWorship
import shmp.simulation.culture.group.cultureaspect.worship.WorshipObject
import shmp.simulation.culture.group.cultureaspect.worship.WorshipObjectDependent
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.thinking.meaning.Meme
import java.util.*


class TaleSystem(
        tales: Collection<Tale>,
        val groupingConcept: ReasonConcept,
) : CultureAspect, WorshipObjectDependent {
    val tales: MutableSet<Tale> = tales.toMutableSet()
    override fun getRequest(group: Group): Request? = null

    fun addTale(tale: Tale) {
        tales.add(tale)
    }

    override fun use(group: Group) {
        tales.forEach { it.use(group) }
    }

    override fun adopt(group: Group): TaleSystem? {
        val newTales = tales.map { it.adopt(group) }
        if (newTales.any { it == null }) return null
        return TaleSystem(newTales, groupingConcept)
    }

    override fun die(group: Group) {}

    override fun swapWorship(worshipObject: WorshipObject) =
            if (worshipObject is ConceptObjectWorship && groupingConcept is ObjectConcept)
            TaleSystem(
            tales.map {
                Tale(it.template.copy(), it.info.changedInfo(groupingConcept))
            },
            groupingConcept
            )
            else null


    override fun toString(): String {
        return "Tale system about $groupingConcept"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as TaleSystem
        return groupingConcept == that.groupingConcept
    }

    override fun hashCode(): Int {
        return Objects.hash(groupingConcept)
    }
}