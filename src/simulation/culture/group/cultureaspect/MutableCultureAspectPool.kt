package simulation.culture.group.cultureaspect

class MutableCultureAspectPool(initialAspects: MutableSet<CultureAspect>) : CultureAspectPool(initialAspects) {
    fun add(aspect: CultureAspect) {
        if (when (aspect) {
            is Ritual -> ritualAdd(aspect)
            is RitualSystem -> ritualSystemAdd(aspect)
            is Tale -> taleAdd(aspect)
            is TaleSystem -> taleSystemAdd(aspect)
                    else -> false
        }) return
        aspectMap[aspect] = aspect
    }

    fun addAll(aspects: Collection<CultureAspect>) = aspects.forEach { add(it) }

    fun removeAll(cultureAspects: List<CultureAspect>) = cultureAspects.forEach {
        aspectMap.remove(it)
    }

    private fun ritualAdd(ritual: Ritual): Boolean {
        val system = getAll()
                .filterIsInstance<RitualSystem>()
                .firstOrNull { it.reason == ritual.reason }
                ?: return false
        system.addRitual(ritual)
        return true
    }

    private fun ritualSystemAdd(system: RitualSystem): Boolean {
        val existingSystem = getAll()
                .filterIsInstance<RitualSystem>()
                .firstOrNull { it.reason == system.reason }
                ?: return false
        system.rituals.forEach {
            existingSystem.addRitual(it)
        }
        return true
    }

    private fun taleAdd(tale: Tale): Boolean {
        val system = getAll()
                .filterIsInstance<TaleSystem>()
                .firstOrNull { tale.info.map[it.infoTag] == it.groupingMeme }
                ?: return false
        system.addTale(tale)
        return true
    }

    private fun taleSystemAdd(system: TaleSystem): Boolean {
        val existingSystem = getAll()
                .filterIsInstance<TaleSystem>()
                .firstOrNull { it.groupingMeme == system.groupingMeme && it.infoTag == system.infoTag }
                ?: return false
        system.tales.forEach {
            existingSystem.addTale(it)
        }
        return true
    }
}