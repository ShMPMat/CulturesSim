package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.group.cultureaspect.worship.MultigroupWorshipWrapper
import io.tashtabash.sim.culture.group.cultureaspect.worship.Worship

class MutableCultureAspectPool(initialAspects: MutableSet<CultureAspect>) : CultureAspectPool(initialAspects) {
    fun add(aspect: CultureAspect) {
        if (when (aspect) {
                    is Ritual -> ritualAdd(aspect)
                    is RitualSystem -> ritualSystemAdd(aspect)
                    is Tale -> taleAdd(aspect)
                    is TaleSystem -> taleSystemAdd(aspect)
                    is DepictObject -> depictObjectAdd(aspect)
                    is DepictSystem -> depictSystemAdd(aspect)
                    is Worship -> worshipAdd(aspect)
                    is MultigroupWorshipWrapper -> worshipAdd(aspect.worship)
                    else -> false
                }) return
        aspectMap[aspect] = aspect
    }

    fun addAll(aspects: Collection<CultureAspect>) = aspects.forEach { add(it) }

    fun remove(cultureAspect: CultureAspect) = aspectMap.remove(cultureAspect)

    fun removeAll(cultureAspects: List<CultureAspect>) = cultureAspects.mapNotNull { remove(it) }

    private fun ritualAdd(ritual: Ritual): Boolean {
        val system = ritualSystems
                .firstOrNull { it.reason == ritual.reason }
                ?: return false
        system.addRitual(ritual)
        return true
    }

    private fun ritualSystemAdd(system: RitualSystem): Boolean {
        val existingSystem = ritualSystems
                .firstOrNull { it.reason == system.reason }
                ?: return false
        system.rituals.forEach {
            existingSystem.addRitual(it)
        }
        return true
    }

    private fun taleAdd(tale: Tale): Boolean {
        val system = taleSystems
                .firstOrNull { tale.info.actorConcept == it.groupingConcept }
                ?: return false
        system.addTale(tale)
        return true
    }

    private fun taleSystemAdd(system: TaleSystem): Boolean {
        val existingSystem = taleSystems
                .firstOrNull { it.groupingConcept == system.groupingConcept }
                ?: return false
        system.tales.forEach {
            existingSystem.addTale(it)
        }
        return true
    }

    private fun depictObjectAdd(depiction: DepictObject): Boolean {
        val existingSystem = depictSystems
                .firstOrNull { s -> depiction.meme.anyMatch { s.groupingMeme == it.topMemeCopy() } }
                ?: return false
        existingSystem.addDepiction(depiction)
        return true
    }

    private fun depictSystemAdd(system: DepictSystem): Boolean {
        val existingSystem = depictSystems
                .firstOrNull { system.groupingMeme == it.groupingMeme }
                ?: return false
        system.depictions.forEach {
            existingSystem.addDepiction(it)
        }
        return true
    }

    private fun worshipAdd(worship: Worship): Boolean {
        return worship in worships
    }
}