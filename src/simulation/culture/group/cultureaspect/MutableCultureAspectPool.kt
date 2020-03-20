package simulation.culture.group.cultureaspect

class MutableCultureAspectPool(initialAspects: MutableSet<CultureAspect>) : CultureAspectPool(initialAspects) {
    fun add(aspect: CultureAspect) {
        if (aspect is Ritual) {
            val system = getAll()
                    .filterIsInstance<RitualSystem>()
                    .firstOrNull { it.reason == aspect.reason }
            if (system != null) {
                system.addRitual(aspect)
                return
            }
        } else if (aspect is RitualSystem) {
            val system = getAll()
                    .filterIsInstance<RitualSystem>()
                    .firstOrNull { it.reason == aspect.reason }
            if (system != null) {
                aspect.rituals.forEach {
                    system.addRitual(it)
                }
                return
            }
        }
        aspectMap[aspect] = aspect
    }

    fun addAll(aspects: Collection<CultureAspect>) = aspects.forEach { add(it) }

    fun removeAll(cultureAspects: List<CultureAspect>) = cultureAspects.forEach {
        aspectMap.remove(it)
    }
}