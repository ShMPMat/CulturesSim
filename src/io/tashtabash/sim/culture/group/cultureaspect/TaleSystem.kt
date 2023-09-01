package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.generator.culture.worldview.reasoning.concept.ReasonConcept
import io.tashtabash.sim.culture.group.cultureaspect.worship.ConceptObjectWorship
import io.tashtabash.sim.culture.group.cultureaspect.worship.GodWorship
import io.tashtabash.sim.culture.group.cultureaspect.worship.WorshipObject
import io.tashtabash.sim.culture.group.cultureaspect.worship.WorshipObjectDependent
import io.tashtabash.sim.culture.group.request.Request
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
            when (worshipObject) {
                is ConceptObjectWorship -> TaleSystem(
                        tales.map {
                            Tale(it.template.copy(), it.info.changedInfo(worshipObject.objectConcept))
                        },
                        worshipObject.objectConcept
                )
                is GodWorship -> TaleSystem(
                        tales.map {
                            Tale(it.template.copy(), it.info.changedInfo(worshipObject))
                        },
                        worshipObject
                )
                else -> {
                    println("Unknown WorshipObject type ${worshipObject.javaClass.simpleName} for TaleSystem")
                    null
                }
            }


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