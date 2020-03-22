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

    fun remove(cultureAspect: CultureAspect) = aspectMap.remove(cultureAspect)

    fun removeAll(cultureAspects: List<CultureAspect>) = cultureAspects.forEach {
        remove(it)
    }

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
                .firstOrNull { tale.info.getMainPart(it.infoTag) == it.groupingMeme }
                ?: return false
        system.addTale(tale)
        return true
    }

    private fun taleSystemAdd(system: TaleSystem): Boolean {
        var existingSystem = taleSystems
                .firstOrNull { it.groupingMeme == system.groupingMeme && it.infoTag == system.infoTag }
                ?: return false
        system.tales.forEach {
            existingSystem.addTale(it)
        }
        return true
    }

    private val ritualSystems
        get() = getAll()
                .filterIsInstance<RitualSystem>()

    private val taleSystems
        get() = getAll()
                .filterIsInstance<TaleSystem>()
                .union(getAll()
                        .filterIsInstance<Deity>()
                        .map { it.taleSystem }
                )
}